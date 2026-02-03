package io.github.avstarodub.mosd.scheduler.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.github.avstarodub.mosd.scheduler.api.dto.EstimateRequest;
import io.github.avstarodub.mosd.scheduler.api.dto.EstimateResponse;
import io.github.avstarodub.mosd.scheduler.api.dto.ItemRequest;
import io.github.avstarodub.mosd.scheduler.api.dto.OperationSelectionRequest;
import io.github.avstarodub.mosd.scheduler.api.dto.ProcessSelectionRequest;
import io.github.avstarodub.mosd.scheduler.api.dto.QueueEstimate;
import io.github.avstarodub.mosd.scheduler.domain.port.ProcessProvider;
import io.github.avstarodub.mosd.space.domain.HandoffMode;
import io.github.avstarodub.mosd.space.domain.Operation;
import io.github.avstarodub.mosd.space.domain.Process;
import io.github.avstarodub.mosd.space.domain.Rate;
import io.github.avstarodub.mosd.space.domain.RateOption;
import io.github.avstarodub.mosd.space.domain.TimeOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests: Scheduler EstimateService")
class EstimateServiceTest {

    private static final String ORDER_ID = "ORD-1";

    private static final String ORDER_ID_TIE = "ORD-TIE";

    private static final String ITEM_1 = "ITEM-1";

    private static final String ITEM_1_SHORT = "Item1";

    private static final String ITEM_2_SHORT = "Item2";

    private static final String PROCESS_LASER = "LASER-1";

    private static final String PROCESS_DRILL = "DRILL-1";

    private static final String PROCESS_CUT = "CUT-1";

    private static final String PROCESS_A = "A-1";

    private static final String PROCESS_B = "B-1";

    private static final String PROCESS_C = "C-1";

    private static final String OP_LASER = "laser-op";

    private static final String OP_DRILL = "drill-op";

    private static final String OP_CUT = "cut-op";

    private static final String OP_A = "a-op";

    private static final String OP_B = "b-op";

    private static final String OP_C = "c-op";

    private static final String TIME_OPTION_LASER = "LASER";

    private static final String TIME_OPTION_DRILL = "DRILL";

    private static final String TIME_OPTION_CUT = "CUT";

    private static final String TIME_OPTION_A = "A";

    private static final String TIME_OPTION_B = "B";

    private static final String TIME_OPTION_C = "C";

    private static final String STANDARD_RATE_OPTION = "STD";

    private static final String STANDARD_RATE_OPTION_TITLE = "Standard";

    private static final String VOLUME_UNIT_PCS = "pcs";

    private static final String PROCESS_TYPE = "TYPE";

    private static final int DETERMINISTIC_RUNS = 10;

    @Mock
    private ProcessProvider processProvider;

    @Nested
    @DisplayName("Success scenarios")
    class SuccessScenarios {

        @Test
        @DisplayName("Calculates conveyor timeline for two items and two processes")
        void conveyorForTwoItemsTwoProcesses() {
            stubProcess(
                    PROCESS_LASER,
                    process(new ProcessSpec(PROCESS_LASER, 1, 10, OP_LASER, TIME_OPTION_LASER, 4, 10, 6)));
            stubProcess(
                    PROCESS_DRILL,
                    process(new ProcessSpec(PROCESS_DRILL, 1, 10, OP_DRILL, TIME_OPTION_DRILL, 5, 10, 10)));
            var service = createService();

            var actualResponse = service.estimate(requestForTwoItemsTwoProcesses());

            var actualTotalMinutes = actualResponse.totalMinutes();
            var expectedTotalMinutes = 70;
            assertThat(actualTotalMinutes).isEqualTo(expectedTotalMinutes);

            var actualLaserTimeline = timeline(actualResponse, PROCESS_LASER);
            var expectedLaserTimeline = List.of(tuple(0, 10), tuple(10, 20), tuple(20, 30), tuple(30, 40));
            assertThat(actualLaserTimeline).containsExactlyElementsOf(expectedLaserTimeline);

            var actualDrillTimeline = timeline(actualResponse, PROCESS_DRILL);
            var expectedDrillTimeline = List.of(tuple(10, 25), tuple(25, 40), tuple(40, 55), tuple(55, 70));
            assertThat(actualDrillTimeline).containsExactlyElementsOf(expectedDrillTimeline);
        }

        @Test
        @DisplayName("Uses per-batch duration for remainder batch")
        void perBatchDurationForRemainder() {
            stubProcess(PROCESS_CUT, process(new ProcessSpec(PROCESS_CUT, 1, 10, OP_CUT, TIME_OPTION_CUT, 2, 6, 5)));
            var service = createService();
            var request = requestSingleProcess(
                    ORDER_ID, ITEM_1, selection(PROCESS_CUT, 25, null, OP_CUT, TIME_OPTION_CUT));

            var actualResponse = service.estimate(request);

            var actualTotalMinutes = actualResponse.totalMinutes();
            var expectedTotalMinutes = 31;
            assertThat(actualTotalMinutes).isEqualTo(expectedTotalMinutes);

            var actualBatchDurations = durations(actualResponse, PROCESS_CUT);
            var expectedBatchDurations = List.of(12, 12, 7);
            assertThat(actualBatchDurations).containsExactlyElementsOf(expectedBatchDurations);
        }

        @Test
        @DisplayName("Applies batch override when provided")
        void appliesBatchOverride() {
            stubProcess(PROCESS_CUT, process(new ProcessSpec(PROCESS_CUT, 1, 10, OP_CUT, TIME_OPTION_CUT, 2, 10, 5)));
            var service = createService();
            var request = requestSingleProcess(
                    ORDER_ID, ITEM_1, selection(PROCESS_CUT, 20, 25, OP_CUT, TIME_OPTION_CUT));

            var actualResponse = service.estimate(request);
            var actualNumberOfBatches =
                    actualResponse.items().getFirst().processes().getFirst().numberOfBatches();
            var expectedNumberOfBatches = 1;

            assertThat(actualNumberOfBatches).isEqualTo(expectedNumberOfBatches);
        }

        @Test
        @DisplayName("Produces deterministic result for same input")
        void deterministicResult() {
            stubProcess(
                    PROCESS_LASER,
                    process(new ProcessSpec(PROCESS_LASER, 1, 10, OP_LASER, TIME_OPTION_LASER, 4, 10, 6)));
            stubProcess(
                    PROCESS_DRILL,
                    process(new ProcessSpec(PROCESS_DRILL, 1, 10, OP_DRILL, TIME_OPTION_DRILL, 5, 10, 10)));
            var service = createService();
            var request = requestForTwoItemsTwoProcesses();

            var expectedResponse = service.estimate(request);

            var actualResponses = IntStream.range(0, DETERMINISTIC_RUNS)
                    .mapToObj(index -> service.estimate(request))
                    .toList();
            var expectedResponses = IntStream.range(0, DETERMINISTIC_RUNS)
                    .mapToObj(index -> expectedResponse)
                    .toList();

            assertThat(actualResponses).containsExactlyElementsOf(expectedResponses);
        }

        @Test
        @DisplayName("Tie on completion uses input item order")
        void tieResolvesByInputItemOrder() {
            stubProcess(PROCESS_A, process(new ProcessSpec(PROCESS_A, 1, 10, OP_A, TIME_OPTION_A, 4, 10, 6)));
            stubProcess(PROCESS_B, process(new ProcessSpec(PROCESS_B, 1, 10, OP_B, TIME_OPTION_B, 5, 10, 5)));
            stubProcess(PROCESS_C, process(new ProcessSpec(PROCESS_C, 1, 10, OP_C, TIME_OPTION_C, 2, 10, 3)));
            var service = createService();

            var item1 = new ItemRequest(
                    ITEM_1_SHORT,
                    List.of(
                            selection(PROCESS_A, 10, null, OP_A, TIME_OPTION_A),
                            selection(PROCESS_C, 10, null, OP_C, TIME_OPTION_C)));
            var item2 = new ItemRequest(
                    ITEM_2_SHORT,
                    List.of(
                            selection(PROCESS_B, 10, null, OP_B, TIME_OPTION_B),
                            selection(PROCESS_C, 10, null, OP_C, TIME_OPTION_C)));
            var request = new EstimateRequest(ORDER_ID_TIE, List.of(item1, item2));

            var actualResponse = service.estimate(request);

            var actualTimeline = timelineWithItem(actualResponse, PROCESS_C);
            var expectedTimeline = List.of(Tuple.tuple(ITEM_1_SHORT, 10, 15), Tuple.tuple(ITEM_2_SHORT, 15, 20));

            assertThat(actualTimeline).containsExactlyElementsOf(expectedTimeline);
        }
    }

    @Nested
    @DisplayName("Validation scenarios")
    class ValidationScenarios {

        @Test
        @DisplayName("Rejects non-positive process version")
        void rejectNonPositiveVersion() {
            var service = createService();
            var request = requestWithInvalidVersion();
            var expectedMessagePart = "Process version must be > 0";

            assertThatThrownBy(() -> service.estimate(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(expectedMessagePart);
        }

        @Test
        @DisplayName("Rejects non-positive batch override")
        void rejectNonPositiveBatchOverride() {
            var service = createService();
            var request = requestSingleProcess(
                    ORDER_ID, ITEM_1, selection(PROCESS_CUT, 20, 0, OP_CUT, TIME_OPTION_CUT));
            var expectedMessagePart = "Batch volume override must be > 0";

            assertThatThrownBy(() -> service.estimate(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(expectedMessagePart);
        }
    }

    private EstimateService createService() {
        return new EstimateService(processProvider);
    }

    private void stubProcess(String processKey, Process process) {
        when(processProvider.findByKey(processKey)).thenReturn(Optional.of(process));
    }

    private Map<String, QueueEstimate> toQueueMap(List<QueueEstimate> queues) {
        return queues.stream().collect(Collectors.toMap(QueueEstimate::processKey, queue -> queue));
    }

    private List<Tuple> timeline(EstimateResponse response, String processKey) {
        return toQueueMap(response.queues()).get(processKey).batches().stream()
                .map(batch -> tuple(batch.startMinute(), batch.endMinute()))
                .toList();
    }

    private List<Integer> durations(EstimateResponse response, String processKey) {
        return toQueueMap(response.queues()).get(processKey).batches().stream()
                .map(batch -> batch.durationMinutes())
                .toList();
    }

    private List<Tuple> timelineWithItem(EstimateResponse response, String processKey) {
        return toQueueMap(response.queues()).get(processKey).batches().stream()
                .map(batch -> Tuple.tuple(batch.itemExternalId(), batch.startMinute(), batch.endMinute()))
                .toList();
    }

    private EstimateRequest requestForTwoItemsTwoProcesses() {
        var item1 = new ItemRequest(
                ITEM_1_SHORT,
                List.of(
                        selection(PROCESS_LASER, 20, null, OP_LASER, TIME_OPTION_LASER),
                        selection(PROCESS_DRILL, 20, null, OP_DRILL, TIME_OPTION_DRILL)));
        var item2 = new ItemRequest(
                ITEM_2_SHORT,
                List.of(
                        selection(PROCESS_LASER, 20, null, OP_LASER, TIME_OPTION_LASER),
                        selection(PROCESS_DRILL, 20, null, OP_DRILL, TIME_OPTION_DRILL)));
        return new EstimateRequest(ORDER_ID, List.of(item1, item2));
    }

    private EstimateRequest requestSingleProcess(String orderId, String itemId, ProcessSelectionRequest selection) {
        var item = new ItemRequest(itemId, List.of(selection));
        return new EstimateRequest(orderId, List.of(item));
    }

    private EstimateRequest requestWithInvalidVersion() {
        var process = new ProcessSelectionRequest(
                PROCESS_CUT,
                0,
                20,
                VOLUME_UNIT_PCS,
                null,
                STANDARD_RATE_OPTION,
                List.of(new OperationSelectionRequest(OP_CUT, TIME_OPTION_CUT)));
        return new EstimateRequest(ORDER_ID, List.of(new ItemRequest(ITEM_1, List.of(process))));
    }

    private ProcessSelectionRequest selection(
            String processKey, int volume, Integer batchOverride, String operationName, String timeOptionKey) {
        return new ProcessSelectionRequest(
                processKey,
                null,
                volume,
                VOLUME_UNIT_PCS,
                batchOverride,
                STANDARD_RATE_OPTION,
                List.of(new OperationSelectionRequest(operationName, timeOptionKey)));
    }

    private Process process(ProcessSpec spec) {
        var operation = new Operation(
                null,
                spec.operationName(),
                1,
                List.of(new TimeOption(
                        spec.timeOptionKey(), spec.timeOptionKey() + " option", spec.operationMinutes())));
        return new Process(
                null,
                spec.processKey(),
                spec.version(),
                PROCESS_TYPE,
                null,
                VOLUME_UNIT_PCS,
                spec.batchVolume(),
                HandoffMode.BATCHED,
                List.of(operation),
                List.of(new RateOption(
                        STANDARD_RATE_OPTION,
                        STANDARD_RATE_OPTION_TITLE,
                        new Rate(spec.rateUnits(), spec.rateMinutes()))),
                null,
                null);
    }

    private record ProcessSpec(
            String processKey,
            int version,
            int batchVolume,
            String operationName,
            String timeOptionKey,
            int operationMinutes,
            int rateUnits,
            int rateMinutes) {}

    private static Tuple tuple(int startMinute, int endMinute) {
        return Tuple.tuple(startMinute, endMinute);
    }
}
