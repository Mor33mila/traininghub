package com.traininghub.enrollment.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiError(OffsetDateTime timestamp, int status, String message, String path,
                       Map<String, String> fieldErrors) { }