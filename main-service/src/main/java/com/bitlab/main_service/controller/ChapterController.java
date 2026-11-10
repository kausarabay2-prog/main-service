package com.bitlab.main_service.controller;

import com.bitlab.main_service.dto.ChapterDto;
import com.bitlab.main_service.dto.ErrorResponse;
import com.bitlab.main_service.service.ChapterService;
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
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
@Tag(name = "Chapters", description = "Manage chapters of courses")
public class ChapterController {

    private final ChapterService chapterService;

    @GetMapping
    @Operation(summary = "Get the chapters of a course, sorted by order")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Course not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<ChapterDto> getAllByCourse(
            @Parameter(description = "Id of the course") @RequestParam Long courseId) {
        return chapterService.getAllByCourse(courseId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a chapter by id")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Chapter not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ChapterDto getById(@PathVariable Long id) {
        return chapterService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create a chapter (courseId is required)")
    @ApiResponse(responseCode = "201", description = "Chapter created")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Course not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ChapterDto> create(@Valid @RequestBody ChapterDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chapterService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a chapter (the course of a chapter does not change)")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Chapter not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ChapterDto update(@PathVariable Long id, @Valid @RequestBody ChapterDto dto) {
        return chapterService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a chapter together with its lessons")
    @ApiResponse(responseCode = "204", description = "Chapter deleted")
    @ApiResponse(responseCode = "404", description = "Chapter not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        chapterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}