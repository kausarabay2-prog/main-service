package com.bitlab.main_service.service;

import com.bitlab.main_service.dto.ChapterDto;
import com.bitlab.main_service.entity.Chapter;
import com.bitlab.main_service.entity.Course;
import com.bitlab.main_service.exception.BadRequestException;
import com.bitlab.main_service.exception.ResourceNotFoundException;
import com.bitlab.main_service.mapper.ChapterMapper;
import com.bitlab.main_service.repository.ChapterRepository;
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
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;
    private final ChapterMapper chapterMapper;

    /**
     * Chapters of one course, sorted by their order.
     */
    @Transactional(readOnly = true)
    public List<ChapterDto> getAllByCourse(Long courseId) {
        log.debug("Fetching chapters of course: courseId={}", courseId);
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", courseId);
        }
        return chapterRepository.findAllByCourseId(courseId, Sort.by(Sort.Direction.ASC, "order")).stream()
                .map(chapterMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChapterDto getById(Long id) {
        log.debug("Fetching chapter by id={}", id);
        return chapterMapper.toDto(findOrThrow(id));
    }

    @Transactional
    public ChapterDto create(ChapterDto dto) {
        log.info("Creating new chapter");
        log.debug("Chapter data: {}", dto);
        if (dto.getCourseId() == null) {
            throw new BadRequestException("courseId is required");
        }
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", dto.getCourseId()));
        Chapter chapter = chapterMapper.toEntity(dto);
        chapter.setCourse(course);
        Chapter saved = chapterRepository.save(chapter);
        log.info("Chapter created: id={}", saved.getId());
        return chapterMapper.toDto(saved);
    }

    @Transactional
    public ChapterDto update(Long id, ChapterDto dto) {
        log.info("Updating chapter with id {}", id);
        log.debug("New chapter data: {}", dto);
        Chapter chapter = findOrThrow(id);
        chapterMapper.updateEntity(dto, chapter);
        Chapter saved = chapterRepository.save(chapter);
        log.info("Chapter updated: id={}", saved.getId());
        return chapterMapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting chapter: id={}", id);
        if (!chapterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Chapter", id);
        }
        chapterRepository.deleteById(id);
        log.info("Chapter deleted: id={}", id);
    }

    private Chapter findOrThrow(Long id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", id));
    }
}