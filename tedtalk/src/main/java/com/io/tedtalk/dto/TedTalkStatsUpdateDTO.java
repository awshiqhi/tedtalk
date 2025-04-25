package com.io.tedtalk.dto;

import jakarta.validation.constraints.Min;

public record TedTalkStatsUpdateDTO(
        @Min(0) Integer views,
        @Min(0) Integer likes
) {}

