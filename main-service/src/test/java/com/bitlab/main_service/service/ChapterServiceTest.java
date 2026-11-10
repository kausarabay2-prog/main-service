package com.bitlab.main_service.service;

import com.bitlab.main_service.dto.ChapterDto;
import com.bitlab.main_service.entity.Chapter;
import com.bitlab.main_service.entity.Course;
import com.bitlab.main_service.exception.BadRequestException;
import com.bitlab.main_service.exception.ResourceNotFoundException;
import com.bitlab.main_service.mapper.ChapterMapper;
import com.bitlab.main_service.repository.ChapterRepository;
import com.bitlab.main_service.repository.CourseRepository;
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
class ChapterServiceTest {

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private CourseRepository courseRepository;

    @Spy
    private ChapterMapper chapterMapper = Mappers.getMapper(ChapterMapper.class);

    @InjectMocks
    private ChapterService chapterService;

    private Course course(Long id) {
        Course course = new Course();
        course.setId(id);
        course.setName("Java Developer");
        return course;
    }

    private Chapter chapter(Long id, String name, int order, Course course) {
        Chapter chapter = new Chapter();
        chapter.setId(id);
        chapter.setName(name);
        chapter.setDescription("Description of " + name);
        chapter.setOrder(order);
        chapter.setCourse(course);
        return chapter;
    }

    // --------------------------------------------------------- getAllByCourse

    @Test
    @DisplayName("getAllByCourse returns the chapters of the course sorted by order")
    void getAllByCourse_returnsChaptersSortedByOrder() {
        Course course = course(1L);
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(chapterRepository.findAllByCourseId(anyLong(), any(Sort.class))).thenReturn(List.of(
                chapter(10L, "Variables and Data Types", 1, course),
                chapter(11L, "if-else conditions", 2, course)));

        List<ChapterDto> result = chapterService.getAllByCourse(1L);

        assertEquals(2, result.size());
        assertEquals("Variables and Data Types", result.get(0).getName());
        assertEquals(1L, result.get(0).getCourseId());
        assertEquals("if-else conditions", result.get(1).getName());

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(chapterRepository).findAllByCourseId(anyLong(), sortCaptor.capture());
        assertEquals(Sort.by(Sort.Direction.ASC, "order"), sortCaptor.getValue());
    }

    @Test
    @DisplayName("getAllByCourse throws ResourceNotFoundException when the course does not exist")
    void getAllByCourse_missingCourse_throwsNotFound() {
        when(courseRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> chapterService.getAllByCourse(99L));

        verify(chapterRepository, never()).findAllByCourseId(anyLong(), any(Sort.class));
    }

    // --------------------------------------------------------------- getById

    @Test
    @DisplayName("getById returns the chapter when it exists")
    void getById_existingChapter_returnsDto() {
        when(chapterRepository.findById(10L))
                .thenReturn(Optional.of(chapter(10L, "if-else conditions", 2, course(1L))));

        ChapterDto result = chapterService.getById(10L);

        assertEquals(10L, result.getId());
        assertEquals("if-else conditions", result.getName());
        assertEquals(2, result.getOrder());
        assertEquals(1L, result.getCourseId());
    }

    @Test
    @DisplayName("getById throws ResourceNotFoundException when the chapter does not exist")
    void getById_missingChapter_throwsNotFound() {
        when(chapterRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class, () -> chapterService.getById(99L));

        assertEquals("Chapter with id 99 not found", ex.getMessage());
    }

    // ---------------------------------------------------------------- create

    @Test
    @DisplayName("create saves the chapter and links it to its course")
    void create_validDto_savesChapterWithCourse() {
        Course course = course(1L);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(chapterRepository.save(any(Chapter.class))).thenAnswer(invocation -> {
            Chapter saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        ChapterDto request = ChapterDto.builder()
                .name("Loops").description("About loops").order(3).courseId(1L).build();

        ChapterDto result = chapterService.create(request);

        ArgumentCaptor<Chapter> captor = ArgumentCaptor.forClass(Chapter.class);
        verify(chapterRepository).save(captor.capture());
        assertEquals("Loops", captor.getValue().getName());
        assertEquals(3, captor.getValue().getOrder());
        assertSame(course, captor.getValue().getCourse());
        assertEquals(10L, result.getId());
        assertEquals(1L, result.getCourseId());
    }

    @Test
    @DisplayName("create throws BadRequestException when courseId is missing")
    void create_withoutCourseId_throwsBadRequest() {
        ChapterDto request = ChapterDto.builder().name("Loops").order(3).build();

        assertThrows(BadRequestException.class, () -> chapterService.create(request));

        verifyNoInteractions(courseRepository, chapterRepository);
    }

    @Test
    @DisplayName("create throws ResourceNotFoundException when the course does not exist")
    void create_missingCourse_throwsNotFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());
        ChapterDto request = ChapterDto.builder().name("Loops").order(3).courseId(99L).build();

        assertThrows(ResourceNotFoundException.class, () -> chapterService.create(request));

        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    // ---------------------------------------------------------------- update

    @Test
    @DisplayName("update changes the fields of an existing chapter and keeps its course")
    void update_existingChapter_updatesFields() {
        Course course = course(1L);
        Chapter existing = chapter(10L, "Old name", 1, course);
        when(chapterRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(chapterRepository.save(any(Chapter.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ChapterDto request = ChapterDto.builder()
                .name("New name").description("New description").order(5).courseId(777L).build();

        ChapterDto result = chapterService.update(10L, request);

        assertEquals("New name", result.getName());
        assertEquals("New description", result.getDescription());
        assertEquals(5, result.getOrder());
        assertEquals(1L, result.getCourseId());
        assertSame(course, existing.getCourse());
        verify(chapterRepository).save(existing);
    }

    @Test
    @DisplayName("update throws ResourceNotFoundException and saves nothing when the chapter does not exist")
    void update_missingChapter_throwsNotFound() {
        when(chapterRepository.findById(99L)).thenReturn(Optional.empty());
        ChapterDto request = ChapterDto.builder().name("New name").order(1).build();

        assertThrows(ResourceNotFoundException.class, () -> chapterService.update(99L, request));

        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    // ---------------------------------------------------------------- delete

    @Test
    @DisplayName("delete removes an existing chapter")
    void delete_existingChapter_deletesById() {
        when(chapterRepository.existsById(10L)).thenReturn(true);

        chapterService.delete(10L);

        verify(chapterRepository).deleteById(10L);
    }

    @Test
    @DisplayName("delete throws ResourceNotFoundException and deletes nothing when the chapter does not exist")
    void delete_missingChapter_throwsNotFound() {
        when(chapterRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> chapterService.delete(99L));

        verify(chapterRepository, never()).deleteById(anyLong());
    }
}