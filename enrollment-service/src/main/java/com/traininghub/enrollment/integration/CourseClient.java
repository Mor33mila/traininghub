package com.traininghub.enrollment.integration;

import java.util.UUID;

public interface CourseClient {
    CourseSummary findById(UUID courseId);
}