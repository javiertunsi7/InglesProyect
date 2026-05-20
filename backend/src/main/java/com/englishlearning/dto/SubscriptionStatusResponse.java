package com.englishlearning.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionStatusResponse {

    private boolean premium;
    private String plan;
    private String status;
    private Long currentPeriodEnd;
    private Long trialEnd;
    private boolean cancelAtPeriodEnd;
}
