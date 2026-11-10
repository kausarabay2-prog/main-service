package com.bitlab.main_service.service;

import com.bitlab.main_service.dto.LessonDto;
import com.bitlab.main_service.entity.Chapter;
import com.bitlab.main_service.entity.Lesson;
import com.bitlab.main_service.exception.BadRequestException;
import com.bitlab.main_service.exception.ResourceNotFoundException;
import com.bitlab.main_service.mapper.LessonMapper;
import com.bitlab.main_service.repository.ChapterRepository;
import com.bitlab.main_service.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ChapterRepository chapterRepository;
    private final LessonMapper lessonMapper;

    /**
     * Lessons of one chapter, sorted by their order.
     */
    @Transactional(readOnly = true)
    public List<LessonDto> getAllByChapter(Long chapterId) {
        log.debug("Fetching lessons of chapter: chapterId={}", chapterId);
        if (!chapterRepository.existsById(chapterId)) {
            throw new ResourceNotFoundException("Chapter", chapterId);
        }
        return lessonRepository.findAllByChapterId(chapterId, Sort.by(Sort.Direction.ASC, "order")).stream()
                .map(lessonMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public LessonDto getById(Long id) {
        log.debug("Fetching lesson by id={}", id);
        return lessonMapper.toDto(findOrThrow(id));
    }

    @Transactional
    public LessonDto create(LessonDto dto) {
        log.info("Creating new lesson");
        log.debug("Lesson data: {}", dto);
        if (dto.getChapterId() == null) {
            throw new BadRequestException("chapterId is required");
        }
        Chapter chapter = chapterRepository.findById(dto.getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException("Chapter", dto.getChapterId()));
        Lesson lesson = lessonMapper.toEntity(dto);
        lesson.setChapter(chapter);
        Lesson saved = lessonRepository.save(lesson);
        log.info("Lesson created: id={}", saved.getId());
        return lessonMapper.toDto(saved);
    }

    @Transactional
    public LessonDto update(Long id, LessonDto dto) {
        log.info("Updating lesson with id {}", id);
        log.debug("New lesson data: {}", dto);
        Lesson lesson = findOrThrow(id);
        lessonMapper.updateEntity(dto, lesson);
        Lesson saved = lessonRepository.save(lesson);
        log.info("Lesson updated: id={}", saved.getId());
        return lessonMapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting lesson: id={}", id);
        if (!lessonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lesson", id);
        }
        lessonRepository.deleteById(id);
        log.info("Lesson deleted: id={}", id);
    }

    private Lesson findOrThrow(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));
    }
}