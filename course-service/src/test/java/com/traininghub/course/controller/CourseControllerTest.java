package com.traininghub.course.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traininghub.course.dto.CourseRequest;
import com.traininghub.course.security.ApiSecurityConfiguration;
import com.traininghub.course.security.JwtSecurityBeansConfiguration;
import com.traininghub.course.entity.CourseMode;
import com.traininghub.course.entity.CourseStatus;
import com.traininghub.course.mapper.CourseMapper;
import com.traininghub.course.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
@Import({CourseMapper.class, com.traininghub.course.exception.CourseExceptionHandler.class,
    ApiSecurityConfiguration.class, JwtSecurityBeansConfiguration.class})
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;

    @Test
    void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void returnsCoursesByStatus() throws Exception {
        when(courseService.findByStatus(eq(CourseStatus.SCHEDULED), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/courses/status/SCHEDULED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void rejectsInvalidRequest() throws Exception {
        CourseRequest request = new CourseRequest();

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.courseCode").exists())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void forbidsTeacherFromCreatingCourse() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
    }

    private CourseRequest validRequest() {
        CourseRequest request = new CourseRequest();
        request.setCourseCode("JAVA-101");
        request.setTitle("Java Fundamentals");
        request.setTotalHours(BigDecimal.TEN);
        request.setStartDate(LocalDate.of(2026, 10, 1));
        request.setEndDate(LocalDate.of(2026, 10, 2));
        request.setMaximumCapacity(10);
        request.setMode(CourseMode.PRESENCE);
        request.setStatus(CourseStatus.SCHEDULED);
        return request;
    }
}