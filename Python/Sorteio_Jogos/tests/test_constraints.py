#!/usr/bin/env python3
"""
Tests for Constraints Module
============================
"""
import pytest
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent))

from models import Club, Competition
from constraints import (
    DrawConstraints,
    ChampionsLeagueConstraints,
    EuropaLeagueConstraints,
    ConferenceLeagueConstraints
)


class TestChampionsLeagueConstraints:
    """Tests for Champions League specific rules."""
    
    @pytest.fixture
    def constraints(self):
        return ChampionsLeagueConstraints()
    
    @pytest.fixture
    def portuguese_clubs(self):
        return [
            Club("FC Porto", "POR", pot=3),
            Club("Benfica", "POR", pot=2),
            Club("Sporting CP", "POR", pot=2),
        ]
    
    def test_same_country_blocked(self, constraints, portuguese_clubs):
        """Portuguese teams cannot face each other."""
        porto, benfica, sporting = portuguese_clubs
        
        can_pair, reason = constraints.can_pair(porto, benfica, True)
        assert not can_pair
        
        can_pair, reason = constraints.can_pair(benfica, sporting, False)
        assert not can_pair
    
    def test_max_same_country_opponents(self, constraints):
        """Max 2 opponents from same country."""
        club = Club("Test", "TST")
        
        # Add 2 Spanish opponents
        spanish1 = Club("Spanish1", "ESP")
        spanish2 = Club("Spanish2", "ESP")
        spanish3 = Club("Spanish3", "ESP")
        
        club.home_opponents = [spanish1, spanish2]
        
        can_pair, reason = constraints.can_pair(club, spanish3, True)
        assert not can_pair
        assert "ESP" in reason
    
    def test_home_capacity_limit(self, constraints):
        """Cannot add more than 4 home matches."""
        club = Club("Test", "TST")
        opponent = Club("Opp", "OPP")
        
        # Fill 4 home matches
        for i in range(4):
            club.home_opponents.append(Club(f"H{i}", f"C{i}"))
        
        can_pair, reason = constraints.can_pair(club, opponent, as_home=True)
        assert not can_pair
        assert "home" in reason.lower()
    
    def test_away_capacity_limit(self, constraints):
        """Cannot add more than 4 away matches."""
        club = Club("Test", "TST")
        opponent = Club("Opp", "OPP")
        
        # Fill 4 away matches
        for i in range(4):
            club.away_opponents.append(Club(f"A{i}", f"C{i}"))
        
        can_pair, reason = constraints.can_pair(club, opponent, as_home=False)
        assert not can_pair
        assert "away" in reason.lower()
    
    def test_already_opponents_blocked(self, constraints):
        """Cannot face same team twice."""
        club = Club("Test", "TST")
        opponent = Club("Opp", "OPP")
        
        club.home_opponents.append(opponent)
        
        can_pair, reason = constraints.can_pair(club, opponent, False)
        assert not can_pair
        assert "already" in reason.lower()
    
    def test_valid_opponents_filtering(self, constraints):
        """Get only valid opponents."""
        club = Club("Test", "TST")
        
        candidates = [
            Club("Valid1", "AAA"),
            Club("Valid2", "BBB"),
            Club("Invalid", "TST"),  # Same country
        ]
        
        valid = constraints.get_valid_opponents(club, candidates, as_home=True)
        
        assert len(valid) == 2
        assert all(v.country != "TST" for v in valid)


class TestConferenceLeagueConstraints:
    """Tests for Conference League rules (6 matches)."""
    
    @pytest.fixture
    def constraints(self):
        return ConferenceLeagueConstraints()
    
    def test_match_counts(self, constraints):
        """Conference League has 6 matches."""
        assert constraints.matches_per_team == 6
        assert constraints.home_matches == 3
        assert constraints.away_matches == 3
    
    def test_home_limit_three(self, constraints):
        """Only 3 home matches in Conference League."""
        club = Club("Test", "TST")
        opponent = Club("Opp", "OPP")
        
        for i in range(3):
            club.home_opponents.append(Club(f"H{i}", f"C{i}"))
        
        can_pair, reason = constraints.can_pair(club, opponent, as_home=True)
        assert not can_pair


class TestDrawValidation:
    """Tests for final draw validation."""
    
    def test_validate_finds_incomplete(self):
        """Validation finds incomplete matches."""
        constraints = ChampionsLeagueConstraints()
        
        # Club with only 3 home matches
        club = Club("Incomplete", "TST")
        for i in range(3):
            club.home_opponents.append(Club(f"H{i}", f"C{i}"))
        for i in range(4):
            club.away_opponents.append(Club(f"A{i}", f"C{i+3}"))
        
        is_valid, violations = constraints.validate_final_draw([club])
        
        assert not is_valid
        assert any("home" in v.lower() for v in violations)
    
    def test_validate_finds_same_country(self):
        """Validation finds same country matches."""
        constraints = ChampionsLeagueConstraints()
        
        club = Club("Test", "POR")
        club.home_opponents = [Club("Portuguese", "POR")]  # Invalid!
        
        is_valid, violations = constraints.validate_final_draw([club])
        
        assert not is_valid
        assert any("country" in v.lower() for v in violations)


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
