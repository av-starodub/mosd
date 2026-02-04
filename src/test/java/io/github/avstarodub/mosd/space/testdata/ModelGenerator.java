package io.github.avstarodub.mosd.space.testdata;

import static org.instancio.Select.field;

import io.github.avstarodub.mosd.space.domain.Operation;
import io.github.avstarodub.mosd.space.domain.OperationType;
import io.github.avstarodub.mosd.space.domain.Process;
import io.github.avstarodub.mosd.space.domain.ProcessType;
import io.github.avstarodub.mosd.space.domain.Rate;
import io.github.avstarodub.mosd.space.domain.RateOption;
import io.github.avstarodub.mosd.space.domain.TimeOption;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.Getter;
import org.instancio.Instancio;
import org.instancio.Model;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ModelGenerator {

    private Model<Rate> rateModel;

    private Model<RateOption> rateOptionModel;

    private Model<TimeOption> timeOptionModel;

    private Model<Operation> operationModel;

    private Model<ProcessType> processTypeModel;

    private Model<OperationType> operationTypeModel;

    private Model<Process> processModel;

    public Process process() {
        return Instancio.create(processModel);
    }

    public ProcessType processType() {
        return Instancio.create(processTypeModel);
    }

    public OperationType operationType() {
        return Instancio.create(operationTypeModel);
    }

    @PostConstruct
    private void init() {
        rateModel = buildRateModel();
        rateOptionModel = buildRateOptionModel();
        timeOptionModel = buildTimeOptionModel();
        operationModel = buildOperationModel();
        processTypeModel = buildProcessTypeModel();
        operationTypeModel = buildOperationTypeModel();
        processModel = buildProcessModel();
    }

    private Model<Rate> buildRateModel() {
        return Instancio.of(Rate.class)
                .set(field(Rate::units), 3)
                .set(field(Rate::minutes), 2)
                .toModel();
    }

    private Model<RateOption> buildRateOptionModel() {
        var rate = new Rate(3, 2);
        return Instancio.of(RateOption.class)
                .set(field(RateOption::key), "STD")
                .set(field(RateOption::title), "Standard")
                .set(field(RateOption::rate), rate)
                .toModel();
    }

    private Model<TimeOption> buildTimeOptionModel() {
        return Instancio.of(TimeOption.class)
                .set(field(TimeOption::key), "FAST")
                .set(field(TimeOption::title), "Fast Mode")
                .set(field(TimeOption::duration), 5)
                .toModel();
    }

    private Model<Operation> buildOperationModel() {
        var timeOptions = List.of(new TimeOption("FAST", "Fast Mode", 5));
        return Instancio.of(Operation.class)
                .set(field(Operation::operationType), null)
                .set(field(Operation::name), "op-1")
                .set(field(Operation::quantity), 1)
                .set(field(Operation::timeOptions), timeOptions)
                .toModel();
    }

    private Model<ProcessType> buildProcessTypeModel() {
        return Instancio.of(ProcessType.class)
                .ignore(field(ProcessType::id))
                .ignore(field(ProcessType::createdAt))
                .ignore(field(ProcessType::updatedAt))
                .set(field(ProcessType::type), "CUT")
                .set(field(ProcessType::title), "Cutting")
                .toModel();
    }

    private Model<OperationType> buildOperationTypeModel() {
        var defaultTimeOptions =
                List.of(new TimeOption("FAST", "Fast Mode", 5), new TimeOption("STD", "Standard Mode", 10));
        return Instancio.of(OperationType.class)
                .ignore(field(OperationType::id))
                .ignore(field(OperationType::createdAt))
                .ignore(field(OperationType::updatedAt))
                .set(field(OperationType::type), "OP-A")
                .set(field(OperationType::title), "Operation Type A")
                .set(field(OperationType::processType), "CUT")
                .set(field(OperationType::defaultTimeOptions), defaultTimeOptions)
                .toModel();
    }

    private Model<Process> buildProcessModel() {
        var timeOptions1 = List.of(new TimeOption("FAST", "Fast Mode", 5));
        var timeOptions2 = List.of(new TimeOption("STD", "Standard Mode", 10));
        var operations =
                List.of(new Operation(null, "op-1", 1, timeOptions1), new Operation(null, "op-2", 1, timeOptions2));
        var rateOptions = List.of(new RateOption("STD", "Standard", new Rate(3, 2)));
        return Instancio.of(Process.class)
                .ignore(field(Process::id))
                .ignore(field(Process::createdAt))
                .ignore(field(Process::updatedAt))
                .set(field(Process::processKey), "PROC-1")
                .set(field(Process::version), 1)
                .set(field(Process::type), "CUT")
                .set(field(Process::description), "Primary cutting")
                .set(field(Process::volumeUnit), "pcs")
                .set(field(Process::batchVolume), 50)
                .set(field(Process::operations), operations)
                .set(field(Process::rateOptions), rateOptions)
                .toModel();
    }
}
