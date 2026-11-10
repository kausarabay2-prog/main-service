package com.bitlab.main_service.controller;

import com.bitlab.main_service.dto.CourseDto;
import com.bitlab.main_service.dto.ErrorResponse;
import com.bitlab.main_service.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Manage courses")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Get all courses")
    @ApiResponse(responseCode = "200", description = "OK")
    public List<CourseDto> getAll() {
        return courseService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a course by id")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Course not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CourseDto getById(@PathVariable Long id) {
        return courseService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create a course")
    @ApiResponse(responseCode = "201", description = "Course created")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<CourseDto> create(@Valid @RequestBody CourseDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a course")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Course not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CourseDto update(@PathVariable Long id, @Valid @RequestBody CourseDto dto) {
        return courseService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a course together with its chapters and lessons")
    @ApiResponse(responseCode = "204", description = "Course deleted")
    @ApiResponse(responseCode = "404", description = "Course not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}