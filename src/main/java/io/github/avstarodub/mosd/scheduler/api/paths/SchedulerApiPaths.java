package io.github.avstarodub.mosd.scheduler.api.paths;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class SchedulerApiPaths {

    public static final String BASE = "/api/v1/scheduler";

    public static final String ESTIMATE_RELATIVE = "/estimate";

    public static final String ESTIMATE = BASE + ESTIMATE_RELATIVE;
}
