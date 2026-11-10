package com.bitlab.main_service.controller;

import com.bitlab.main_service.dto.CourseDto;
import com.bitlab.main_service.exception.ResourceNotFoundException;
import com.bitlab.main_service.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller slice test: only the web layer is started, the service is a Mockito mock.
 */
@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;

    private CourseDto course(Long id, String name) {
        return CourseDto.builder().id(id).name(name).description("Description of " + name).build();
    }

    @Test
    @DisplayName("GET /api/courses returns 200 and the list of courses")
    void getAll_returns200AndList() throws Exception {
        when(courseService.getAll()).thenReturn(List.of(course(1L, "Java Developer"), course(2L, "Python Developer")));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Java Developer"))
                .andExpect(jsonPath("$[1].name").value("Python Developer"));
    }

    @Test
    @DisplayName("GET /api/courses/{id} returns 200 and the course")
    void getById_existing_returns200() throws Exception {
        when(courseService.getById(1L)).thenReturn(course(1L, "Java Developer"));

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Java Developer"));
    }

    @Test
    @DisplayName("GET /api/courses/{id} returns 404 in the common error format when the course does not exist")
    void getById_missing_returns404() throws Exception {
        when(courseService.getById(99L)).thenThrow(new ResourceNotFoundException("Course", 99L));

        mockMvc.perform(get("/api/courses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Course with id 99 not found"))
                .andExpect(jsonPath("$.path").value("/api/courses/99"));
    }

    @Test
    @DisplayName("GET /api/courses/{id} returns 400 when the id is not a number")
    void getById_invalidId_returns400() throws Exception {
        mockMvc.perform(get("/api/courses/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/courses returns 201 and the created course")
    void create_validBody_returns201() throws Exception {
        when(courseService.create(any(CourseDto.class))).thenReturn(course(1L, "Java Developer"));
        CourseDto request = CourseDto.builder().name("Java Developer").description("Java course").build();

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Java Developer"));
    }

    @Test
    @DisplayName("POST /api/courses returns 400 with validation errors when the name is blank")
    void create_blankName_returns400() throws Exception {
        CourseDto request = CourseDto.builder().name("").build();

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.name").value("name must not be blank"));

        verify(courseService, never()).create(any(CourseDto.class));
    }

    @Test
    @DisplayName("POST /api/courses returns 400 when the JSON is malformed")
    void create_malformedJson_returns400() throws Exception {
        // Intentionally invalid JSON. It is kept in a variable so that the IDE does not
        // treat the string as a JSON document and underline it.
        String malformedJson = "{ this is not json";

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/courses/{id} returns 200 and the updated course")
    void update_existing_returns200() throws Exception {
        when(courseService.update(eq(1L), any(CourseDto.class))).thenReturn(course(1L, "Java Developer"));
        CourseDto request = CourseDto.builder().name("Java Developer").description("New description").build();

        mockMvc.perform(put("/api/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Java Developer"));
    }

    @Test
    @DisplayName("PUT /api/courses/{id} returns 404 when the course does not exist")
    void update_missing_returns404() throws Exception {
        when(courseService.update(eq(99L), any(CourseDto.class))).thenThrow(new ResourceNotFoundException("Course", 99L));
        CourseDto request = CourseDto.builder().name("Java Developer").build();

        mockMvc.perform(put("/api/courses/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course with id 99 not found"));
    }

    @Test
    @DisplayName("PUT /api/courses/{id} returns 400 when the name is blank")
    void update_blankName_returns400() throws Exception {
        CourseDto request = CourseDto.builder().name(" ").build();

        mockMvc.perform(put("/api/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists());

        verify(courseService, never()).update(eq(1L), any(CourseDto.class));
    }

    @Test
    @DisplayName("DELETE /api/courses/{id} returns 204")
    void delete_existing_returns204() throws Exception {
        mockMvc.perform(delete("/api/courses/1"))
                .andExpect(status().isNoContent());

        verify(courseService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/courses/{id} returns 404 when the course does not exist")
    void delete_missing_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Course", 99L)).when(courseService).delete(99L);

        mockMvc.perform(delete("/api/courses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}