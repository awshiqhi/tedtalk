package com.io.tedtalk.controller;

import com.io.tedtalk.dto.ImportResponse;
import com.io.tedtalk.service.CsvImportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/tedtalks")
@RequiredArgsConstructor
public class CsvImportController {

    private final CsvImportService csvImportService;

    @Operation(
            summary = "Import TED Talks from a CSV file",
            description = "Allows uploading a CSV file to import TED Talk data. Validates fields and handles errorCount."
    )
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResponse> importTedTalks(@RequestParam("file") MultipartFile file) throws Exception {
        ImportResponse response = csvImportService.importTedTalksFromCsv(file);
        return ResponseEntity.ok(response);
    }

}


