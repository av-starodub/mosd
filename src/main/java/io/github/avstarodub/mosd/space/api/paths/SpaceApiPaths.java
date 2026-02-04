package io.github.avstarodub.mosd.space.api.paths;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class SpaceApiPaths {

    private static final String SPACE_BASE = "/api/v1/space";

    public static final class Processes {

        public static final String BASE = SPACE_BASE + "/processes";

        public static final String BY_KEY = "/{processKey}";

        public static final String VERSIONS = "/{processKey}/versions";

        public static final String VERSION_BY_NUMBER = "/{processKey}/versions/{version}";

        public static final String TYPE_PARAM = "type";
    }

    public static final class ProcessTypes {

        public static final String BASE = SPACE_BASE + "/process-types";

        public static final String BY_TYPE = "/{type}";
    }

    public static final class OperationTypes {

        public static final String BASE = SPACE_BASE + "/operation-types";

        public static final String BY_TYPE = "/{type}";

        public static final String PROCESS_TYPE_PARAM = "processType";
    }
}
