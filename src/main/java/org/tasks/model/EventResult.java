package org.tasks.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

public enum EventResult {
    SUCCESS, ERROR;

    @Getter
    @Setter
    private String message;


    @Override
    public String toString() {
        return name() + (StringUtils.isNotEmpty(message) ? ": " + message : "");
    }
}
