package io.github.avstarodub.mosd.exception;

import lombok.Getter;

@Getter
public class ResourceInUseException extends RuntimeException {

    private final String resourceName;

    private final String fieldName;

    private final String fieldValue;

    public ResourceInUseException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s is in use with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = String.valueOf(fieldValue);
    }
}
