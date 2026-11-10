package com.bitlab.main_service.controller;

import com.bitlab.main_service.dto.LessonDto;
import com.bitlab.main_service.exception.BadRequestException;
import com.bitlab.main_service.exception.ResourceNotFoundException;
import com.bitlab.main_service.service.LessonService;
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

@WebMvcTest(LessonController.class)
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LessonService lessonService;

    private LessonDto lesson(Long id, String name, int order) {
        return LessonDto.builder().id(id).name(name).content("Content of " + name).order(order).chapterId(5L).build();
    }

    @Test
    @DisplayName("GET /api/lessons?chapterId= returns 200 and the lessons of the chapter")
    void getAllByChapter_returns200AndList() throws Exception {
        when(lessonService.getAllByChapter(5L)).thenReturn(List.of(
                lesson(1L, "Lecture for if-else conditions", 1),
                lesson(2L, "Practice for if-else conditions", 2)));

        mockMvc.perform(get("/api/lessons").param("chapterId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Lecture for if-else conditions"))
                .andExpect(jsonPath("$[1].order").value(2))
                .andExpect(jsonPath("$[1].chapterId").value(5));
    }

    @Test
    @DisplayName("GET /api/lessons without chapterId returns 400")
    void getAllByChapter_withoutChapterId_returns400() throws Exception {
        mockMvc.perform(get("/api/lessons"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(lessonService, never()).getAllByChapter(anyLong());
    }

    @Test
    @DisplayName("GET /api/lessons?chapterId= returns 404 when the chapter does not exist")
    void getAllByChapter_missingChapter_returns404() throws Exception {
        when(lessonService.getAllByChapter(99L)).thenThrow(new ResourceNotFoundException("Chapter", 99L));

        mockMvc.perform(get("/api/lessons").param("chapterId", "99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Chapter with id 99 not found"));
    }

    @Test
    @DisplayName("GET /api/lessons/{id} returns 200 and the lesson with its content")
    void getById_existing_returns200() throws Exception {
        when(lessonService.getById(1L)).thenReturn(lesson(1L, "Lecture for if-else conditions", 1));

        mockMvc.perform(get("/api/lessons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Content of Lecture for if-else conditions"));
    }

    @Test
    @DisplayName("GET /api/lessons/{id} returns 404 when the lesson does not exist")
    void getById_missing_returns404() throws Exception {
        when(lessonService.getById(99L)).thenThrow(new ResourceNotFoundException("Lesson", 99L));

        mockMvc.perform(get("/api/lessons/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Lesson with id 99 not found"))
                .andExpect(jsonPath("$.path").value("/api/lessons/99"));
    }

    @Test
    @DisplayName("POST /api/lessons returns 201 and the created lesson")
    void create_validBody_returns201() throws Exception {
        when(lessonService.create(any(LessonDto.class))).thenReturn(lesson(20L, "Lecture for Loops", 1));
        LessonDto request = LessonDto.builder()
                .name("Lecture for Loops").content("Loops repeat code.").order(1).chapterId(5L).build();

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.name").value("Lecture for Loops"));
    }

    @Test
    @DisplayName("POST /api/lessons returns 400 with validation errors for a blank name and a wrong order")
    void create_invalidBody_returns400() throws Exception {
        LessonDto request = LessonDto.builder().name("").order(0).chapterId(5L).build();

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.order").exists());

        verify(lessonService, never()).create(any(LessonDto.class));
    }

    @Test
    @DisplayName("POST /api/lessons returns 400 when the service reports a bad request")
    void create_withoutChapterId_returns400() throws Exception {
        when(lessonService.create(any(LessonDto.class))).thenThrow(new BadRequestException("chapterId is required"));
        LessonDto request = LessonDto.builder().name("Lecture for Loops").order(1).build();

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("chapterId is required"));
    }

    @Test
    @DisplayName("POST /api/lessons returns 404 when the chapter does not exist")
    void create_missingChapter_returns404() throws Exception {
        when(lessonService.create(any(LessonDto.class))).thenThrow(new ResourceNotFoundException("Chapter", 99L));
        LessonDto request = LessonDto.builder().name("Lecture for Loops").order(1).chapterId(99L).build();

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/lessons/{id} returns 200 and the updated lesson")
    void update_existing_returns200() throws Exception {
        when(lessonService.update(eq(1L), any(LessonDto.class))).thenReturn(lesson(1L, "New name", 2));
        LessonDto request = LessonDto.builder().name("New name").order(2).build();

        mockMvc.perform(put("/api/lessons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"));
    }

    @Test
    @DisplayName("PUT /api/lessons/{id} returns 404 when the lesson does not exist")
    void update_missing_returns404() throws Exception {
        when(lessonService.update(eq(99L), any(LessonDto.class))).thenThrow(new ResourceNotFoundException("Lesson", 99L));
        LessonDto request = LessonDto.builder().name("New name").order(2).build();

        mockMvc.perform(put("/api/lessons/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/lessons/{id} returns 204")
    void delete_existing_returns204() throws Exception {
        mockMvc.perform(delete("/api/lessons/1"))
                .andExpect(status().isNoContent());

        verify(lessonService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/lessons/{id} returns 404 when the lesson does not exist")
    void delete_missing_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Lesson", 99L)).when(lessonService).delete(99L);

        mockMvc.perform(delete("/api/lessons/99"))
                .andExpect(status().isNotFound());
    }
}