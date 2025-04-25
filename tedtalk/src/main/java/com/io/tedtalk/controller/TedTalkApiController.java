package com.io.tedtalk.controller;

import com.io.tedtalk.dto.InfluencerDTO;
import com.io.tedtalk.dto.MostInfluentialSpeakerDTO;
import com.io.tedtalk.dto.TedTalkStatsUpdateDTO;
import com.io.tedtalk.model.TedTalk;
import com.io.tedtalk.service.TedTalkApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tedtalks")
@RequiredArgsConstructor
public class TedTalkApiController {
    private final TedTalkApiService tedTalkApiService;

    @Operation(summary = "Get all TED Talks",
            description = "Retrieve a list of all TED Talks in the database. Returns a list of TED Talk objects.")
    @GetMapping
    public ResponseEntity<List<TedTalk>> getAllTedTalks() {
        List<TedTalk> talks = tedTalkApiService.getAllTedTalks();
        return ResponseEntity.ok(talks);
    }

    @Operation(summary = "Get TED Talk by ID",
            description = "Retrieve the details of a TED Talk by its ID. Returns the TED Talk object with the specified ID. If not found, returns a 404 error.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the TED Talk"),
            @ApiResponse(responseCode = "404", description = "TED Talk not found with the provided ID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TedTalk> getTedTalkById(@PathVariable Long id) {
        return ResponseEntity.ok(tedTalkApiService.getTedTalkById(id));
    }

    @Operation(summary = "Create a new TED Talk",
            description = "Create a new TED Talk by providing the necessary details in the request body. This endpoint will save the TED Talk to the database and return the created TED Talk object.")

    @PostMapping
    public ResponseEntity<TedTalk> createTedTalk(@RequestBody TedTalk tedTalk) {
        return ResponseEntity.ok(tedTalkApiService.createTedTalk(tedTalk));
    }

    @Operation(summary = "Update TED Talk by ID",
            description = "Update the details of a TED Talk identified by its ID. The request body should contain the updated TED Talk details. Returns the updated TED Talk object. If the TED Talk is not found, a 404 error is returned.")
    @PutMapping("/{id}")
    public ResponseEntity<TedTalk> updateTedTalk(@PathVariable Long id, @RequestBody TedTalk tedTalk) {
        return ResponseEntity.ok(tedTalkApiService.updateTedTalk(id, tedTalk));
    }

    @Operation(summary = "Delete TED Talk by ID",
            description = "Delete a TED Talk identified by its ID. If the TED Talk with the provided ID exists, it will be deleted. If not found, a 404 error will be returned.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTedTalk(@PathVariable Long id) {
        tedTalkApiService.deleteTedTalk(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search TED Talks by author",
            description = "Search for TED Talks by a specific author. The search returns all TED Talks where the author name contains the given text.")
    @GetMapping("/search/author")
    public ResponseEntity<List<TedTalk>> searchByAuthor(@RequestParam String author) {
        return ResponseEntity.ok(tedTalkApiService.searchByAuthor(author));
    }

    @Operation(summary = "Search TED Talks by title",
            description = "Search for TED Talks by title. The search returns all TED Talks that contain the provided title substring.")
    @GetMapping("/search/title")
    public ResponseEntity<List<TedTalk>> searchByTitle(@RequestParam String title) {
        return ResponseEntity.ok(tedTalkApiService.searchByTitle(title));
    }

    @Operation(
            summary = "Get influential TED Talk speakers",
            description = "Returns a ranked list of speakers based on total views and likes across all talks."
    )
    @GetMapping("/influencers")
    public ResponseEntity<List<InfluencerDTO>> getInfluentialSpeakers() {
        return ResponseEntity.ok(tedTalkApiService.getTopInfluentialSpeakers());
    }

    @Operation(
            summary = "Get most influential TED Talk per year",
            description = "Returns the single most influential talk per year, ranked by views + likes."
    )
    @GetMapping("/influencers/per-year")
    public ResponseEntity<List<MostInfluentialSpeakerDTO>> getMostInfluentialPerYear() {
        return ResponseEntity.ok(tedTalkApiService.getMostInfluentialTalksPerYear());
    }

    @Operation(summary = "Update views and likes for a TedTalk")
    @PatchMapping("/{id}/stats")
    public ResponseEntity<String> updateStats(@PathVariable Long id, @RequestBody TedTalkStatsUpdateDTO dto) {
        return tedTalkApiService.updateViewsAndLikes(id, dto)
                .map(updatedTalk -> ResponseEntity.ok("Updated views/likes for TedTalk with ID " + id))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("TedTalk not found with ID " + id));
    }


}
