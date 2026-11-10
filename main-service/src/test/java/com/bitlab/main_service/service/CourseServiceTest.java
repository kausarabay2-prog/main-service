package com.bitlab.main_service.service;

import com.bitlab.main_service.dto.CourseDto;
import com.bitlab.main_service.entity.Course;
import com.bitlab.main_service.exception.ResourceNotFoundException;
import com.bitlab.main_service.mapper.CourseMapper;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The repository is replaced by a Mockito mock, the mapper is the real MapStruct mapper.
 */
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Spy
    private CourseMapper courseMapper = Mappers.getMapper(CourseMapper.class);

    @InjectMocks
    private CourseService courseService;

    private Course course(Long id, String name, String description) {
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        course.setDescription(description);
        return course;
    }

    // ---------------------------------------------------------------- getAll

    @Test
    @DisplayName("getAll returns all courses sorted by id")
    void getAll_returnsAllCourses() {
        when(courseRepository.findAll(any(Sort.class))).thenReturn(List.of(
                course(1L, "Java Developer", "Java course"),
                course(2L, "Python Developer", "Python course")));

        List<CourseDto> result = courseService.getAll();

        assertEquals(2, result.size());
        assertEquals("Java Developer", result.get(0).getName());
        assertEquals("Python Developer", result.get(1).getName());
        verify(courseRepository).findAll(Sort.by("id"));
    }

    @Test
    @DisplayName("getAll returns an empty list when there are no courses")
    void getAll_noCourses_returnsEmptyList() {
        when(courseRepository.findAll(any(Sort.class))).thenReturn(List.of());

        assertTrue(courseService.getAll().isEmpty());
    }

    // --------------------------------------------------------------- getById

    @Test
    @DisplayName("getById returns the course when it exists")
    void getById_existingCourse_returnsDto() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course(1L, "Java Developer", "Java course")));

        CourseDto result = courseService.getById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Java Developer", result.getName());
        assertEquals("Java course", result.getDescription());
    }

    @Test
    @DisplayName("getById throws ResourceNotFoundException when the course does not exist")
    void getById_missingCourse_throwsNotFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class, () -> courseService.getById(99L));

        assertEquals("Course with id 99 not found", ex.getMessage());
    }

    // ---------------------------------------------------------------- create

    @Test
    @DisplayName("create saves a new course and returns it with a generated id")
    void create_validDto_savesAndReturnsDto() {
        CourseDto request = CourseDto.builder().name("Java Developer").description("Java course").build();
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        CourseDto result = courseService.create(request);

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(captor.capture());
        assertEquals("Java Developer", captor.getValue().getName());
        assertEquals("Java course", captor.getValue().getDescription());
        assertEquals(10L, result.getId());
        assertEquals("Java Developer", result.getName());
    }

    @Test
    @DisplayName("create ignores an id sent by the client")
    void create_idInRequest_isIgnored() {
        CourseDto request = CourseDto.builder().id(555L).name("Java Developer").build();
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        courseService.create(request);

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(captor.capture());
        assertNull(captor.getValue().getId());
    }

    // ---------------------------------------------------------------- update

    @Test
    @DisplayName("update changes the fields of an existing course")
    void update_existingCourse_updatesFields() {
        Course existing = course(1L, "Java", "Old description");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CourseDto request = CourseDto.builder().name("Java Developer").description("New description").build();

        CourseDto result = courseService.update(1L, request);

        assertEquals(1L, result.getId());
        assertEquals("Java Developer", result.getName());
        assertEquals("New description", result.getDescription());
        verify(courseRepository).save(existing);
    }

    @Test
    @DisplayName("update throws ResourceNotFoundException and saves nothing when the course does not exist")
    void update_missingCourse_throwsNotFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());
        CourseDto request = CourseDto.builder().name("Java Developer").build();

        assertThrows(ResourceNotFoundException.class, () -> courseService.update(99L, request));

        verify(courseRepository, never()).save(any(Course.class));
    }

    // ---------------------------------------------------------------- delete

    @Test
    @DisplayName("delete removes an existing course")
    void delete_existingCourse_deletesById() {
        when(courseRepository.existsById(1L)).thenReturn(true);

        courseService.delete(1L);

        verify(courseRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete throws ResourceNotFoundException and deletes nothing when the course does not exist")
    void delete_missingCourse_throwsNotFound() {
        when(courseRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> courseService.delete(99L));

        verify(courseRepository, never()).deleteById(anyLong());
    }
}