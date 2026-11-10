package com.bitlab.main_service.controller;

import com.bitlab.main_service.dto.LessonDto;
import com.bitlab.main_service.dto.ErrorResponse;
import com.bitlab.main_service.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@Tag(name = "Lessons", description = "Manage lessons of chapters")
public class LessonController {

    private final LessonService lessonService;

    @GetMapping
    @Operation(summary = "Get the lessons of a chapter, sorted by order")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Chapter not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<LessonDto> getAllByChapter(
            @Parameter(description = "Id of the chapter") @RequestParam Long chapterId) {
        return lessonService.getAllByChapter(chapterId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a lesson by id")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Lesson not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public LessonDto getById(@PathVariable Long id) {
        return lessonService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create a lesson (chapterId is required)")
    @ApiResponse(responseCode = "201", description = "Lesson created")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Chapter not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<LessonDto> create(@Valid @RequestBody LessonDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a lesson (the chapter of a lesson does not change)")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Lesson not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public LessonDto update(@PathVariable Long id, @Valid @RequestBody LessonDto dto) {
        return lessonService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a lesson")
    @ApiResponse(responseCode = "204", description = "Lesson deleted")
    @ApiResponse(responseCode = "404", description = "Lesson not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lessonService.delete(id);
        return ResponseEntity.noContent().build();
    }
}