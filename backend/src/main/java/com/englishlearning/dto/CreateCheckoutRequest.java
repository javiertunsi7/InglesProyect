package com.englishlearning.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCheckoutRequest {

    @NotBlank
    private String priceId;
}
