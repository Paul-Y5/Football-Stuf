"""
Data Models for Football Club Rankings
======================================

Dataclasses for type-safe data handling.
"""
from dataclasses import dataclass, field
from typing import List, Optional
from enum import Enum


class TrendDirection(Enum):
    """Trend direction for club ranking changes."""
    UP = "+"
    DOWN = "-"
    STABLE = "="


@dataclass
class Club:
    """
    Represents a football club with ranking data.
    
    Attributes:
        ranking: Current world ranking position
        name: Club name
        country: Country name
        points: Current point score
        year_change: Position change in 1 year (positive = improved)
        previous_points: Previous point score
        trend: Direction of change (+, -, =)
    """
    ranking: int
    name: str
    country: str
    points: int
    year_change: int
    previous_points: int
    trend: TrendDirection
    
    @property
    def points_change(self) -> int:
        """Calculate points difference from previous score."""
        return self.points - self.previous_points
    
    @property
    def improved(self) -> bool:
        """Check if club improved in ranking."""
        return self.trend == TrendDirection.UP
    
    @property
    def declined(self) -> bool:
        """Check if club declined in ranking."""
        return self.trend == TrendDirection.DOWN
    
    def __str__(self) -> str:
        return f"{self.ranking}. {self.name} ({self.country}) - {self.points} pts"


@dataclass
class CountryStats:
    """
    Statistical summary for a country's clubs.
    
    Attributes:
        country: Country name
        clubs: List of clubs from this country
        total_clubs: Number of clubs
        avg_ranking: Average ranking position
        avg_points: Average points
        std_dev_points: Standard deviation of points (competitiveness)
        best_club: Highest ranked club
        worst_club: Lowest ranked club
        median_ranking: Median ranking position
    """
    country: str
    clubs: List[Club] = field(default_factory=list)
    
    @property
    def total_clubs(self) -> int:
        """Number of clubs in country."""
        return len(self.clubs)
    
    @property
    def avg_ranking(self) -> float:
        """Average ranking position."""
        if not self.clubs:
            return 0.0
        return sum(c.ranking for c in self.clubs) / len(self.clubs)
    
    @property
    def avg_points(self) -> float:
        """Average points."""
        if not self.clubs:
            return 0.0
        return sum(c.points for c in self.clubs) / len(self.clubs)
    
    @property
    def std_dev_points(self) -> float:
        """Standard deviation of points (measures internal competitiveness)."""
        if len(self.clubs) < 2:
            return 0.0
        avg = self.avg_points
        variance = sum((c.points - avg) ** 2 for c in self.clubs) / len(self.clubs)
        return variance ** 0.5
    
    @property
    def best_club(self) -> Optional[Club]:
        """Highest ranked club."""
        if not self.clubs:
            return None
        return min(self.clubs, key=lambda c: c.ranking)
    
    @property
    def worst_club(self) -> Optional[Club]:
        """Lowest ranked club."""
        if not self.clubs:
            return None
        return max(self.clubs, key=lambda c: c.ranking)
    
    @property
    def median_ranking(self) -> float:
        """Median ranking position."""
        if not self.clubs:
            return 0.0
        sorted_rankings = sorted(c.ranking for c in self.clubs)
        n = len(sorted_rankings)
        mid = n // 2
        if n % 2 == 0:
            return (sorted_rankings[mid - 1] + sorted_rankings[mid]) / 2
        return float(sorted_rankings[mid])
    
    @property
    def top_10_count(self) -> int:
        """Number of clubs in world top 10."""
        return sum(1 for c in self.clubs if c.ranking <= 10)
    
    @property
    def top_50_count(self) -> int:
        """Number of clubs in world top 50."""
        return sum(1 for c in self.clubs if c.ranking <= 50)
    
    @property
    def top_100_count(self) -> int:
        """Number of clubs in world top 100."""
        return sum(1 for c in self.clubs if c.ranking <= 100)
    
    def get_top_n(self, n: int = 10) -> List[Club]:
        """Get top N clubs by ranking."""
        return sorted(self.clubs, key=lambda c: c.ranking)[:n]
    
    def __str__(self) -> str:
        return (f"{self.country}: {self.total_clubs} clubs, "
                f"avg rank: {self.avg_ranking:.1f}, "
                f"avg pts: {self.avg_points:.1f}")
