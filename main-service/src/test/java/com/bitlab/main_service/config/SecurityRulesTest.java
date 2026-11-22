package com.bitlab.main_service.config;

import com.bitlab.main_service.controller.CourseController;
import com.bitlab.main_service.controller.MeController;
import com.bitlab.main_service.dto.CourseDto;
import com.bitlab.main_service.service.CourseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CourseController.class, MeController.class})
@Import(SecurityConfig.class)
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static final String COURSE_JSON = "{\"name\":\"Java Developer\",\"description\":\"Java course\"}";

    @Test
    @DisplayName("GET /api/courses without a token returns 401")
    void get_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/courses with the STUDENT role returns 200")
    void get_asStudent_returns200() throws Exception {
        when(courseService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/courses")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/courses without a token returns 401")
    void post_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COURSE_JSON))
                .andExpect(status().isUnauthorized());

        verify(courseService, never()).create(any(CourseDto.class));
    }

    @Test
    @DisplayName("POST /api/courses with the STUDENT role returns 403")
    void post_asStudent_returns403() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COURSE_JSON))
                .andExpect(status().isForbidden());

        verify(courseService, never()).create(any(CourseDto.class));
    }

    @Test
    @DisplayName("POST /api/courses with the ADMIN role returns 201")
    void post_asAdmin_returns201() throws Exception {
        when(courseService.create(any(CourseDto.class)))
                .thenReturn(CourseDto.builder().id(1L).name("Java Developer").build());

        mockMvc.perform(post("/api/courses")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COURSE_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("DELETE /api/courses/1 with the STUDENT role returns 403")
    void delete_asStudent_returns403() throws Exception {
        mockMvc.perform(delete("/api/courses/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
                .andExpect(status().isForbidden());

        verify(courseService, never()).delete(1L);
    }

    @Test
    @DisplayName("GET /api/me returns the Spring Security user and its authorities")
    @WithMockUser(username = "admin1", roles = "ADMIN")
    void me_returnsUserAndAuthorities() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin1"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("GET /api/me without a token returns 401")
    void me_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }
}