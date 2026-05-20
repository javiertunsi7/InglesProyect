package com.englishlearning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnswerRequest(
        @NotBlank(message = "La respuesta no puede estar vacía.")
        @Size(max = 255, message = "La respuesta es demasiado larga.")
        String answer
) {}
