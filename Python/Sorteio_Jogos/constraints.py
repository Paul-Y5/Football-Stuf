"""
UEFA Draw Constraints
=====================

Implementation of official UEFA draw rules and restrictions.
"""
from typing import List, Set, Optional, Tuple
from models import Club, Competition


class DrawConstraints:
    """
    UEFA draw constraint checker.
    
    Implements official rules for European competition draws.
    """
    
    def __init__(
        self,
        competition: Competition = Competition.CHAMPIONS_LEAGUE,
        max_same_country: int = 2,
        matches_per_team: int = 8,
        home_matches: int = 4,
        away_matches: int = 4
    ):
        """
        Initialize constraints.
        
        Args:
            competition: Which UEFA competition
            max_same_country: Max opponents from same country
            matches_per_team: Total matches per team
            home_matches: Number of home matches
            away_matches: Number of away matches
        """
        self.competition = competition
        self.max_same_country = max_same_country
        self.matches_per_team = matches_per_team
        self.home_matches = home_matches
        self.away_matches = away_matches
    
    def can_pair(
        self,
        club1: Club,
        club2: Club,
        as_home: bool = True
    ) -> Tuple[bool, Optional[str]]:
        """
        Check if two clubs can be paired.
        
        Args:
            club1: First club (home if as_home)
            club2: Second club (away if as_home)
            as_home: Whether club1 plays at home
            
        Returns:
            (is_valid, reason_if_invalid)
        """
        # Same club check
        if club1 == club2:
            return False, "Cannot play against self"
        
        # Same country check
        if club1.country == club2.country:
            return False, f"Same country ({club1.country})"
        
        # Already opponents check
        if club2 in club1.all_opponents:
            return False, "Already opponents"
        
        # Country limit check
        c1_from_c2_country = club1.opponent_countries.get(club2.country, 0)
        c2_from_c1_country = club2.opponent_countries.get(club1.country, 0)
        
        if c1_from_c2_country >= self.max_same_country:
            return False, f"{club1.name} already has {self.max_same_country} opponents from {club2.country}"
        
        if c2_from_c1_country >= self.max_same_country:
            return False, f"{club2.name} already has {self.max_same_country} opponents from {club1.country}"
        
        # Home/Away capacity check
        if as_home:
            if len(club1.home_opponents) >= self.home_matches:
                return False, f"{club1.name} home matches full"
            if len(club2.away_opponents) >= self.away_matches:
                return False, f"{club2.name} away matches full"
        else:
            if len(club1.away_opponents) >= self.away_matches:
                return False, f"{club1.name} away matches full"
            if len(club2.home_opponents) >= self.home_matches:
                return False, f"{club2.name} home matches full"
        
        return True, None
    
    def get_valid_opponents(
        self,
        club: Club,
        candidates: List[Club],
        as_home: bool = True
    ) -> List[Club]:
        """
        Get all valid opponents for a club.
        
        Args:
            club: Club to find opponents for
            candidates: Potential opponents
            as_home: Whether club plays at home
            
        Returns:
            List of valid opponent clubs
        """
        valid = []
        for candidate in candidates:
            is_valid, _ = self.can_pair(club, candidate, as_home)
            if is_valid:
                valid.append(candidate)
        return valid
    
    def is_draw_possible(
        self,
        club: Club,
        remaining_clubs: List[Club]
    ) -> bool:
        """
        Check if a valid draw is still possible for this club.
        
        Uses look-ahead to detect dead ends.
        """
        needed_home = club.needs_home
        needed_away = club.needs_away
        
        valid_home = len(self.get_valid_opponents(club, remaining_clubs, as_home=True))
        valid_away = len(self.get_valid_opponents(club, remaining_clubs, as_home=False))
        
        return valid_home >= needed_home and valid_away >= needed_away
    
    def validate_final_draw(self, clubs: List[Club]) -> Tuple[bool, List[str]]:
        """
        Validate the final draw result.
        
        Returns:
            (is_valid, list_of_violations)
        """
        violations = []
        
        for club in clubs:
            # Check match count
            if len(club.home_opponents) != self.home_matches:
                violations.append(f"{club.name}: {len(club.home_opponents)} home (expected {self.home_matches})")
            
            if len(club.away_opponents) != self.away_matches:
                violations.append(f"{club.name}: {len(club.away_opponents)} away (expected {self.away_matches})")
            
            # Check same country
            for opp in club.all_opponents:
                if opp.country == club.country:
                    violations.append(f"{club.name} vs {opp.name}: Same country")
            
            # Check country limit
            for country, count in club.opponent_countries.items():
                if count > self.max_same_country:
                    violations.append(f"{club.name}: {count} opponents from {country}")
            
            # Check duplicates
            if len(club.all_opponents) != len(set(club.all_opponents)):
                violations.append(f"{club.name}: Duplicate opponent")
        
        return len(violations) == 0, violations


class ChampionsLeagueConstraints(DrawConstraints):
    """
    Champions League specific constraints (2024/25 format).
    
    36 teams, 8 matches each:
    - 2 opponents from each pot
    - 4 home, 4 away
    - No same country
    - Max 2 from same country
    """
    
    def __init__(self):
        super().__init__(
            competition=Competition.CHAMPIONS_LEAGUE,
            max_same_country=2,
            matches_per_team=8,
            home_matches=4,
            away_matches=4
        )
        self.opponents_per_pot = 2
    
    def can_pair_with_pot_check(
        self,
        club: Club,
        opponent: Club,
        as_home: bool = True
    ) -> Tuple[bool, Optional[str]]:
        """Check pairing including pot constraints."""
        # Basic checks
        is_valid, reason = self.can_pair(club, opponent, as_home)
        if not is_valid:
            return False, reason
        
        # Count opponents from same pot
        same_pot_count = sum(
            1 for opp in club.all_opponents if opp.pot == opponent.pot
        )
        
        if same_pot_count >= self.opponents_per_pot:
            return False, f"Already has {self.opponents_per_pot} from Pot {opponent.pot}"
        
        return True, None


class EuropaLeagueConstraints(DrawConstraints):
    """Europa League specific constraints."""
    
    def __init__(self):
        super().__init__(
            competition=Competition.EUROPA_LEAGUE,
            max_same_country=2,
            matches_per_team=8,
            home_matches=4,
            away_matches=4
        )


class ConferenceLeagueConstraints(DrawConstraints):
    """Conference League specific constraints."""
    
    def __init__(self):
        super().__init__(
            competition=Competition.CONFERENCE_LEAGUE,
            max_same_country=2,
            matches_per_team=6,
            home_matches=3,
            away_matches=3
        )
