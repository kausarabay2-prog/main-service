package com.bitlab.main_service.mapper;

import com.bitlab.main_service.dto.CourseDto;
import com.bitlab.main_service.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    CourseDto toDto(Course course);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    Course toEntity(CourseDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    void updateEntity(CourseDto dto, @MappingTarget Course course);
}