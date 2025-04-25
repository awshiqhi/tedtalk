package com.io.tedtalk.dto;

import java.util.List;

public record ImportResponse(
            int importedCount,
            int skippedCount,
            int errorCount,
            List<String> messages
    ) { }

