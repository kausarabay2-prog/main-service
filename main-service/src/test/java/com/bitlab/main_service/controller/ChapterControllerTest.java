package com.bitlab.main_service.controller;

import com.bitlab.main_service.dto.ChapterDto;
import com.bitlab.main_service.exception.BadRequestException;
import com.bitlab.main_service.exception.ResourceNotFoundException;
import com.bitlab.main_service.service.ChapterService;
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
import static org.mockito.ArgumentMatchers.anyLong;
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

@WebMvcTest(ChapterController.class)
class ChapterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChapterService chapterService;

    private ChapterDto chapter(Long id, String name, int order) {
        return ChapterDto.builder().id(id).name(name).order(order).courseId(1L).build();
    }

    @Test
    @DisplayName("GET /api/chapters?courseId= returns 200 and the chapters of the course")
    void getAllByCourse_returns200AndList() throws Exception {
        when(chapterService.getAllByCourse(1L)).thenReturn(List.of(
                chapter(10L, "Variables and Data Types", 1),
                chapter(11L, "if-else conditions", 2)));

        mockMvc.perform(get("/api/chapters").param("courseId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Variables and Data Types"))
                .andExpect(jsonPath("$[0].order").value(1))
                .andExpect(jsonPath("$[1].courseId").value(1));
    }

    @Test
    @DisplayName("GET /api/chapters without courseId returns 400")
    void getAllByCourse_withoutCourseId_returns400() throws Exception {
        mockMvc.perform(get("/api/chapters"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(chapterService, never()).getAllByCourse(anyLong());
    }

    @Test
    @DisplayName("GET /api/chapters?courseId= returns 404 when the course does not exist")
    void getAllByCourse_missingCourse_returns404() throws Exception {
        when(chapterService.getAllByCourse(99L)).thenThrow(new ResourceNotFoundException("Course", 99L));

        mockMvc.perform(get("/api/chapters").param("courseId", "99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course with id 99 not found"));
    }

    @Test
    @DisplayName("GET /api/chapters/{id} returns 200 and the chapter")
    void getById_existing_returns200() throws Exception {
        when(chapterService.getById(10L)).thenReturn(chapter(10L, "if-else conditions", 2));

        mockMvc.perform(get("/api/chapters/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("if-else conditions"));
    }

    @Test
    @DisplayName("GET /api/chapters/{id} returns 404 when the chapter does not exist")
    void getById_missing_returns404() throws Exception {
        when(chapterService.getById(99L)).thenThrow(new ResourceNotFoundException("Chapter", 99L));

        mockMvc.perform(get("/api/chapters/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Chapter with id 99 not found"))
                .andExpect(jsonPath("$.path").value("/api/chapters/99"));
    }

    @Test
    @DisplayName("POST /api/chapters returns 201 and the created chapter")
    void create_validBody_returns201() throws Exception {
        when(chapterService.create(any(ChapterDto.class))).thenReturn(chapter(10L, "Loops", 3));
        ChapterDto request = ChapterDto.builder().name("Loops").order(3).courseId(1L).build();

        mockMvc.perform(post("/api/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Loops"))
                .andExpect(jsonPath("$.courseId").value(1));
    }

    @Test
    @DisplayName("POST /api/chapters returns 400 with validation errors for a blank name and a wrong order")
    void create_invalidBody_returns400() throws Exception {
        ChapterDto request = ChapterDto.builder().name("").order(0).courseId(1L).build();

        mockMvc.perform(post("/api/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.order").exists());

        verify(chapterService, never()).create(any(ChapterDto.class));
    }

    @Test
    @DisplayName("POST /api/chapters returns 400 when the service reports a bad request")
    void create_withoutCourseId_returns400() throws Exception {
        when(chapterService.create(any(ChapterDto.class))).thenThrow(new BadRequestException("courseId is required"));
        ChapterDto request = ChapterDto.builder().name("Loops").order(3).build();

        mockMvc.perform(post("/api/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("courseId is required"));
    }

    @Test
    @DisplayName("POST /api/chapters returns 404 when the course does not exist")
    void create_missingCourse_returns404() throws Exception {
        when(chapterService.create(any(ChapterDto.class))).thenThrow(new ResourceNotFoundException("Course", 99L));
        ChapterDto request = ChapterDto.builder().name("Loops").order(3).courseId(99L).build();

        mockMvc.perform(post("/api/chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/chapters/{id} returns 200 and the updated chapter")
    void update_existing_returns200() throws Exception {
        when(chapterService.update(eq(10L), any(ChapterDto.class))).thenReturn(chapter(10L, "New name", 2));
        ChapterDto request = ChapterDto.builder().name("New name").order(2).build();

        mockMvc.perform(put("/api/chapters/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"));
    }

    @Test
    @DisplayName("PUT /api/chapters/{id} returns 404 when the chapter does not exist")
    void update_missing_returns404() throws Exception {
        when(chapterService.update(eq(99L), any(ChapterDto.class))).thenThrow(new ResourceNotFoundException("Chapter", 99L));
        ChapterDto request = ChapterDto.builder().name("New name").order(2).build();

        mockMvc.perform(put("/api/chapters/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/chapters/{id} returns 204")
    void delete_existing_returns204() throws Exception {
        mockMvc.perform(delete("/api/chapters/10"))
                .andExpect(status().isNoContent());

        verify(chapterService).delete(10L);
    }

    @Test
    @DisplayName("DELETE /api/chapters/{id} returns 404 when the chapter does not exist")
    void delete_missing_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Chapter", 99L)).when(chapterService).delete(99L);

        mockMvc.perform(delete("/api/chapters/99"))
                .andExpect(status().isNotFound());
    }
}