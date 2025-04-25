package com.io.tedtalk.dto;

public record MostInfluentialSpeakerDTO(
        int year,
        String author,
        int views,
        int likes,
        long influenceScore
) {}
