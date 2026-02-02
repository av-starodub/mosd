package io.github.avstarodub.mosd.scheduler.app;

import io.github.avstarodub.mosd.exception.ResourceNotFoundException;
import io.github.avstarodub.mosd.scheduler.api.dto.EstimateRequest;
import io.github.avstarodub.mosd.scheduler.api.dto.EstimateResponse;
import io.github.avstarodub.mosd.scheduler.api.dto.ItemEstimate;
import io.github.avstarodub.mosd.scheduler.api.dto.ItemRequest;
import io.github.avstarodub.mosd.scheduler.api.dto.OperationEstimate;
import io.github.avstarodub.mosd.scheduler.api.dto.ProcessEstimate;
import io.github.avstarodub.mosd.scheduler.api.dto.ProcessSelectionRequest;
import io.github.avstarodub.mosd.scheduler.api.dto.QueueEstimate;
import io.github.avstarodub.mosd.scheduler.api.dto.ScheduledBatch;
import io.github.avstarodub.mosd.scheduler.domain.port.ProcessProvider;
import io.github.avstarodub.mosd.space.domain.Operation;
import io.github.avstarodub.mosd.space.domain.Process;
import io.github.avstarodub.mosd.space.domain.RateOption;
import io.github.avstarodub.mosd.space.domain.TimeOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EstimateService {

    private final ProcessProvider processProvider;

    public EstimateResponse estimate(EstimateRequest request) {
        var context = new SimulationContext(request.orderId());

        for (var itemRequest : request.items()) {
            processItemRequest(context, itemRequest);
        }

        runSimulation(context);

        return buildResponse(context);
    }

    private void processItemRequest(SimulationContext context, ItemRequest itemRequest) {
        var itemData = new ItemData(itemRequest.externalId(), new ArrayList<>());
        context.getItems().add(itemData);

        for (int i = 0; i < itemRequest.processSelections().size(); i++) {
            var selection = itemRequest.processSelections().get(i);
            var processData = validateAndBuildProcessData(selection);
            itemData.processes.add(processData);
            createBatchesForProcess(context, itemRequest.externalId(), i, processData);
        }
    }

    private ProcessData validateAndBuildProcessData(ProcessSelectionRequest selection) {
        validatePositiveOptionalValues(selection);
        var process = findProcess(selection);
        validateVolumeUnit(selection, process);
        var rateOption = findRateOption(process, selection.selectedRateOptionKey());
        var operations = validateAndBuildOperations(process, selection);
        return buildProcessData(selection, process, rateOption, operations);
    }

    private Process findProcess(ProcessSelectionRequest selection) {
        if (selection.version() != null) {
            return processProvider
                    .findByKeyAndVersion(selection.processKey(), selection.version())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Process", "processKey/version", selection.processKey() + "/" + selection.version()));
        }
        return processProvider
                .findByKey(selection.processKey())
                .orElseThrow(() -> new ResourceNotFoundException("Process", "processKey", selection.processKey()));
    }

    private void validatePositiveOptionalValues(ProcessSelectionRequest selection) {
        if (selection.version() != null && selection.version() <= 0) {
            throw new IllegalArgumentException("Process version must be > 0");
        }
        if (selection.batchVolumeOverride() != null && selection.batchVolumeOverride() <= 0) {
            throw new IllegalArgumentException("Batch volume override must be > 0");
        }
    }

    private void validateVolumeUnit(ProcessSelectionRequest selection, Process process) {
        if (!selection.volumeUnit().equals(process.volumeUnit())) {
            throw new IllegalArgumentException(String.format(
                    "Volume unit '%s' doesn't match process '%s' unit '%s'",
                    selection.volumeUnit(), selection.processKey(), process.volumeUnit()));
        }
    }

    private RateOption findRateOption(Process process, String rateOptionKey) {
        return process.rateOptions().stream()
                .filter(r -> r.key().equals(rateOptionKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "RateOption '%s' not found in process '%s'", rateOptionKey, process.processKey())));
    }

    private List<OperationData> validateAndBuildOperations(Process process, ProcessSelectionRequest selection) {
        var operations = new ArrayList<OperationData>();
        for (var opSelection : selection.operationSelections()) {
            var operation = findOperation(process, opSelection.operationName());
            var timeOption = findTimeOption(operation, opSelection.selectedTimeOptionKey(), process.processKey());
            operations.add(new OperationData(opSelection.operationName(), timeOption.key(), timeOption.duration()));
        }
        return operations;
    }

    private Operation findOperation(Process process, String operationName) {
        return process.operations().stream()
                .filter(op -> op.name().equals(operationName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Operation '%s' not found in process '%s'", operationName, process.processKey())));
    }

    private TimeOption findTimeOption(Operation operation, String timeOptionKey, String processKey) {
        return operation.timeOptions().stream()
                .filter(t -> t.key().equals(timeOptionKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "TimeOption '%s' not found in operation '%s' of process '%s'",
                        timeOptionKey, operation.name(), processKey)));
    }

    private ProcessData buildProcessData(
            ProcessSelectionRequest selection, Process process, RateOption rateOption, List<OperationData> operations) {
        int operationsMinutes =
                operations.stream().mapToInt(OperationData::durationMinutes).sum();
        int batchVolume = determineBatchVolume(selection, process);
        int numberOfBatches = (int) Math.ceil((double) selection.volume() / batchVolume);
        int actualBatchVolume = Math.min(batchVolume, selection.volume());
        int rateMinutes = calculateRateMinutes(actualBatchVolume, rateOption);
        int minutesPerBatch = operationsMinutes + rateMinutes;

        return new ProcessData(
                selection.processKey(),
                process.version(),
                numberOfBatches,
                minutesPerBatch,
                operationsMinutes,
                rateMinutes,
                operations);
    }

    private int determineBatchVolume(ProcessSelectionRequest selection, Process process) {
        if (selection.batchVolumeOverride() != null) {
            return selection.batchVolumeOverride();
        }
        if (process.batchVolume() != null) {
            return process.batchVolume();
        }
        return selection.volume();
    }

    private int calculateRateMinutes(int batchVolume, RateOption rateOption) {
        var rate = rateOption.rate();
        int batches = (int) Math.ceil((double) batchVolume / rate.units());
        return batches * rate.minutes();
    }

    private void createBatchesForProcess(
            SimulationContext context, String itemExternalId, int processIndex, ProcessData processData) {
        var processKey = processData.processKey();
        context.getProcessQueues().putIfAbsent(processKey, new LinkedList<>());
        context.getScheduledBatches().putIfAbsent(processKey, new ArrayList<>());
        context.getProcessAvailableAt().putIfAbsent(processKey, 0);

        for (int batchNum = 1; batchNum <= processData.numberOfBatches(); batchNum++) {
            var batch = new PendingBatch(
                    itemExternalId, batchNum, processIndex, processKey, processData.minutesPerBatch(), 0);
            if (processIndex == 0) {
                context.getProcessQueues().get(processKey).add(batch);
            } else {
                context.getWaitingForPrevious().add(batch);
            }
        }
    }

    private void runSimulation(SimulationContext context) {
        startInitialBatches(context);

        while (!context.getEvents().isEmpty()) {
            var event = context.getEvents().poll();
            processCompletionEvent(context, event);
        }
    }

    private void startInitialBatches(SimulationContext context) {
        for (var processKey : context.getProcessQueues().keySet()) {
            tryStartNextBatch(context, processKey);
        }
    }

    private void processCompletionEvent(SimulationContext context, CompletionEvent event) {
        context.getProcessAvailableAt().put(event.processKey(), event.endMinute());
        context.getBatchEndTimes().put(batchKey(event), event.endMinute());

        moveReadyBatchesToQueues(context, event);
        tryStartNextBatch(context, event.processKey());
    }

    private void moveReadyBatchesToQueues(SimulationContext context, CompletionEvent event) {
        var iterator = context.getWaitingForPrevious().iterator();
        while (iterator.hasNext()) {
            var waiting = iterator.next();
            if (isPreviousBatchCompleted(waiting, event)) {
                waiting = waiting.withReadyAt(event.endMinute());
                context.getProcessQueues().get(waiting.processKey()).add(waiting);
                iterator.remove();
                tryStartNextBatch(context, waiting.processKey());
            }
        }
    }

    private boolean isPreviousBatchCompleted(PendingBatch waiting, CompletionEvent event) {
        if (!waiting.itemExternalId().equals(event.itemExternalId())) {
            return false;
        }
        if (waiting.batchNumber() != event.batchNumber()) {
            return false;
        }
        return waiting.processIndex() == event.processIndex() + 1;
    }

    private void tryStartNextBatch(SimulationContext context, String processKey) {
        var queue = context.getProcessQueues().get(processKey);
        if (queue == null || queue.isEmpty()) {
            return;
        }

        var batch = queue.poll();
        int availableAt = context.getProcessAvailableAt().get(processKey);
        int startAt = Math.max(availableAt, batch.readyAt());
        int endAt = startAt + batch.durationMinutes();

        context.getScheduledBatches()
                .get(processKey)
                .add(new ScheduledBatch(
                        batch.itemExternalId(), batch.batchNumber(), startAt, endAt, batch.durationMinutes()));

        context.getEvents()
                .add(new CompletionEvent(
                        endAt, batch.itemExternalId(), batch.batchNumber(), batch.processIndex(), processKey));
    }

    private String batchKey(CompletionEvent event) {
        return event.itemExternalId() + ":" + event.batchNumber() + ":" + event.processIndex();
    }

    private EstimateResponse buildResponse(SimulationContext context) {
        int totalMinutes = calculateTotalMinutes(context);
        var items = buildItemEstimates(context);
        var queues = buildQueueEstimates(context);
        return new EstimateResponse(context.getOrderId(), totalMinutes, items, queues);
    }

    private int calculateTotalMinutes(SimulationContext context) {
        return context.getScheduledBatches().values().stream()
                .flatMap(List::stream)
                .mapToInt(ScheduledBatch::endMinute)
                .max()
                .orElse(0);
    }

    private List<ItemEstimate> buildItemEstimates(SimulationContext context) {
        var itemEstimates = new ArrayList<ItemEstimate>();
        for (var itemData : context.getItems()) {
            int itemMinutes = calculateItemMinutes(context, itemData.externalId());
            var processes = buildProcessEstimates(itemData);
            itemEstimates.add(new ItemEstimate(itemData.externalId(), itemMinutes, processes));
        }
        return itemEstimates;
    }

    private int calculateItemMinutes(SimulationContext context, String itemExternalId) {
        return context.getScheduledBatches().values().stream()
                .flatMap(List::stream)
                .filter(b -> b.itemExternalId().equals(itemExternalId))
                .mapToInt(ScheduledBatch::endMinute)
                .max()
                .orElse(0);
    }

    private List<ProcessEstimate> buildProcessEstimates(ItemData itemData) {
        var estimates = new ArrayList<ProcessEstimate>();
        for (var data : itemData.processes()) {
            estimates.add(toProcessEstimate(data));
        }
        return estimates;
    }

    private ProcessEstimate toProcessEstimate(ProcessData data) {
        var operations = new ArrayList<OperationEstimate>();
        for (var op : data.operations()) {
            operations.add(new OperationEstimate(op.operationName(), op.timeOptionKey(), op.durationMinutes()));
        }
        int estimatedMinutes = data.numberOfBatches() * data.minutesPerBatch();
        return new ProcessEstimate(
                data.processKey(),
                data.version(),
                estimatedMinutes,
                data.numberOfBatches(),
                data.minutesPerBatch(),
                data.operationsMinutes(),
                data.rateMinutes(),
                operations);
    }

    private List<QueueEstimate> buildQueueEstimates(SimulationContext context) {
        var queues = new ArrayList<QueueEstimate>();
        for (var entry : context.getScheduledBatches().entrySet()) {
            var batches = entry.getValue().stream()
                    .sorted(Comparator.comparingInt(ScheduledBatch::startMinute))
                    .toList();
            int totalMinutes =
                    batches.stream().mapToInt(ScheduledBatch::endMinute).max().orElse(0);
            queues.add(new QueueEstimate(entry.getKey(), totalMinutes, batches));
        }
        return queues;
    }

    // Internal data structures for simulation
    @lombok.Getter
    private static class SimulationContext {

        private final String orderId;

        private final List<ItemData> items = new ArrayList<>();

        private final Map<String, Queue<PendingBatch>> processQueues = new HashMap<>();

        private final Map<String, Integer> processAvailableAt = new HashMap<>();

        private final Map<String, List<ScheduledBatch>> scheduledBatches = new HashMap<>();

        private final List<PendingBatch> waitingForPrevious = new ArrayList<>();

        private final Map<String, Integer> batchEndTimes = new HashMap<>();

        private final PriorityQueue<CompletionEvent> events =
                new PriorityQueue<>(Comparator.comparingInt(CompletionEvent::endMinute));

        SimulationContext(String orderId) {
            this.orderId = orderId;
        }
    }

    private record ItemData(String externalId, List<ProcessData> processes) {}

    private record ProcessData(
            String processKey,
            int version,
            int numberOfBatches,
            int minutesPerBatch,
            int operationsMinutes,
            int rateMinutes,
            List<OperationData> operations) {}

    private record OperationData(String operationName, String timeOptionKey, int durationMinutes) {}

    private record PendingBatch(
            String itemExternalId,
            int batchNumber,
            int processIndex,
            String processKey,
            int durationMinutes,
            int readyAt) {
        PendingBatch withReadyAt(int newReadyAt) {
            return new PendingBatch(itemExternalId, batchNumber, processIndex, processKey, durationMinutes, newReadyAt);
        }
    }

    private record CompletionEvent(
            int endMinute, String itemExternalId, int batchNumber, int processIndex, String processKey) {}
}
