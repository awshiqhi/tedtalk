package com.io.tedtalk.repository;

import com.io.tedtalk.dto.InfluencerDTO;
import com.io.tedtalk.model.TedTalk;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TedTalkRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TedTalkRepository tedTalkRepository;

    @Test
    void findByLink_shouldReturnTedTalkWhenExists() {
        TedTalk talk = new TedTalk("Test Title", "Test Author", "June 2023", 1000, 100, "http://example.com");
        entityManager.persist(talk);
        entityManager.flush();

        Optional<TedTalk> found = tedTalkRepository.findByLink("http://example.com");

        assertTrue(found.isPresent());
        assertEquals("Test Title", found.get().getTitle());
    }

    @Test
    void findTopInfluentialSpeakers_shouldReturnCorrectResults() {
        TedTalk talk1 = new TedTalk("Talk 1", "Author A", "June 2023", 1000, 100, "http://example.com/1");
        TedTalk talk2 = new TedTalk("Talk 2", "Author A", "July 2023", 2000, 200, "http://example.com/2");
        TedTalk talk3 = new TedTalk("Talk 3", "Author B", "August 2023", 500, 50, "http://example.com/3");

        entityManager.persist(talk1);
        entityManager.persist(talk2);
        entityManager.persist(talk3);
        entityManager.flush();

        List<InfluencerDTO> result = tedTalkRepository.findTopInfluentialSpeakers();

        assertEquals(2, result.size());
        assertEquals("Author A", result.getFirst().speaker());
        assertEquals(3000L, result.getFirst().totalViews()); // 1000 + 2000
    }
}