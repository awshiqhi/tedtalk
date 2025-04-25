package com.io.tedtalk.service;

import com.io.tedtalk.dto.ImportResponse;
import com.io.tedtalk.model.TedTalk;
import com.io.tedtalk.repository.TedTalkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvImportServiceTest {

    @Mock
    private TedTalkRepository tedTalkRepository;

    @InjectMocks
    private CsvImportService csvImportService;

    private final String VALID_HEADER = "title,author,date,views,likes,link";
    private final String VALID_ROW = "The power of vulnerability,Brené Brown,June 2010,50000000,2500000,https://example.com/brown";

    @BeforeEach
    void setUp() {
    }
    @Test
    void importTedTalksFromCsv_WithNonCsvFile_ShouldThrowException() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> {
            csvImportService.importTedTalksFromCsv(file);
        });
    }

    @Test
    void importTedTalksFromCsv_WithInvalidHeader_ShouldThrowException() {
        // Arrange
        String content = "invalid,header,row\n" + VALID_ROW;
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                content.getBytes()
        );

        assertThrows(Exception.class, () -> {
            csvImportService.importTedTalksFromCsv(file);
        });
    }
    @Test
    void importTedTalksFromCsv_WithEmptyRow_ShouldSkipAndCountError() throws Exception {
        String content = VALID_HEADER + "\n \n" + VALID_ROW;
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                content.getBytes()
        );

        when(tedTalkRepository.findByLink(any())).thenReturn(Optional.empty());
        when(tedTalkRepository.saveAll(any())).thenReturn(List.of(new TedTalk()));

        ImportResponse response = csvImportService.importTedTalksFromCsv(file);

        assertEquals(1, response.importedCount());
        assertEquals(0, response.skippedCount());
        assertEquals(1, response.errorCount());
        assertTrue(response.messages().contains("Row 2: Empty row"));
    }

    @Test
    void importTedTalksFromCsv_WithInvalidColumnCount_ShouldSkipAndCountError() throws Exception {
        String content = VALID_HEADER + "\ntitle,author,date,views"; // missing likes and link
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                content.getBytes()
        );

        ImportResponse response = csvImportService.importTedTalksFromCsv(file);

        assertEquals(0, response.importedCount());
        assertEquals(0, response.skippedCount());
        assertEquals(1, response.errorCount());
        assertTrue(response.messages().stream()
                .anyMatch(msg -> msg.contains("Expected 6 columns")));
    }

    @Test
    void importTedTalksFromCsv_WithEmptyFields_ShouldSkipAndCountError() throws Exception {
        String content = VALID_HEADER + "\n,,June 2010,50000000,2500000,https://example.com/brown";
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                content.getBytes()
        );

        ImportResponse response = csvImportService.importTedTalksFromCsv(file);

        assertEquals(0, response.importedCount());
        assertEquals(0, response.skippedCount());
        assertEquals(1, response.errorCount());
        assertTrue(response.messages().stream()
                .anyMatch(msg -> msg.contains("One or more fields are empty")));
    }

    @Test
    void importTedTalksFromCsv_WithInvalidDate_ShouldSkipAndCountError() throws Exception {
        // Arrange
        String content = VALID_HEADER + "\nThe power of vulnerability,Brené Brown,InvalidDate,50000000,2500000,https://example.com/brown";
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                content.getBytes()
        );

        ImportResponse response = csvImportService.importTedTalksFromCsv(file);

        assertEquals(0, response.importedCount());
        assertEquals(0, response.skippedCount());
        assertEquals(1, response.errorCount());
        assertTrue(response.messages().stream()
                .anyMatch(msg -> msg.contains("Invalid date format")));
    }

    @Test
    void importTedTalksFromCsv_WithInvalidUrl_ShouldSkipAndCountError() throws Exception {

        String content = VALID_HEADER + "\nThe power of vulnerability,Brené Brown,June 2010,50000000,2500000,invalid-url";
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                content.getBytes()
        );

        ImportResponse response = csvImportService.importTedTalksFromCsv(file);

        assertEquals(0, response.importedCount());
        assertEquals(0, response.skippedCount());
        assertEquals(1, response.errorCount());
        assertTrue(response.messages().stream()
                .anyMatch(msg -> msg.contains("Invalid URL format")));
    }

    @Test
    void importTedTalksFromCsv_WithDuplicateLinkInCsv_ShouldSkipAndCountError() throws Exception {

        String content = VALID_HEADER + "\n" + VALID_ROW + "\n" + VALID_ROW;
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                content.getBytes()
        );

        ImportResponse response = csvImportService.importTedTalksFromCsv(file);

        assertEquals(1, response.importedCount());
        assertEquals(0, response.skippedCount());
        assertEquals(1, response.errorCount());
        assertTrue(response.messages().stream()
                .anyMatch(msg -> msg.contains("Duplicate link in CSV")));
    }

    @Test
    void importTedTalksFromCsv_WithExistingLinkInDb_ShouldSkip() throws Exception {
        String content = VALID_HEADER + "\n" + VALID_ROW;
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                content.getBytes()
        );

        when(tedTalkRepository.findByLink(any())).thenReturn(Optional.of(new TedTalk()));

        ImportResponse response = csvImportService.importTedTalksFromCsv(file);

        assertEquals(0, response.importedCount());
        assertEquals(1, response.skippedCount());
        assertEquals(0, response.errorCount());
        assertTrue(response.messages().stream()
                .anyMatch(msg -> msg.contains("Skipped due to duplicate link")));
    }

    @Test
    void importTedTalksFromCsv_WithValidRows_ShouldImportSuccessfully() throws Exception {
        String content = VALID_HEADER + "\n" + VALID_ROW;
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                content.getBytes()
        );

        when(tedTalkRepository.findByLink(any())).thenReturn(Optional.empty());
        when(tedTalkRepository.saveAll(any())).thenReturn(List.of(new TedTalk()));

        ImportResponse response = csvImportService.importTedTalksFromCsv(file);

        assertEquals(1, response.importedCount());
        assertEquals(0, response.skippedCount());
        assertEquals(0, response.errorCount());
        assertTrue(response.messages().isEmpty());
        verify(tedTalkRepository, times(1)).saveAll(any());
    }

    @Test
    void importTedTalksFromCsv_WithMultipleValidRows_ShouldImportAll() throws Exception {
        String row2 = "How to speak so that people want to listen,Julian Treasure,March 2014,45000000,2200000,https://example.com/treasure";
        String content = VALID_HEADER + "\n" + VALID_ROW + "\n" + row2;
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                content.getBytes()
        );

        when(tedTalkRepository.findByLink(any())).thenReturn(Optional.empty());
        when(tedTalkRepository.saveAll(any())).thenReturn(List.of(new TedTalk(), new TedTalk()));

        ImportResponse response = csvImportService.importTedTalksFromCsv(file);

        assertEquals(2, response.importedCount());
        assertEquals(0, response.skippedCount());
        assertEquals(0, response.errorCount());
        verify(tedTalkRepository, times(1)).saveAll(any());
    }
    @Test
    void importTedTalksFromCsv_WithIOException_ShouldThrowException() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.csv");
        when(file.getInputStream()).thenThrow(new IOException("Test exception"));

        assertThrows(IOException.class, () -> {
            csvImportService.importTedTalksFromCsv(file);
        });
    }

    @Test
    void importTedTalksFromCsv_WithUnexpectedError_ShouldThrowExceptionWithRowNumber() throws Exception {
        String content = VALID_HEADER + "\n" + VALID_ROW;
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                content.getBytes()
        );

        when(tedTalkRepository.findByLink(any())).thenThrow(new RuntimeException("Test exception"));

        Exception exception = assertThrows(Exception.class, () -> {
            csvImportService.importTedTalksFromCsv(file);
        });
        assertTrue(exception.getMessage().contains("Error processing file, RowNumber at the time of error is:"));
    }
}