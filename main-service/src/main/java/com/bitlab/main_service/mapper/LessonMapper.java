package com.bitlab.main_service.mapper;

import com.bitlab.main_service.dto.LessonDto;
import com.bitlab.main_service.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    @Mapping(source = "chapter.id", target = "chapterId")
    LessonDto toDto(Lesson lesson);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chapter", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    Lesson toEntity(LessonDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chapter", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    void updateEntity(LessonDto dto, @MappingTarget Lesson lesson);
}