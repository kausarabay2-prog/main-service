package com.bitlab.main_service.repository;

import com.bitlab.main_service.entity.Chapter;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    List<Chapter> findAllByCourseId(Long courseId, Sort sort);
}