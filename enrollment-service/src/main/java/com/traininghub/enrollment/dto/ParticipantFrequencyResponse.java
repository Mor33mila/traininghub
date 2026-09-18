package com.traininghub.enrollment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ParticipantFrequencyResponse(UUID participantId, BigDecimal attendedHours,
                                           BigDecimal absentHours, BigDecimal totalHours, BigDecimal percentage,
                                           boolean belowMinimumThreshold) { }