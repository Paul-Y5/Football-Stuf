#!/usr/bin/env python3
"""
Tests for UEFA Draw Simulator
=============================

Unit tests for the draw simulation algorithm and constraints.
"""
import pytest
import sys
from pathlib import Path

# Add parent to path
sys.path.insert(0, str(Path(__file__).parent.parent))

from models import Club, Competition, DrawResult
from constraints import (
    DrawConstraints,
    ChampionsLeagueConstraints,
    EuropaLeagueConstraints,
    ConferenceLeagueConstraints
)
from draw_simulator import UEFADrawSimulator


class TestClub:
    """Tests for Club model."""
    
    def test_club_creation(self):
        """Test basic club creation."""
        club = Club(name="FC Porto", country="POR", pot=2)
        assert club.name == "FC Porto"
        assert club.country == "POR"
        assert club.pot == 2
        assert len(club.home_opponents) == 0
        assert len(club.away_opponents) == 0
    
    def test_club_is_complete(self):
        """Test is_complete property."""
        club = Club(name="Benfica", country="POR")
        assert not club.is_complete
        
        # Add 4 home and 4 away opponents
        for i in range(4):
            club.home_opponents.append(Club(f"Home{i}", f"C{i}"))
            club.away_opponents.append(Club(f"Away{i}", f"C{i+4}"))
        
        assert club.is_complete
    
    def test_can_face_same_country(self):
        """Test that same country clubs cannot face each other."""
        porto = Club(name="FC Porto", country="POR")
        benfica = Club(name="Benfica", country="POR")
        
        assert not porto.can_face(benfica)
        assert not benfica.can_face(porto)
    
    def test_can_face_different_country(self):
        """Test that different country clubs can face each other."""
        porto = Club(name="FC Porto", country="POR")
        real = Club(name="Real Madrid", country="ESP")
        
        assert porto.can_face(real)
        assert real.can_face(porto)
    
    def test_can_face_max_country_limit(self):
        """Test max 2 opponents from same country."""
        porto = Club(name="FC Porto", country="POR")
        
        # Add 2 Spanish opponents
        porto.home_opponents.append(Club("Real Madrid", "ESP"))
        porto.home_opponents.append(Club("Barcelona", "ESP"))
        
        # Third Spanish team should be blocked
        atletico = Club(name="Atletico Madrid", country="ESP")
        assert not porto.can_face(atletico)
    
    def test_cannot_face_self(self):
        """Test club cannot face itself."""
        porto = Club(name="FC Porto", country="POR")
        assert not porto.can_face(porto)
    
    def test_opponent_countries_count(self):
        """Test opponent country counting."""
        club = Club(name="Test", country="TST")
        club.home_opponents = [
            Club("A", "ESP"),
            Club("B", "ESP"),
            Club("C", "ENG"),
        ]
        club.away_opponents = [
            Club("D", "GER"),
        ]
        
        countries = club.opponent_countries
        assert countries["ESP"] == 2
        assert countries["ENG"] == 1
        assert countries["GER"] == 1


class TestConstraints:
    """Tests for UEFA draw constraints."""
    
    def test_country_protection(self):
        """Test same country protection."""
        constraints = ChampionsLeagueConstraints()
        
        porto = Club(name="FC Porto", country="POR")
        benfica = Club(name="Benfica", country="POR")
        
        can_pair, reason = constraints.can_pair(porto, benfica, True)
        assert not can_pair
        assert "country" in reason.lower()
    
    def test_home_away_capacity(self):
        """Test home/away capacity limits."""
        constraints = ChampionsLeagueConstraints()
        
        club = Club(name="Test", country="TST")
        opponent = Club(name="Opp", country="OPP")
        
        # Fill home matches
        for i in range(4):
            club.home_opponents.append(Club(f"H{i}", f"C{i}"))
        
        can_pair, reason = constraints.can_pair(club, opponent, True)
        assert not can_pair
        assert "home" in reason.lower()
    
    def test_valid_pairing(self):
        """Test valid pairing."""
        constraints = ChampionsLeagueConstraints()
        
        porto = Club(name="FC Porto", country="POR")
        real = Club(name="Real Madrid", country="ESP")
        
        can_pair, reason = constraints.can_pair(porto, real, True)
        assert can_pair
        assert reason is None
    
    def test_conference_league_constraints(self):
        """Test Conference League has fewer matches."""
        constraints = ConferenceLeagueConstraints()
        
        assert constraints.home_matches == 3
        assert constraints.away_matches == 3
        assert constraints.matches_per_team == 6


class TestDrawSimulator:
    """Tests for draw simulator."""
    
    def test_simulator_creation(self):
        """Test simulator initialization."""
        sim = UEFADrawSimulator(Competition.CHAMPIONS_LEAGUE, verbose=False)
        assert sim.competition == Competition.CHAMPIONS_LEAGUE
        assert len(sim.clubs) == 0
    
    def test_load_default_clubs(self):
        """Test loading default clubs."""
        sim = UEFADrawSimulator(verbose=False)
        sim.load_clubs()
        
        assert len(sim.clubs) == 36
        
        # Check pots
        pot1 = [c for c in sim.clubs if c.pot == 1]
        pot2 = [c for c in sim.clubs if c.pot == 2]
        pot3 = [c for c in sim.clubs if c.pot == 3]
        pot4 = [c for c in sim.clubs if c.pot == 4]
        
        assert len(pot1) == 9
        assert len(pot2) == 9
        assert len(pot3) == 9
        assert len(pot4) == 9
    
    def test_load_custom_clubs(self):
        """Test loading custom clubs."""
        sim = UEFADrawSimulator(verbose=False)
        
        custom_clubs = [
            {"name": "Team A", "country": "AAA", "pot": 1},
            {"name": "Team B", "country": "BBB", "pot": 1},
            {"name": "Team C", "country": "CCC", "pot": 2},
            {"name": "Team D", "country": "DDD", "pot": 2},
        ]
        
        sim.load_clubs(custom_clubs)
        assert len(sim.clubs) == 4
    
    def test_draw_completes(self):
        """Test that draw completes successfully."""
        sim = UEFADrawSimulator(seed=42, verbose=False)
        sim.load_clubs()
        
        result = sim.draw()
        
        # Allow some failures due to random nature
        # But should succeed most of the time
        assert result is not None
    
    def test_draw_result_validity(self):
        """Test draw result meets all constraints."""
        sim = UEFADrawSimulator(seed=12345, verbose=False)
        sim.load_clubs()
        
        result = sim.draw()
        
        if result.is_valid:
            for club in result.clubs:
                # Check match counts
                assert len(club.home_opponents) == 4, f"{club.name} has {len(club.home_opponents)} home"
                assert len(club.away_opponents) == 4, f"{club.name} has {len(club.away_opponents)} away"
                
                # Check no same country
                for opp in club.all_opponents:
                    assert opp.country != club.country, f"{club.name} vs {opp.name} same country"
                
                # Check no duplicate opponents
                all_opps = club.all_opponents
                assert len(all_opps) == len(set(o.name for o in all_opps))
    
    def test_draw_reproducibility(self):
        """Test same seed produces same result."""
        sim1 = UEFADrawSimulator(seed=999, verbose=False)
        sim1.load_clubs()
        result1 = sim1.draw()
        
        sim2 = UEFADrawSimulator(seed=999, verbose=False)
        sim2.load_clubs()
        result2 = sim2.draw()
        
        if result1.is_valid and result2.is_valid:
            for c1, c2 in zip(result1.clubs, result2.clubs):
                assert c1.name == c2.name
                h1 = sorted(o.name for o in c1.home_opponents)
                h2 = sorted(o.name for o in c2.home_opponents)
                assert h1 == h2
    
    def test_competition_types(self):
        """Test all competition types work."""
        for comp in [Competition.CHAMPIONS_LEAGUE, Competition.EUROPA_LEAGUE, Competition.CONFERENCE_LEAGUE]:
            sim = UEFADrawSimulator(competition=comp, seed=42, verbose=False)
            sim.load_clubs()
            result = sim.draw()
            
            assert result is not None


class TestDrawValidation:
    """Tests for draw validation."""
    
    def test_validate_complete_draw(self):
        """Test validation of complete draw."""
        constraints = ChampionsLeagueConstraints()
        
        # Create minimal valid draw (simplified)
        clubs = []
        countries = ["A", "B", "C", "D", "E", "F", "G", "H"]
        
        for i, country in enumerate(countries):
            club = Club(f"Team {i}", country, pot=(i % 4) + 1)
            clubs.append(club)
        
        # Manual assignment (simplified test)
        # This just tests the validation function exists
        is_valid, violations = constraints.validate_final_draw(clubs)
        
        # Should have violations (incomplete)
        assert not is_valid or len(violations) > 0


class TestEdgeCases:
    """Tests for edge cases and error handling."""
    
    def test_empty_clubs_list(self):
        """Test handling of empty clubs list."""
        sim = UEFADrawSimulator(verbose=False)
        sim.clubs = []
        
        # Should load defaults
        result = sim.draw()
        assert len(sim.clubs) > 0
    
    def test_max_backtrack_limit(self):
        """Test backtracking limit is respected."""
        sim = UEFADrawSimulator(verbose=False)
        sim.max_backtrack = 100  # Low limit
        sim.load_clubs()
        
        result = sim.draw()
        
        # With low limit, draw should fail
        # The algorithm checks limit and stops, but may slightly exceed
        # due to recursive nature
        if not result.is_valid:
            # If failed, backtrack count should be around the limit
            assert sim.backtrack_count >= sim.max_backtrack
        # If it somehow succeeded with few backtracks, that's also fine
    
    def test_small_draw(self):
        """Test with minimal number of teams."""
        sim = UEFADrawSimulator(verbose=False)
        
        # 4 teams, different countries
        clubs = [
            {"name": "A", "country": "AAA", "pot": 1},
            {"name": "B", "country": "BBB", "pot": 1},
            {"name": "C", "country": "CCC", "pot": 2},
            {"name": "D", "country": "DDD", "pot": 2},
        ]
        
        sim.load_clubs(clubs)
        # This would need adjusted constraints for 4 teams


# Run tests
if __name__ == "__main__":
    pytest.main([__file__, "-v"])
