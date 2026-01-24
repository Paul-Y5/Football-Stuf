package com.football.unit.model;

import com.football.model.Club;
import com.football.model.CountryStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Unit tests for CountryStats.
 */
class CountryStatsTest {

    private CountryStats stats;

    @BeforeEach
    void setUp() {
        stats = new CountryStats("Germany");
        stats.addClub(new Club("Bayern", "Germany", 2, 1000));
        stats.addClub(new Club("Dortmund", "Germany", 10, 800));
        stats.addClub(new Club("Leipzig", "Germany", 20, 600));
        stats.addClub(new Club("Leverkusen", "Germany", 30, 500));
    }

    @Test
    @DisplayName("Should return correct total clubs")
    void shouldReturnTotalClubs() {
        assertThat(stats.getTotalClubs()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should calculate average ranking")
    void shouldCalculateAvgRanking() {
        // (2 + 10 + 20 + 30) / 4 = 15.5
        assertThat(stats.getAvgRanking()).isEqualTo(15.5);
    }

    @Test
    @DisplayName("Should calculate average points")
    void shouldCalculateAvgPoints() {
        // (1000 + 800 + 600 + 500) / 4 = 725
        assertThat(stats.getAvgPoints()).isEqualTo(725.0);
    }

    @Test
    @DisplayName("Should calculate standard deviation")
    void shouldCalculateStdDev() {
        // sqrt(((1000-725)^2 + (800-725)^2 + (600-725)^2 + (500-725)^2) / 4) ≈ 192
        assertThat(stats.getStdDevPoints()).isCloseTo(192.0, within(1.0));
    }

    @Test
    @DisplayName("Should return best club")
    void shouldReturnBestClub() {
        assertThat(stats.getBestClub())
                .isPresent()
                .hasValueSatisfying(club -> assertThat(club.getName()).isEqualTo("Bayern"));
    }

    @Test
    @DisplayName("Should return worst club")
    void shouldReturnWorstClub() {
        assertThat(stats.getWorstClub())
                .isPresent()
                .hasValueSatisfying(club -> assertThat(club.getName()).isEqualTo("Leverkusen"));
    }

    @Test
    @DisplayName("Should calculate median ranking")
    void shouldCalculateMedianRanking() {
        // Sorted rankings: 2, 10, 20, 30 -> median = (10 + 20) / 2 = 15
        assertThat(stats.getMedianRanking()).isEqualTo(15.0);
    }

    @Test
    @DisplayName("Should count top N clubs")
    void shouldCountTopN() {
        assertThat(stats.getTop10Count()).isEqualTo(2); // Bayern + Dortmund
        assertThat(stats.getTop50Count()).isEqualTo(4); // All
        assertThat(stats.getTop100Count()).isEqualTo(4); // All
    }

    @Test
    @DisplayName("Should return top N clubs sorted")
    void shouldReturnTopNSorted() {
        List<Club> top2 = stats.getTopN(2);

        assertThat(top2).hasSize(2);
        assertThat(top2.get(0).getName()).isEqualTo("Bayern");
        assertThat(top2.get(1).getName()).isEqualTo("Dortmund");
    }

    @Test
    @DisplayName("Should handle empty country stats")
    void shouldHandleEmptyStats() {
        CountryStats empty = new CountryStats("Empty");

        assertThat(empty.getTotalClubs()).isZero();
        assertThat(empty.getAvgRanking()).isZero();
        assertThat(empty.getAvgPoints()).isZero();
        assertThat(empty.getStdDevPoints()).isZero();
        assertThat(empty.getBestClub()).isEmpty();
        assertThat(empty.getWorstClub()).isEmpty();
    }

    @Test
    @DisplayName("Should handle single club country")
    void shouldHandleSingleClub() {
        CountryStats single = new CountryStats("Portugal");
        single.addClub(new Club("Porto", "Portugal", 5, 700));

        assertThat(single.getStdDevPoints()).isZero();
        assertThat(single.getMedianRanking()).isEqualTo(5.0);
    }
}
