package com.io.tedtalk.repository;

import com.io.tedtalk.dto.InfluencerDTO;
import com.io.tedtalk.dto.MostInfluentialSpeakerDTO;
import com.io.tedtalk.model.TedTalk;
import jakarta.validation.constraints.Min;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TedTalkRepository extends JpaRepository<TedTalk, Long> {
    List<TedTalk> findByAuthorContainingIgnoreCase(String author);
    List<TedTalk> findByTitleContainingIgnoreCase(String title);
    Optional<TedTalk> findByLink(String link);
    @Query("""
    SELECT new com.io.tedtalk.dto.InfluencerDTO(
        t.author,
        SUM(t.views),
        SUM(t.likes),
        SUM(t.views + t.likes)
    )
    FROM TedTalk t
    GROUP BY t.author
    ORDER BY SUM(t.views + t.likes) DESC
""")
    List<InfluencerDTO> findTopInfluentialSpeakers();

    @Query("""
    SELECT new com.io.tedtalk.dto.MostInfluentialSpeakerDTO(
        t.year,
        t.author,
        t.views,
        t.likes,
        (t.views + t.likes)
    )
    FROM TedTalk t
    WHERE (t.views + t.likes) = (
        SELECT MAX(t2.views + t2.likes)
        FROM TedTalk t2
        WHERE t2.year = t.year
    )
    GROUP BY t.year, t.author, t.views, t.likes
    ORDER BY t.year ASC
""")
    List<MostInfluentialSpeakerDTO> findMostInfluentialTalksPerYear();

}
