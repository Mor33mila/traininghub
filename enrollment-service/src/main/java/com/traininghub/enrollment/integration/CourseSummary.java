package com.traininghub.enrollment.integration;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CourseSummary(UUID id, Integer maximumCapacity, BigDecimal totalHours,
							LocalDate startDate, LocalDate endDate, UUID instructorId) {
	public CourseSummary(UUID id, Integer maximumCapacity) {
		this(id, maximumCapacity, null, null, null, null);
	}

	public CourseSummary(UUID id, Integer maximumCapacity, BigDecimal totalHours,
						 LocalDate startDate, LocalDate endDate) {
		this(id, maximumCapacity, totalHours, startDate, endDate, null);
	}
}