package com.bitlab.main_service.mapper;

import com.bitlab.main_service.dto.ChapterDto;
import com.bitlab.main_service.entity.Chapter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ChapterMapper {

    @Mapping(source = "course.id", target = "courseId")
    ChapterDto toDto(Chapter chapter);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    Chapter toEntity(ChapterDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    void updateEntity(ChapterDto dto, @MappingTarget Chapter chapter);
}