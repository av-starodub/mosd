package io.github.avstarodub.mosd.space.testdata;

import static org.instancio.Select.field;

import io.github.avstarodub.mosd.space.domain.HandoffMode;
import io.github.avstarodub.mosd.space.domain.Operation;
import io.github.avstarodub.mosd.space.domain.Process;
import io.github.avstarodub.mosd.space.domain.ProcessType;
import io.github.avstarodub.mosd.space.domain.Rate;
import io.github.avstarodub.mosd.space.domain.RateOption;
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

    private Model<Operation> operationModel;

    private Model<ProcessType> processTypeModel;

    private Model<Process> processModel;

    @PostConstruct
    private void init() {
        rateModel = buildRateModel();
        rateOptionModel = buildRateOptionModel();
        operationModel = buildOperationModel();
        processTypeModel = buildProcessTypeModel();
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

    private Model<Operation> buildOperationModel() {
        return Instancio.of(Operation.class)
                .set(field(Operation::name), "op-1")
                .set(field(Operation::executionTime), 5L)
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

    private Model<Process> buildProcessModel() {
        var operations = List.of(new Operation("op-1", 5L), new Operation("op-2", 10L));
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
                .set(field(Process::handoffMode), HandoffMode.BATCHED)
                .set(field(Process::operations), operations)
                .set(field(Process::rateOptions), rateOptions)
                .toModel();
    }
}
