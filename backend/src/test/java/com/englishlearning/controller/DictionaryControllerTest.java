package com.englishlearning.controller;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.LevelCode;
import com.englishlearning.dto.DictionaryEntryResponse;
import com.englishlearning.dto.DictionaryPageResponse;
import com.englishlearning.security.JwtService;
import com.englishlearning.security.SecurityConfig;
import com.englishlearning.service.DictionaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DictionaryController.class)
@Import(SecurityConfig.class)
class DictionaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DictionaryService dictionaryService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void search_shouldReturn200WithEntries() throws Exception {
        var entries = List.of(
                new DictionaryEntryResponse(1L, "hello", "/heˈloʊ/", "interjection",
                        "Saludo informal", "Hello!", "¡Hola!", CategoryType.GENERAL, LevelCode.A1)
        );
        var page = new DictionaryPageResponse(entries, 1L, 0, 24, 1);
        when(dictionaryService.search(any(), any(), any(), eq(0), eq(24))).thenReturn(page);

        mockMvc.perform(get("/v1/dictionary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].word").value("hello"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void search_withQuery_shouldReturnFiltered() throws Exception {
        var entries = List.of(
                new DictionaryEntryResponse(1L, "bug", "/bʌɡ/", "noun",
                        "Error en el código", "I fixed a bug.", "Arreglé un bug.",
                        CategoryType.TECH, LevelCode.A1)
        );
        var page = new DictionaryPageResponse(entries, 1L, 0, 24, 1);
        when(dictionaryService.search(eq("bug"), any(), any(), eq(0), eq(24))).thenReturn(page);

        mockMvc.perform(get("/v1/dictionary?q=bug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].word").value("bug"));
    }

    @Test
    void search_shouldReturnEmptyForNoMatch() throws Exception {
        var page = new DictionaryPageResponse(List.of(), 0L, 0, 24, 0);
        when(dictionaryService.search(eq("zzzzz"), any(), any(), eq(0), eq(24))).thenReturn(page);

        mockMvc.perform(get("/v1/dictionary?q=zzzzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.total").value(0));
    }
}
