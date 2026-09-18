package com.traininghub.course.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiError(OffsetDateTime timestamp, int status, String message, String path,
                       Map<String, String> fieldErrors) {
}