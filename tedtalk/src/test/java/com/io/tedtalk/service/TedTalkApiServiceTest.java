package com.io.tedtalk.service;


import com.io.tedtalk.dto.InfluencerDTO;
import com.io.tedtalk.dto.MostInfluentialSpeakerDTO;
import com.io.tedtalk.model.TedTalk;
import com.io.tedtalk.repository.TedTalkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TedTalkApiServiceTest {

    @Mock
    private TedTalkRepository tedTalkRepository;

    @InjectMocks
    private com.io.tedtalk.service.TedTalkApiService tedTalkService;

    private TedTalk tedTalk1;
    private TedTalk tedTalk2;

    @BeforeEach
    void setUp() {
        tedTalk1 = TedTalk.builder()
                .id(1L)
                .title("Testing Ted talk One")
                .author("Minna Shemeer")
                .year(2022)
                .month(2)
                .views(50000)
                .likes(25000)
                .link("https://www.ted.com/talks/minna_shemeer_testing_tedtalk_one")
                .build();

        tedTalk2 = TedTalk.builder()
                .id(2L)
                .title("My new school")
                .author("Hana Shemeer")
                .year(2019)
                .month(9)
                .views(45000)
                .likes(20000)
                .link("https://www.ted.com/talks/hana_shemeer_my_new_school")
                .build();
    }

    @Test
    void getAllTedTalks() {
        when(tedTalkRepository.findAll()).thenReturn(Arrays.asList(tedTalk1, tedTalk2));
        List<TedTalk> tedTalks = tedTalkService.getAllTedTalks();
        assertEquals(2, tedTalks.size());
        verify(tedTalkRepository, times(1)).findAll();
    }

    @Test
    void getAllTedTalks_WhenNoTalksExist_ShouldReturnEmptyList() {
        when(tedTalkRepository.findAll()).thenReturn(List.of());
        List<TedTalk> result = tedTalkService.getAllTedTalks();
        assertTrue(result.isEmpty());
        verify(tedTalkRepository, times(1)).findAll();
    }

    @Test
    void getTedTalkById_Success() {
        when(tedTalkRepository.findById(1L)).thenReturn(Optional.of(tedTalk1));

        TedTalk foundTedTalk = tedTalkService.getTedTalkById(1L);

        assertNotNull(foundTedTalk);
        assertEquals(tedTalk1.getTitle(), foundTedTalk.getTitle());
    }

    @Test
    void getTedTalkById_NotFound() {
        when(tedTalkRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> tedTalkService.getTedTalkById(99L));
    }

    @Test
    void createTedTalk() {
        when(tedTalkRepository.save(any(TedTalk.class))).thenReturn(tedTalk1);

        TedTalk savedTedTalk = tedTalkService.createTedTalk(tedTalk1);

        assertNotNull(savedTedTalk);
        assertEquals(tedTalk1.getTitle(), savedTedTalk.getTitle());
    }

    @Test
    void createTedTalk_shouldThrowException_whenRepositoryFails() {
        TedTalkRepository tedTalkRepository = mock(TedTalkRepository.class);
        TedTalkApiService tedTalkService = new TedTalkApiService(tedTalkRepository);

        TedTalk invalidTedTalk = new TedTalk();
        when(tedTalkRepository.save(any(TedTalk.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate link"));

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> tedTalkService.createTedTalk(invalidTedTalk)
        );

        assertEquals("Duplicate link", exception.getMessage());
    }

    @Test
    void updateTedTalk() {
        when(tedTalkRepository.findById(1L)).thenReturn(Optional.of(tedTalk1));
        when(tedTalkRepository.save(any(TedTalk.class))).thenReturn(tedTalk1);

        TedTalk updatedTedTalk = tedTalkService.updateTedTalk(1L, tedTalk1);

        assertNotNull(updatedTedTalk);
        assertEquals(tedTalk1.getTitle(), updatedTedTalk.getTitle());
    }

    @Test
    void deleteTedTalk() {
        when(tedTalkRepository.findById(1L)).thenReturn(Optional.of(tedTalk1));
        doNothing().when(tedTalkRepository).delete(tedTalk1);

        tedTalkService.deleteTedTalk(1L);

        verify(tedTalkRepository, times(1)).delete(tedTalk1);
    }

    @Test
    void searchByAuthor() {
        when(tedTalkRepository.findByAuthorContainingIgnoreCase("Minna")).thenReturn(Collections.singletonList(tedTalk1));

        List<TedTalk> result = tedTalkService.searchByAuthor("Minna");

        assertEquals(1, result.size());
        assertEquals(tedTalk1.getAuthor(), result.getFirst().getAuthor());
    }
    @Test
    void searchByAuthor_WithNoMatches_ShouldReturnEmptyList() {
        String searchTerm = "Nonexistent";
        when(tedTalkRepository.findByAuthorContainingIgnoreCase(searchTerm)).thenReturn(List.of());

        List<TedTalk> result = tedTalkService.searchByAuthor(searchTerm);

        assertTrue(result.isEmpty());
        verify(tedTalkRepository, times(1)).findByAuthorContainingIgnoreCase(searchTerm);
    }


    @Test
    void searchByTitle_WithNoMatches_ShouldReturnEmptyList() {
        String searchTerm = "Nonexistent";
        when(tedTalkRepository.findByTitleContainingIgnoreCase(searchTerm)).thenReturn(List.of());

        List<TedTalk> result = tedTalkService.searchByTitle(searchTerm);

        assertTrue(result.isEmpty());
        verify(tedTalkRepository, times(1)).findByTitleContainingIgnoreCase(searchTerm);
    }
    @Test
    void getTopInfluentialSpeakers_ShouldReturnSpeakers() {
        List<InfluencerDTO> expected = List.of(
                new InfluencerDTO("Hana Shemeer", 50000000L, 2500000L,75000000L )
        );
        when(tedTalkRepository.findTopInfluentialSpeakers()).thenReturn(expected);

        List<InfluencerDTO> result = tedTalkService.getTopInfluentialSpeakers();

        assertEquals(1, result.size());
        verify(tedTalkRepository, times(1)).findTopInfluentialSpeakers();
    }

    @Test
    void getTopInfluentialSpeakers_WhenNoData_ShouldReturnEmptyList() {
        when(tedTalkRepository.findTopInfluentialSpeakers()).thenReturn(List.of());

        List<InfluencerDTO> result = tedTalkService.getTopInfluentialSpeakers();

        assertTrue(result.isEmpty());
        verify(tedTalkRepository, times(1)).findTopInfluentialSpeakers();
    }

    @Test
    void getMostInfluentialTalksPerYear_ShouldReturnTalks() {
        List<MostInfluentialSpeakerDTO> expected = List.of(
                new MostInfluentialSpeakerDTO(2016, "Minna Shemeer", 40000000, 50000000,90000000L)
        );
        when(tedTalkRepository.findMostInfluentialTalksPerYear()).thenReturn(expected);

        List<MostInfluentialSpeakerDTO> result = tedTalkService.getMostInfluentialTalksPerYear();

        assertEquals(1, result.size());
        assertEquals(2016, result.getFirst().year());
        verify(tedTalkRepository, times(1)).findMostInfluentialTalksPerYear();
    }
}
