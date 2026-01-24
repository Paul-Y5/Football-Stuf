package com.football.repository;

import com.football.model.Club;
import com.football.model.TrendDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ClubRepository.
 */
@DataJpaTest
class ClubRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClubRepository clubRepository;

    private Club barcelona;
    private Club realMadrid;
    private Club manchesterCity;

    @BeforeEach
    void setUp() {
        // Spanish clubs
        barcelona = createClub("FC Barcelona", "Spain", 1, 2000, TrendDirection.UP);
        realMadrid = createClub("Real Madrid", "Spain", 2, 1950, TrendDirection.STABLE);
        
        // English club
        manchesterCity = createClub("Manchester City", "England", 3, 1900, TrendDirection.DOWN);

        entityManager.persist(barcelona);
        entityManager.persist(realMadrid);
        entityManager.persist(manchesterCity);
        entityManager.flush();
    }

    private Club createClub(String name, String country, int ranking, int points, TrendDirection trend) {
        Club club = new Club();
        club.setName(name);
        club.setCountry(country);
        club.setRanking(ranking);
        club.setPoints(points);
        club.setPreviousPoints(points - 50);
        club.setTrend(trend);
        return club;
    }

    @Test
    void whenFindByNameIgnoreCase_thenReturnClub() {
        // when
        Optional<Club> found = clubRepository.findByNameIgnoreCase("fc barcelona");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("FC Barcelona");
    }

    @Test
    void whenFindByNameIgnoreCase_withNonExisting_thenReturnEmpty() {
        // when
        Optional<Club> found = clubRepository.findByNameIgnoreCase("Liverpool");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void whenFindByCountryOrderByRankingAsc_thenReturnClubsInOrder() {
        // when
        List<Club> spanishClubs = clubRepository.findByCountryOrderByRankingAsc("Spain");

        // then
        assertThat(spanishClubs).hasSize(2);
        assertThat(spanishClubs.get(0).getName()).isEqualTo("FC Barcelona");
        assertThat(spanishClubs.get(1).getName()).isEqualTo("Real Madrid");
    }

    @Test
    void whenFindByCountry_withPagination_thenReturnPage() {
        // when
        Page<Club> page = clubRepository.findByCountry("Spain", PageRequest.of(0, 1));

        // then
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void whenFindTop100ByOrderByRankingAsc_thenReturnTopClubs() {
        // when
        List<Club> topClubs = clubRepository.findTop100ByOrderByRankingAsc();

        // then
        assertThat(topClubs).hasSize(3);
        assertThat(topClubs.get(0).getRanking()).isEqualTo(1);
        assertThat(topClubs.get(2).getRanking()).isEqualTo(3);
    }

    @Test
    void whenSearchByNameOrCountry_thenReturnMatches() {
        // when - search by name
        List<Club> nameResults = clubRepository.searchByNameOrCountry("Barcelona");
        
        // then
        assertThat(nameResults).hasSize(1);
        assertThat(nameResults.get(0).getName()).isEqualTo("FC Barcelona");

        // when - search by country
        List<Club> countryResults = clubRepository.searchByNameOrCountry("England");
        
        // then
        assertThat(countryResults).hasSize(1);
        assertThat(countryResults.get(0).getCountry()).isEqualTo("England");
    }

    @Test
    void whenFindByPointsBetweenOrderByPointsDesc_thenReturnClubsInRange() {
        // when
        List<Club> clubs = clubRepository.findByPointsBetweenOrderByPointsDesc(1900, 2000);

        // then
        assertThat(clubs).hasSize(3);
        assertThat(clubs.get(0).getPoints()).isGreaterThanOrEqualTo(clubs.get(1).getPoints());
    }

    @Test
    void whenCountByCountry_thenReturnCount() {
        // when
        long count = clubRepository.countByCountry("Spain");

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void whenFindDistinctCountries_thenReturnUniqueCountries() {
        // when
        List<String> countries = clubRepository.findDistinctCountries();

        // then
        assertThat(countries).hasSize(2);
        assertThat(countries).containsExactlyInAnyOrder("England", "Spain");
    }

    @Test
    void whenFindImprovedClubs_thenReturnOnlyUpTrend() {
        // when
        List<Club> improved = clubRepository.findImprovedClubs();

        // then
        assertThat(improved).hasSize(1);
        assertThat(improved.get(0).getName()).isEqualTo("FC Barcelona");
        assertThat(improved.get(0).getTrend()).isEqualTo(TrendDirection.UP);
    }

    @Test
    void whenFindDeclinedClubs_thenReturnOnlyDownTrend() {
        // when
        List<Club> declined = clubRepository.findDeclinedClubs();

        // then
        assertThat(declined).hasSize(1);
        assertThat(declined.get(0).getName()).isEqualTo("Manchester City");
        assertThat(declined.get(0).getTrend()).isEqualTo(TrendDirection.DOWN);
    }

    @Test
    void whenExistsByNameIgnoreCase_thenReturnTrue() {
        // when
        boolean exists = clubRepository.existsByNameIgnoreCase("FC BARCELONA");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void whenExistsByNameIgnoreCase_withNonExisting_thenReturnFalse() {
        // when
        boolean exists = clubRepository.existsByNameIgnoreCase("Liverpool");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void whenSaveAndRetrieve_thenTimestampsAreSet() {
        // given
        Club newClub = createClub("Bayern Munich", "Germany", 4, 1850, TrendDirection.UP);

        // when
        Club saved = clubRepository.save(newClub);
        entityManager.flush();
        Club found = clubRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }
}
