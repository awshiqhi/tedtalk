package com.io.tedtalk.service;

import com.io.tedtalk.dto.InfluencerDTO;
import com.io.tedtalk.dto.MostInfluentialSpeakerDTO;
import com.io.tedtalk.dto.TedTalkStatsUpdateDTO;
import com.io.tedtalk.model.TedTalk;
import com.io.tedtalk.repository.TedTalkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TedTalkApiService {
    private final TedTalkRepository tedTalkRepository;

    public List<TedTalk> getAllTedTalks() {
        return tedTalkRepository.findAll();
    }

    public TedTalk getTedTalkById(Long id) {
        return tedTalkRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Ted Talk not found with id: " + id));
    }

    public TedTalk createTedTalk(TedTalk tedTalk) {
        return tedTalkRepository.save(tedTalk);
    }

    public TedTalk updateTedTalk(Long id, TedTalk tedTalkDetails) {
        TedTalk tedTalk = getTedTalkById(id);

        tedTalk.setTitle(tedTalkDetails.getTitle());
        tedTalk.setAuthor(tedTalkDetails.getAuthor());
        tedTalk.setYear(tedTalkDetails.getYear());
        tedTalk.setMonth(tedTalkDetails.getMonth());
        tedTalk.setViews(tedTalkDetails.getViews());
        tedTalk.setLikes(tedTalkDetails.getLikes());
        tedTalk.setLink(tedTalkDetails.getLink());

        return tedTalkRepository.save(tedTalk);
    }

    public void deleteTedTalk(Long id) {
        TedTalk tedTalk = getTedTalkById(id);
        tedTalkRepository.delete(tedTalk);
    }

    public List<TedTalk> searchByAuthor(String author) {
        return tedTalkRepository.findByAuthorContainingIgnoreCase(author);
    }

    public List<TedTalk> searchByTitle(String title) {
        return tedTalkRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<InfluencerDTO> getTopInfluentialSpeakers() {
        return tedTalkRepository.findTopInfluentialSpeakers();
    }

    public List<MostInfluentialSpeakerDTO> getMostInfluentialTalksPerYear() {
        return tedTalkRepository.findMostInfluentialTalksPerYear();
    }

    public Optional<TedTalk> updateViewsAndLikes(Long id, TedTalkStatsUpdateDTO dto) {
        return tedTalkRepository.findById(id).map(talk -> {
            if (dto.views() != null) {
                talk.setViews(dto.views());
            }
            if (dto.likes() != null) {
                talk.setLikes(dto.likes());
            }
            return tedTalkRepository.save(talk);
        });
    }

}
