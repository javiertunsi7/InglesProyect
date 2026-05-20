package com.englishlearning.dto;

import java.util.List;

/**
 * Página de resultados del diccionario. Los campos {@code page} y {@code size}
 * son los efectivamente aplicados por el servidor (saneados respecto a lo que
 * pidió el cliente).
 */
public record DictionaryPageResponse(
        List<DictionaryEntryResponse> items,
        long total,
        int page,
        int size,
        int totalPages
) {}
