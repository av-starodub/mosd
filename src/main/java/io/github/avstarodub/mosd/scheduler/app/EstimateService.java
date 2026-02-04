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
import io.github.avstarodub.mosd.scheduler.api.dto.RateOverrideRequest;
import io.github.avstarodub.mosd.scheduler.api.dto.RateSelectionUsed;
import io.github.avstarodub.mosd.scheduler.api.dto.ScheduledBatch;
import io.github.avstarodub.mosd.scheduler.domain.port.ProcessProvider;
import io.github.avstarodub.mosd.space.domain.Operation;
import io.github.avstarodub.mosd.space.domain.Process;
import io.github.avstarodub.mosd.space.domain.Rate;
import io.github.avstarodub.mosd.space.domain.RateOption;
import io.github.avstarodub.mosd.space.domain.TimeOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    private static final Comparator<CompletionEvent> COMPLETION_EVENT_ORDER = Comparator.comparingInt(
                    CompletionEvent::endMinute)
            .thenComparingInt(CompletionEvent::itemOrder)
            .thenComparingInt(CompletionEvent::batchNumber)
            .thenComparingInt(CompletionEvent::processIndex)
            .thenComparing(CompletionEvent::processKey)
            .thenComparing(CompletionEvent::itemExternalId);

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
        int itemOrder = context.getItems().size();
        var itemData = new ItemData(itemOrder, itemRequest.externalId(), new ArrayList<>());
        context.getItems().add(itemData);
        context.getItemsByOrder().put(itemOrder, itemData);

        for (int i = 0; i < itemRequest.processSelections().size(); i++) {
            var selection = itemRequest.processSelections().get(i);
            var processData = validateAndBuildProcessData(selection);
            itemData.processes.add(processData);
            ensureProcessStructures(context, processData.processKey());
        }

        if (!itemData.processes().isEmpty()) {
            createInitialBatches(context, itemData);
        }
    }

    private ProcessData validateAndBuildProcessData(ProcessSelectionRequest selection) {
        validatePositiveOptionalValues(selection);
        var process = findProcess(selection);
        validateVolumeUnit(selection, process);
        var resolvedRate = resolveRateData(process, selection);
        var operations = validateAndBuildOperations(process, selection);
        return buildProcessData(selection, process, resolvedRate.rate(), resolvedRate.rateSelectionUsed(), operations);
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

    private ResolvedRateData resolveRateData(Process process, ProcessSelectionRequest selection) {
        return switch (selection.rateSelectionMode()) {
            case OPTION -> resolveOptionRate(process, selection.selectedRateOptionKey());
            case OVERRIDE -> resolveOverrideRate(selection.rateOverride());
        };
    }

    private ResolvedRateData resolveOptionRate(Process process, String selectedRateOptionKey) {
        var rateOption = findRateOption(process, selectedRateOptionKey);
        return new ResolvedRateData(
                rateOption.rate(),
                RateSelectionUsed.option(
                        rateOption.key(),
                        rateOption.title(),
                        rateOption.rate().units(),
                        rateOption.rate().minutes()));
    }

    private ResolvedRateData resolveOverrideRate(RateOverrideRequest override) {
        return new ResolvedRateData(
                new Rate(override.units(), override.minutes()),
                RateSelectionUsed.override(override.units(), override.minutes(), override.reason()));
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
            ProcessSelectionRequest selection,
            Process process,
            Rate rate,
            RateSelectionUsed rateSelectionUsed,
            List<OperationData> operations) {
        int setupMinutes =
                operations.stream().mapToInt(OperationData::durationMinutes).sum();
        int appliedBatchVolume = determineBatchVolume(selection, process);
        int batchCount = (int) Math.ceil((double) selection.volume() / appliedBatchVolume);
        int lastBatchVolume = calculateLastBatchVolume(selection.volume(), appliedBatchVolume);
        var productionBatchMinutes = buildProductionBatchDurations(selection.volume(), appliedBatchVolume, rate);
        int productionMinutes = calculateProductionMinutes(productionBatchMinutes);

        return new ProcessData(
                selection.processKey(),
                process.version(),
                batchCount,
                appliedBatchVolume,
                lastBatchVolume,
                setupMinutes,
                productionMinutes,
                rateSelectionUsed,
                productionBatchMinutes,
                operations);
    }

    private int calculateLastBatchVolume(int totalVolume, int batchVolume) {
        int remainder = totalVolume % batchVolume;
        if (remainder == 0) {
            return batchVolume;
        }
        return remainder;
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

    private int calculateRateMinutes(int batchVolume, Rate rate) {
        int batches = (int) Math.ceil((double) batchVolume / rate.units());
        return batches * rate.minutes();
    }

    private List<Integer> buildProductionBatchDurations(int totalVolume, int batchVolume, Rate rate) {
        var durations = new ArrayList<Integer>();
        int processed = 0;
        while (processed < totalVolume) {
            int currentBatchVolume = Math.min(batchVolume, totalVolume - processed);
            int rateMinutes = calculateRateMinutes(currentBatchVolume, rate);
            durations.add(rateMinutes);
            processed += currentBatchVolume;
        }
        return durations;
    }

    private int calculateProductionMinutes(List<Integer> productionBatchMinutes) {
        return productionBatchMinutes.stream().mapToInt(Integer::intValue).sum();
    }

    private int durationForSchedule(ProcessData data, int batchNumber) {
        int productionMinutes = data.productionBatchMinutes().get(batchNumber - 1);
        if (batchNumber == 1) {
            return data.setupMinutes() + productionMinutes;
        }
        return productionMinutes;
    }

    private void createInitialBatches(SimulationContext context, ItemData itemData) {
        var firstProcess = itemData.processes().getFirst();
        for (int batchNum = 1; batchNum <= firstProcess.batchCount(); batchNum++) {
            var batch = new PendingBatch(
                    itemData.itemOrder(),
                    itemData.externalId(),
                    batchNum,
                    0,
                    firstProcess.processKey(),
                    durationForSchedule(firstProcess, batchNum),
                    0);
            enqueueBatch(context, batch);
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
        for (var processKey :
                context.getProcessQueues().keySet().stream().sorted().toList()) {
            tryStartNextBatch(context, processKey);
        }
    }

    private void processCompletionEvent(SimulationContext context, CompletionEvent event) {
        context.getProcessAvailableAt().merge(event.processKey(), event.endMinute(), Math::max);
        moveBatchToNextProcess(context, event);
        tryStartNextBatch(context, event.processKey());
    }

    private void moveBatchToNextProcess(SimulationContext context, CompletionEvent event) {
        var itemData = context.getItemsByOrder().get(event.itemOrder());
        int nextProcessIndex = event.processIndex() + 1;
        if (nextProcessIndex >= itemData.processes().size()) {
            return;
        }

        var nextProcess = itemData.processes().get(nextProcessIndex);
        var nextBatch = new PendingBatch(
                event.itemOrder(),
                event.itemExternalId(),
                event.batchNumber(),
                nextProcessIndex,
                nextProcess.processKey(),
                durationForSchedule(nextProcess, event.batchNumber()),
                event.endMinute());
        enqueueBatch(context, nextBatch);
        tryStartNextBatch(context, nextProcess.processKey());
    }

    private void ensureProcessStructures(SimulationContext context, String processKey) {
        context.getProcessQueues().putIfAbsent(processKey, new LinkedList<>());
        context.getScheduledBatches().putIfAbsent(processKey, new ArrayList<>());
        context.getProcessAvailableAt().putIfAbsent(processKey, 0);
    }

    private void enqueueBatch(SimulationContext context, PendingBatch batch) {
        ensureProcessStructures(context, batch.processKey());
        context.getProcessQueues().get(batch.processKey()).add(batch);
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
        context.getProcessAvailableAt().put(processKey, endAt);
        context.getScheduledBatches()
                .get(processKey)
                .add(new ScheduledBatch(
                        batch.itemExternalId(), batch.batchNumber(), startAt, endAt, batch.durationMinutes()));
        context.getEvents()
                .add(new CompletionEvent(
                        endAt,
                        batch.itemOrder(),
                        batch.itemExternalId(),
                        batch.batchNumber(),
                        batch.processIndex(),
                        processKey));
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
        int estimatedMinutes = data.setupMinutes() + data.productionMinutes();
        return new ProcessEstimate(
                data.processKey(),
                data.version(),
                estimatedMinutes,
                data.batchCount(),
                data.appliedBatchVolume(),
                data.lastBatchVolume(),
                data.setupMinutes(),
                data.productionMinutes(),
                data.rateSelectionUsed(),
                operations);
    }

    private List<QueueEstimate> buildQueueEstimates(SimulationContext context) {
        var queues = new ArrayList<QueueEstimate>();
        for (var processKey :
                context.getScheduledBatches().keySet().stream().sorted().toList()) {
            var batches = context.getScheduledBatches().get(processKey).stream()
                    .sorted(Comparator.comparingInt(ScheduledBatch::startMinute))
                    .toList();
            int totalMinutes =
                    batches.stream().mapToInt(ScheduledBatch::endMinute).max().orElse(0);
            queues.add(new QueueEstimate(processKey, totalMinutes, batches));
        }
        return queues;
    }

    // Internal data structures for simulation
    @lombok.Getter
    private static class SimulationContext {

        private final String orderId;

        private final List<ItemData> items = new ArrayList<>();

        private final Map<Integer, ItemData> itemsByOrder = new LinkedHashMap<>();

        private final Map<String, Queue<PendingBatch>> processQueues = new HashMap<>();

        private final Map<String, Integer> processAvailableAt = new HashMap<>();

        private final Map<String, List<ScheduledBatch>> scheduledBatches = new HashMap<>();

        private final PriorityQueue<CompletionEvent> events = new PriorityQueue<>(COMPLETION_EVENT_ORDER);

        SimulationContext(String orderId) {
            this.orderId = orderId;
        }
    }

    private record ItemData(int itemOrder, String externalId, List<ProcessData> processes) {}

    private record ProcessData(
            String processKey,
            int version,
            int batchCount,
            int appliedBatchVolume,
            int lastBatchVolume,
            int setupMinutes,
            int productionMinutes,
            RateSelectionUsed rateSelectionUsed,
            List<Integer> productionBatchMinutes,
            List<OperationData> operations) {}

    private record ResolvedRateData(Rate rate, RateSelectionUsed rateSelectionUsed) {}

    private record OperationData(String operationName, String timeOptionKey, int durationMinutes) {}

    private record PendingBatch(
            int itemOrder,
            String itemExternalId,
            int batchNumber,
            int processIndex,
            String processKey,
            int durationMinutes,
            int readyAt) {}

    private record CompletionEvent(
            int endMinute,
            int itemOrder,
            String itemExternalId,
            int batchNumber,
            int processIndex,
            String processKey) {}
}
