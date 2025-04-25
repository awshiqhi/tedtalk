package com.io.tedtalk.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.io.tedtalk.dto.InfluencerDTO;
import com.io.tedtalk.dto.MostInfluentialSpeakerDTO;
import com.io.tedtalk.dto.TedTalkStatsUpdateDTO;
import com.io.tedtalk.model.TedTalk;
import com.io.tedtalk.service.TedTalkApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TedTalkControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TedTalkApiService tedTalkApiService;

    @InjectMocks
    private TedTalkApiController tedTalkApiController;

    private TedTalk sampleTalk;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tedTalkApiController).build();

        sampleTalk = new TedTalk();
        sampleTalk.setId(1L);
        sampleTalk.setTitle("The power of vulnerability");
        sampleTalk.setAuthor("Brené Brown");
        sampleTalk.setDate("December 2021");
        sampleTalk.setViews(50000000);
        sampleTalk.setLikes(2500000);

        InfluencerDTO sampleInfluencer = new InfluencerDTO("Brené Brown", 50000000, 2500000, 7500000);
        MostInfluentialSpeakerDTO sampleInfluentialSpeaker = new MostInfluentialSpeakerDTO(2010, "Brené Brown", 50000000, 2500000, 7500000);
        TedTalkStatsUpdateDTO statsUpdateDTO = new TedTalkStatsUpdateDTO(1000, 50);
    }
    @Test
    void getAllTedTalks_ShouldReturnListOfTalks() throws Exception {
        when(tedTalkApiService.getAllTedTalks()).thenReturn(List.of(sampleTalk));

        mockMvc.perform(get("/api/tedtalks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("The power of vulnerability"))
                .andExpect(jsonPath("$[0].author").value("Brené Brown"));

        verify(tedTalkApiService, times(1)).getAllTedTalks();
    }

    @Test
    void getAllTedTalks_WhenNoTalksExist_ShouldReturnEmptyList() throws Exception {
        when(tedTalkApiService.getAllTedTalks()).thenReturn(List.of());

        mockMvc.perform(get("/api/tedtalks"))
                .andExpect(status().isOk());

        verify(tedTalkApiService, times(1)).getAllTedTalks();
    }

    @Test
    void getTedTalkById_WithValidId_ShouldReturnTalk() throws Exception {
        when(tedTalkApiService.getTedTalkById(1L)).thenReturn(sampleTalk);

        mockMvc.perform(get("/api/tedtalks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("The power of vulnerability"));

        verify(tedTalkApiService, times(1)).getTedTalkById(1L);
    }

    @Test
    void getTedTalkById_WithInvalidId_ShouldReturn404() throws Exception {
        when(tedTalkApiService.getTedTalkById(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,"Ted Talk not found with id: 999"));

        mockMvc.perform(get("/api/tedtalks/999"))
                .andExpect(status().isNotFound());

        verify(tedTalkApiService, times(1)).getTedTalkById(999L);
    }

    @Test
    void createTedTalk_WithValidTalk_ShouldReturnCreatedTalk() throws Exception {
        when(tedTalkApiService.createTedTalk(any(TedTalk.class))).thenReturn(sampleTalk);

        mockMvc.perform(post("/api/tedtalks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTalk)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The power of vulnerability"));

        verify(tedTalkApiService, times(1)).createTedTalk(any(TedTalk.class));
    }

    @Test
    void createTedTalk_WithInvalidTalk_ShouldReturnBadRequest() throws Exception {

        String invalidDateJson = "{ \"title\": \"Test\", \"author\": \"Author\", \"date\": \"Invalid\" }";

        mockMvc.perform(post("/api/tedtalks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDateJson))
                .andExpect(status().isBadRequest());

    }

    @Test
    void updateTedTalk_WithValidIdAndTalk_ShouldReturnUpdatedTalk() throws Exception {
        when(tedTalkApiService.updateTedTalk(eq(1L), any(TedTalk.class))).thenReturn(sampleTalk);

        mockMvc.perform(put("/api/tedtalks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTalk)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(tedTalkApiService, times(1)).updateTedTalk(eq(1L), any(TedTalk.class));
    }

    @Test
    void updateTedTalk_WithInvalidId_ShouldReturnNotFound() throws Exception {
        when(tedTalkApiService.updateTedTalk(eq(999L), any(TedTalk.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,"Ted Talk not found with id: 999"));

        mockMvc.perform(put("/api/tedtalks/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTalk)))
                .andExpect(status().isNotFound());

        verify(tedTalkApiService, times(1)).updateTedTalk(eq(999L), any(TedTalk.class));
    }

    @Test
    void deleteTedTalk_WithValidId_ShouldReturnNoContent() throws Exception {
        doNothing().when(tedTalkApiService).deleteTedTalk(1L);

        mockMvc.perform(delete("/api/tedtalks/1"))
                .andExpect(status().isNoContent());

        verify(tedTalkApiService, times(1)).deleteTedTalk(1L);
    }

    @Test
    void deleteTedTalk_WithInvalidId_ShouldReturnNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ted Talk not found with id: 999"))
                .when(tedTalkApiService).deleteTedTalk(999L);

        mockMvc.perform(delete("/api/tedtalks/999"))
                .andExpect(status().isNotFound());

        verify(tedTalkApiService, times(1)).deleteTedTalk(999L);
    }
    @Test
    void searchByAuthor_WithMatchingAuthor_ShouldReturnTalks() throws Exception {
        when(tedTalkApiService.searchByAuthor("Brown")).thenReturn(List.of(sampleTalk));

        mockMvc.perform(get("/api/tedtalks/search/author")
                        .param("author", "Brown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].author").value("Brené Brown"));

        verify(tedTalkApiService, times(1)).searchByAuthor("Brown");
    }

    @Test
    void searchByAuthor_WithNoMatches_ShouldReturnEmptyList() throws Exception {
        when(tedTalkApiService.searchByAuthor("Nonexistent")).thenReturn(List.of());

        mockMvc.perform(get("/api/tedtalks/search/author")
                        .param("author", "Nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(tedTalkApiService, times(1)).searchByAuthor("Nonexistent");
    }

    @Test
    void searchByAuthor_WithMissingParam_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/tedtalks/search/author"))
                .andExpect(status().isBadRequest());

        verify(tedTalkApiService, never()).searchByAuthor(any());
    }
}