package com.bitlab.main_service.service;

import com.bitlab.main_service.dto.LessonDto;
import com.bitlab.main_service.entity.Chapter;
import com.bitlab.main_service.entity.Lesson;
import com.bitlab.main_service.exception.BadRequestException;
import com.bitlab.main_service.exception.ResourceNotFoundException;
import com.bitlab.main_service.mapper.LessonMapper;
import com.bitlab.main_service.repository.ChapterRepository;
import com.bitlab.main_service.repository.LessonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Spy
    private LessonMapper lessonMapper = Mappers.getMapper(LessonMapper.class);

    @InjectMocks
    private LessonService lessonService;

    private Chapter chapter(Long id) {
        Chapter chapter = new Chapter();
        chapter.setId(id);
        chapter.setName("if-else conditions");
        return chapter;
    }

    private Lesson lesson(Long id, String name, int order, Chapter chapter) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.setName(name);
        lesson.setDescription("Description of " + name);
        lesson.setContent("Content of " + name);
        lesson.setOrder(order);
        lesson.setChapter(chapter);
        return lesson;
    }

    // -------------------------------------------------------- getAllByChapter

    @Test
    @DisplayName("getAllByChapter returns the lessons of the chapter sorted by order")
    void getAllByChapter_returnsLessonsSortedByOrder() {
        Chapter chapter = chapter(5L);
        when(chapterRepository.existsById(5L)).thenReturn(true);
        when(lessonRepository.findAllByChapterId(anyLong(), any(Sort.class))).thenReturn(List.of(
                lesson(1L, "Lecture for if-else conditions", 1, chapter),
                lesson(2L, "Practice for if-else conditions", 2, chapter)));

        List<LessonDto> result = lessonService.getAllByChapter(5L);

        assertEquals(2, result.size());
        assertEquals("Lecture for if-else conditions", result.get(0).getName());
        assertEquals(5L, result.get(0).getChapterId());
        assertEquals("Practice for if-else conditions", result.get(1).getName());

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(lessonRepository).findAllByChapterId(anyLong(), sortCaptor.capture());
        assertEquals(Sort.by(Sort.Direction.ASC, "order"), sortCaptor.getValue());
    }

    @Test
    @DisplayName("getAllByChapter throws ResourceNotFoundException when the chapter does not exist")
    void getAllByChapter_missingChapter_throwsNotFound() {
        when(chapterRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> lessonService.getAllByChapter(99L));

        verify(lessonRepository, never()).findAllByChapterId(anyLong(), any(Sort.class));
    }

    // --------------------------------------------------------------- getById

    @Test
    @DisplayName("getById returns the lesson with its content when it exists")
    void getById_existingLesson_returnsDto() {
        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson(1L, "Lecture for if-else conditions", 1, chapter(5L))));

        LessonDto result = lessonService.getById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Lecture for if-else conditions", result.getName());
        assertEquals("Content of Lecture for if-else conditions", result.getContent());
        assertEquals(5L, result.getChapterId());
    }

    @Test
    @DisplayName("getById throws ResourceNotFoundException when the lesson does not exist")
    void getById_missingLesson_throwsNotFound() {
        when(lessonRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class, () -> lessonService.getById(99L));

        assertEquals("Lesson with id 99 not found", ex.getMessage());
    }

    // ---------------------------------------------------------------- create

    @Test
    @DisplayName("create saves the lesson and links it to its chapter")
    void create_validDto_savesLessonWithChapter() {
        Chapter chapter = chapter(5L);
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson saved = invocation.getArgument(0);
            saved.setId(20L);
            return saved;
        });
        LessonDto request = LessonDto.builder()
                .name("Lecture for Loops").description("About loops").content("Loops repeat code.")
                .order(1).chapterId(5L).build();

        LessonDto result = lessonService.create(request);

        ArgumentCaptor<Lesson> captor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).save(captor.capture());
        assertEquals("Lecture for Loops", captor.getValue().getName());
        assertEquals("Loops repeat code.", captor.getValue().getContent());
        assertSame(chapter, captor.getValue().getChapter());
        assertEquals(20L, result.getId());
        assertEquals(5L, result.getChapterId());
    }

    @Test
    @DisplayName("create throws BadRequestException when chapterId is missing")
    void create_withoutChapterId_throwsBadRequest() {
        LessonDto request = LessonDto.builder().name("Lecture for Loops").order(1).build();

        assertThrows(BadRequestException.class, () -> lessonService.create(request));

        verifyNoInteractions(chapterRepository, lessonRepository);
    }

    @Test
    @DisplayName("create throws ResourceNotFoundException when the chapter does not exist")
    void create_missingChapter_throwsNotFound() {
        when(chapterRepository.findById(99L)).thenReturn(Optional.empty());
        LessonDto request = LessonDto.builder().name("Lecture for Loops").order(1).chapterId(99L).build();

        assertThrows(ResourceNotFoundException.class, () -> lessonService.create(request));

        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    // ---------------------------------------------------------------- update

    @Test
    @DisplayName("update changes the fields of an existing lesson and keeps its chapter")
    void update_existingLesson_updatesFields() {
        Chapter chapter = chapter(5L);
        Lesson existing = lesson(1L, "Old name", 1, chapter);
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));
        LessonDto request = LessonDto.builder()
                .name("New name").description("New description").content("New content")
                .order(4).chapterId(777L).build();

        LessonDto result = lessonService.update(1L, request);

        assertEquals("New name", result.getName());
        assertEquals("New description", result.getDescription());
        assertEquals("New content", result.getContent());
        assertEquals(4, result.getOrder());
        assertEquals(5L, result.getChapterId());
        assertSame(chapter, existing.getChapter());
        verify(lessonRepository).save(existing);
    }

    @Test
    @DisplayName("update throws ResourceNotFoundException and saves nothing when the lesson does not exist")
    void update_missingLesson_throwsNotFound() {
        when(lessonRepository.findById(99L)).thenReturn(Optional.empty());
        LessonDto request = LessonDto.builder().name("New name").order(1).build();

        assertThrows(ResourceNotFoundException.class, () -> lessonService.update(99L, request));

        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    // ---------------------------------------------------------------- delete

    @Test
    @DisplayName("delete removes an existing lesson")
    void delete_existingLesson_deletesById() {
        when(lessonRepository.existsById(1L)).thenReturn(true);

        lessonService.delete(1L);

        verify(lessonRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete throws ResourceNotFoundException and deletes nothing when the lesson does not exist")
    void delete_missingLesson_throwsNotFound() {
        when(lessonRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> lessonService.delete(99L));

        verify(lessonRepository, never()).deleteById(anyLong());
    }
}