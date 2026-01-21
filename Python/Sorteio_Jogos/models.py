"""
Data Models for UEFA Draw Simulator
====================================

Classes representing clubs, fixtures, and draw state.
"""
from dataclasses import dataclass, field
from typing import List, Dict, Optional, Set
from enum import Enum


class Competition(Enum):
    """UEFA Competitions."""
    CHAMPIONS_LEAGUE = "Champions League"
    EUROPA_LEAGUE = "Europa League"
    CONFERENCE_LEAGUE = "Conference League"


@dataclass
class Club:
    """
    Represents a football club in the draw.
    
    Attributes:
        name: Club name
        country: Country code (e.g., "POR", "ESP")
        pot: Pot number (1-4)
        coefficient: UEFA coefficient ranking
        competition: Which competition
    """
    name: str
    country: str
    pot: int = 1
    coefficient: float = 0.0
    competition: Competition = Competition.CHAMPIONS_LEAGUE
    
    # Draw state
    home_opponents: List["Club"] = field(default_factory=list)
    away_opponents: List["Club"] = field(default_factory=list)
    
    @property
    def all_opponents(self) -> List["Club"]:
        """All assigned opponents."""
        return self.home_opponents + self.away_opponents
    
    @property
    def opponent_countries(self) -> Dict[str, int]:
        """Count of opponents by country."""
        countries = {}
        for opp in self.all_opponents:
            countries[opp.country] = countries.get(opp.country, 0) + 1
        return countries
    
    @property
    def is_complete(self) -> bool:
        """Check if club has all 8 opponents."""
        return len(self.home_opponents) == 4 and len(self.away_opponents) == 4
    
    @property
    def needs_home(self) -> int:
        """Number of home matches still needed."""
        return 4 - len(self.home_opponents)
    
    @property
    def needs_away(self) -> int:
        """Number of away matches still needed."""
        return 4 - len(self.away_opponents)
    
    def can_face(self, other: "Club", max_same_country: int = 2) -> bool:
        """
        Check if this club can face another.
        
        Rules:
        - Cannot face club from same country
        - Cannot face same club twice
        - Max 2 opponents from same country
        """
        if self.name == other.name:
            return False
        
        if self.country == other.country:
            return False
        
        if other in self.all_opponents:
            return False
        
        # Check country limit
        current_from_country = self.opponent_countries.get(other.country, 0)
        if current_from_country >= max_same_country:
            return False
        
        return True
    
    def reset(self) -> None:
        """Reset draw state."""
        self.home_opponents = []
        self.away_opponents = []
    
    def __hash__(self):
        return hash((self.name, self.country))
    
    def __eq__(self, other):
        if not isinstance(other, Club):
            return False
        return self.name == other.name and self.country == other.country
    
    def __repr__(self):
        return f"Club({self.name}, {self.country}, Pot {self.pot})"


@dataclass
class Fixture:
    """A match fixture."""
    home: Club
    away: Club
    matchday: int = 0
    
    def __repr__(self):
        return f"{self.home.name} vs {self.away.name}"


@dataclass
class DrawState:
    """
    Current state of the draw.
    
    Used for backtracking algorithm.
    """
    clubs: List[Club]
    assignments: List[tuple]  # (club1, club2, is_home)
    current_club_idx: int = 0
    
    def save(self) -> Dict:
        """Save state for backtracking."""
        return {
            "assignments": self.assignments.copy(),
            "club_idx": self.current_club_idx,
            "home_opponents": {c.name: c.home_opponents.copy() for c in self.clubs},
            "away_opponents": {c.name: c.away_opponents.copy() for c in self.clubs}
        }
    
    def restore(self, state: Dict) -> None:
        """Restore from saved state."""
        self.assignments = state["assignments"]
        self.current_club_idx = state["club_idx"]
        
        for club in self.clubs:
            club.home_opponents = state["home_opponents"][club.name]
            club.away_opponents = state["away_opponents"][club.name]


@dataclass
class DrawResult:
    """Final draw result."""
    competition: Competition
    clubs: List[Club]
    fixtures: List[Fixture] = field(default_factory=list)
    is_valid: bool = True
    backtrack_count: int = 0
    
    def get_club_fixtures(self, club_name: str) -> Dict[str, List[str]]:
        """Get all fixtures for a club."""
        for club in self.clubs:
            if club.name == club_name:
                return {
                    "home": [c.name for c in club.home_opponents],
                    "away": [c.name for c in club.away_opponents]
                }
        return {"home": [], "away": []}
    
    def to_dict(self) -> Dict:
        """Export to dictionary."""
        return {
            "competition": self.competition.value,
            "valid": self.is_valid,
            "backtrack_count": self.backtrack_count,
            "clubs": [
                {
                    "name": c.name,
                    "country": c.country,
                    "pot": c.pot,
                    "home": [o.name for o in c.home_opponents],
                    "away": [o.name for o in c.away_opponents]
                }
                for c in self.clubs
            ]
        }
    
    def print_summary(self) -> None:
        """Print draw summary."""
        print(f"\n{self.competition.value} Draw Results")
        print("=" * 50)
        
        for pot in range(1, 5):
            pot_clubs = [c for c in self.clubs if c.pot == pot]
            if pot_clubs:
                print(f"\n[Pot {pot}]")
                for club in pot_clubs:
                    home = ", ".join(c.name for c in club.home_opponents)
                    away = ", ".join(c.name for c in club.away_opponents)
                    print(f"  {club.name} ({club.country})")
                    print(f"    [H] Home: {home}")
                    print(f"    [A] Away: {away}")
