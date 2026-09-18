package com.traininghub.course.dto;

import com.traininghub.course.entity.CourseMode;
import com.traininghub.course.entity.CourseStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CourseRequest {
    @NotBlank @Size(max = 50)
    private String courseCode;
    @NotBlank @Size(max = 200)
    private String title;
    private String description;
    @Size(max = 120)
    private String trainingArea;
    @NotNull @Positive @DecimalMin(value = "0.01")
    private BigDecimal totalHours;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @NotNull @Positive
    private Integer maximumCapacity;
    @NotNull
    private CourseMode mode;
    @NotNull
    private CourseStatus status;
    private UUID instructorId;

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTrainingArea() { return trainingArea; }
    public void setTrainingArea(String trainingArea) { this.trainingArea = trainingArea; }
    public BigDecimal getTotalHours() { return totalHours; }
    public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getMaximumCapacity() { return maximumCapacity; }
    public void setMaximumCapacity(Integer maximumCapacity) { this.maximumCapacity = maximumCapacity; }
    public CourseMode getMode() { return mode; }
    public void setMode(CourseMode mode) { this.mode = mode; }
    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }
    public UUID getInstructorId() { return instructorId; }
    public void setInstructorId(UUID instructorId) { this.instructorId = instructorId; }
}