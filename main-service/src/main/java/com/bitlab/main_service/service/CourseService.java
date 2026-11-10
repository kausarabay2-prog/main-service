package com.bitlab.main_service.service;

import com.bitlab.main_service.dto.CourseDto;
import com.bitlab.main_service.entity.Course;
import com.bitlab.main_service.exception.ResourceNotFoundException;
import com.bitlab.main_service.mapper.CourseMapper;
import com.bitlab.main_service.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Transactional(readOnly = true)
    public List<CourseDto> getAll() {
        log.debug("Fetching all courses");
        return courseRepository.findAll(Sort.by("id")).stream()
                .map(courseMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseDto getById(Long id) {
        log.debug("Fetching course by id={}", id);
        return courseMapper.toDto(findOrThrow(id));
    }

    @Transactional
    public CourseDto create(CourseDto dto) {
        log.info("Creating new course");
        log.debug("Course data: {}", dto);
        Course saved = courseRepository.save(courseMapper.toEntity(dto));
        log.info("Course created: id={}", saved.getId());
        return courseMapper.toDto(saved);
    }

    @Transactional
    public CourseDto update(Long id, CourseDto dto) {
        log.info("Updating course with id {}", id);
        log.debug("New course data: {}", dto);
        Course course = findOrThrow(id);
        courseMapper.updateEntity(dto, course);
        Course saved = courseRepository.save(course);
        log.info("Course updated: id={}", saved.getId());
        return courseMapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting course: id={}", id);
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course", id);
        }
        courseRepository.deleteById(id);
        log.info("Course deleted: id={}", id);
    }

    private Course findOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }
}