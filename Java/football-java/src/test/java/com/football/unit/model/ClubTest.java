package com.football.unit.model;

import com.football.model.Club;
import com.football.model.Competition;
import com.football.model.TrendDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Club entity.
 */
class ClubTest {

    private Club club;

    @BeforeEach
    void setUp() {
        club = new Club("Real Madrid", "Spain", 1, 1000);
        club.setPreviousPoints(950);
        club.setTrend(TrendDirection.UP);
    }

    @Test
    @DisplayName("Should calculate points change correctly")
    void shouldCalculatePointsChange() {
        assertThat(club.getPointsChange()).isEqualTo(50);
    }

    @Test
    @DisplayName("Should detect improved status when trend is UP")
    void shouldDetectImproved() {
        assertThat(club.isImproved()).isTrue();
        assertThat(club.isDeclined()).isFalse();
    }

    @Test
    @DisplayName("Should detect declined status when trend is DOWN")
    void shouldDetectDeclined() {
        club.setTrend(TrendDirection.DOWN);
        assertThat(club.isDeclined()).isTrue();
        assertThat(club.isImproved()).isFalse();
    }

    @Test
    @DisplayName("Should track opponents correctly")
    void shouldTrackOpponents() {
        Club opponent1 = new Club("Bayern", "Germany", 2, 900);
        Club opponent2 = new Club("Liverpool", "England", 3, 850);

        club.addHomeOpponent(opponent1);
        club.addAwayOpponent(opponent2);

        assertThat(club.getHomeOpponents()).containsExactly(opponent1);
        assertThat(club.getAwayOpponents()).containsExactly(opponent2);
        assertThat(club.getAllOpponents()).containsExactlyInAnyOrder(opponent1, opponent2);
    }

    @Test
    @DisplayName("Should count opponents by country")
    void shouldCountOpponentsByCountry() {
        Club german1 = new Club("Bayern", "Germany", 2, 900);
        Club german2 = new Club("Dortmund", "Germany", 4, 800);
        Club english = new Club("Liverpool", "England", 3, 850);

        club.addHomeOpponent(german1);
        club.addHomeOpponent(german2);
        club.addAwayOpponent(english);

        assertThat(club.getOpponentCountries())
                .containsEntry("Germany", 2)
                .containsEntry("England", 1);
    }

    @Test
    @DisplayName("Should not allow facing same country club")
    void shouldNotAllowSameCountry() {
        Club spanishOpponent = new Club("Barcelona", "Spain", 5, 750);

        assertThat(club.canFace(spanishOpponent, 2)).isFalse();
    }

    @Test
    @DisplayName("Should not allow facing same club twice")
    void shouldNotAllowDuplicateOpponent() {
        Club opponent = new Club("Bayern", "Germany", 2, 900);
        club.addHomeOpponent(opponent);

        assertThat(club.canFace(opponent, 2)).isFalse();
    }

    @Test
    @DisplayName("Should enforce max same country limit")
    void shouldEnforceMaxSameCountry() {
        Club german1 = new Club("Bayern", "Germany", 2, 900);
        Club german2 = new Club("Dortmund", "Germany", 4, 800);
        Club german3 = new Club("Leipzig", "Germany", 6, 700);

        club.addHomeOpponent(german1);
        club.addHomeOpponent(german2);

        // Max 2 from same country
        assertThat(club.canFace(german3, 2)).isFalse();
    }

    @Test
    @DisplayName("Should reset draw state correctly")
    void shouldResetDrawState() {
        club.addHomeOpponent(new Club("Bayern", "Germany", 2, 900));
        club.addAwayOpponent(new Club("Liverpool", "England", 3, 850));

        club.resetDraw();

        assertThat(club.getHomeOpponents()).isEmpty();
        assertThat(club.getAwayOpponents()).isEmpty();
    }

    @Test
    @DisplayName("Should check completion status")
    void shouldCheckCompletion() {
        Competition comp = Competition.CHAMPIONS_LEAGUE;

        assertThat(club.isComplete(comp)).isFalse();

        // Add 4 home and 4 away opponents
        for (int i = 0; i < 4; i++) {
            club.addHomeOpponent(new Club("Home" + i, "Country" + i, i, 100));
            club.addAwayOpponent(new Club("Away" + i, "OtherCountry" + i, i, 100));
        }

        assertThat(club.isComplete(comp)).isTrue();
    }

    @Test
    @DisplayName("Should implement equals and hashCode based on name and country")
    void shouldImplementEqualsAndHashCode() {
        Club same = new Club("Real Madrid", "Spain", 10, 500);
        Club different = new Club("Barcelona", "Spain", 2, 900);

        assertThat(club).isEqualTo(same);
        assertThat(club).isNotEqualTo(different);
        assertThat(club.hashCode()).isEqualTo(same.hashCode());
    }
}
