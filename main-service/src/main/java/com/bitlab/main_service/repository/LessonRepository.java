package com.bitlab.main_service.repository;

import com.bitlab.main_service.entity.Lesson;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findAllByChapterId(Long chapterId, Sort sort);
}