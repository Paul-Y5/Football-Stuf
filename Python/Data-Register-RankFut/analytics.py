"""
Analytics Module for Football Club Rankings
============================================

Statistical analysis and comparisons between leagues/countries.
"""
from dataclasses import dataclass, field
from typing import List, Dict, Tuple, Optional
from models import Club, CountryStats


@dataclass
class LeagueComparison:
    """
    Comparison result between two countries/leagues.
    
    Attributes:
        country_a: First country stats
        country_b: Second country stats
    """
    country_a: CountryStats
    country_b: CountryStats
    
    @property
    def avg_ranking_diff(self) -> float:
        """Difference in average ranking (negative = A is better)."""
        return self.country_a.avg_ranking - self.country_b.avg_ranking
    
    @property
    def avg_points_diff(self) -> float:
        """Difference in average points (positive = A is better)."""
        return self.country_a.avg_points - self.country_b.avg_points
    
    @property
    def competitiveness_diff(self) -> float:
        """
        Difference in competitiveness (std dev).
        Higher std dev = less competitive (bigger gap between clubs).
        """
        return self.country_a.std_dev_points - self.country_b.std_dev_points
    
    @property
    def winner_by_ranking(self) -> str:
        """Country with better average ranking."""
        if self.avg_ranking_diff < 0:
            return self.country_a.country
        elif self.avg_ranking_diff > 0:
            return self.country_b.country
        return "Tie"
    
    @property
    def winner_by_top_clubs(self) -> str:
        """Country with more top 100 clubs."""
        if self.country_a.top_100_count > self.country_b.top_100_count:
            return self.country_a.country
        elif self.country_a.top_100_count < self.country_b.top_100_count:
            return self.country_b.country
        return "Tie"
    
    @property
    def more_competitive(self) -> str:
        """Country with more internal competition (lower std dev)."""
        if self.country_a.std_dev_points < self.country_b.std_dev_points:
            return self.country_a.country
        elif self.country_a.std_dev_points > self.country_b.std_dev_points:
            return self.country_b.country
        return "Tie"


class LeagueAnalytics:
    """
    Analytics engine for league/country comparisons.
    """
    
    def __init__(self, countries: Dict[str, CountryStats]):
        """
        Initialize analytics with country data.
        
        Args:
            countries: Dictionary of country name to CountryStats.
        """
        self.countries = countries
    
    def compare(self, country_a: str, country_b: str) -> Optional[LeagueComparison]:
        """
        Compare two countries/leagues.
        
        Args:
            country_a: First country name.
            country_b: Second country name.
            
        Returns:
            LeagueComparison or None if country not found.
        """
        stats_a = self.countries.get(country_a)
        stats_b = self.countries.get(country_b)
        
        if stats_a is None or stats_b is None:
            return None
        
        return LeagueComparison(country_a=stats_a, country_b=stats_b)
    
    def rank_by_avg_ranking(self, min_clubs: int = 5) -> List[CountryStats]:
        """
        Rank countries by average club ranking.
        
        Args:
            min_clubs: Minimum clubs required to be included.
            
        Returns:
            List of CountryStats sorted by avg ranking (best first).
        """
        filtered = [c for c in self.countries.values() 
                   if c.total_clubs >= min_clubs]
        return sorted(filtered, key=lambda c: c.avg_ranking)
    
    def rank_by_avg_points(self, min_clubs: int = 5) -> List[CountryStats]:
        """
        Rank countries by average points.
        
        Args:
            min_clubs: Minimum clubs required to be included.
            
        Returns:
            List of CountryStats sorted by avg points (highest first).
        """
        filtered = [c for c in self.countries.values() 
                   if c.total_clubs >= min_clubs]
        return sorted(filtered, key=lambda c: c.avg_points, reverse=True)
    
    def rank_by_competitiveness(self, min_clubs: int = 5) -> List[CountryStats]:
        """
        Rank countries by internal competitiveness (lower std dev = more competitive).
        
        Args:
            min_clubs: Minimum clubs required.
            
        Returns:
            List sorted by std_dev_points (lowest first = most competitive).
        """
        filtered = [c for c in self.countries.values() 
                   if c.total_clubs >= min_clubs]
        return sorted(filtered, key=lambda c: c.std_dev_points)
    
    def rank_by_top_clubs(self, top_n: int = 100, min_clubs: int = 1) -> List[Tuple[str, int]]:
        """
        Rank countries by number of clubs in world top N.
        
        Args:
            top_n: Top N threshold (e.g., top 100).
            min_clubs: Minimum top clubs required to include.
            
        Returns:
            List of (country, count) tuples sorted by count.
        """
        counts = []
        for country, stats in self.countries.items():
            count = sum(1 for c in stats.clubs if c.ranking <= top_n)
            if count >= min_clubs:
                counts.append((country, count))
        
        return sorted(counts, key=lambda x: x[1], reverse=True)
    
    def get_distribution_brackets(self) -> Dict[str, Dict[str, int]]:
        """
        Get club distribution by ranking brackets for each country.
        
        Brackets: 1-50, 51-100, 101-250, 251-500, 501-1000, 1001+
        
        Returns:
            Dict mapping country to bracket counts.
        """
        brackets = {
            "1-50": (1, 50),
            "51-100": (51, 100),
            "101-250": (101, 250),
            "251-500": (251, 500),
            "501-1000": (501, 1000),
            "1001+": (1001, float('inf'))
        }
        
        distribution = {}
        for country, stats in self.countries.items():
            distribution[country] = {bracket: 0 for bracket in brackets}
            
            for club in stats.clubs:
                for bracket_name, (low, high) in brackets.items():
                    if low <= club.ranking <= high:
                        distribution[country][bracket_name] += 1
                        break
        
        return distribution
    
    def get_country_strength_score(self, country: str) -> Optional[float]:
        """
        Calculate composite strength score for a country.
        
        Score combines: avg ranking, top club presence, total clubs.
        Lower score = stronger country.
        
        Args:
            country: Country name.
            
        Returns:
            Strength score or None if country not found.
        """
        stats = self.countries.get(country)
        if stats is None:
            return None
        
        # Weighted score (lower is better)
        # - Average ranking (40% weight)
        # - Best club ranking (30% weight)  
        # - Inverse of top 100 count (30% weight)
        
        best_rank = stats.best_club.ranking if stats.best_club else 2000
        top_100_factor = 100 / (stats.top_100_count + 1)  # +1 to avoid div by 0
        
        score = (
            0.4 * stats.avg_ranking +
            0.3 * best_rank +
            0.3 * top_100_factor
        )
        
        return round(score, 2)
    
    def rank_by_strength(self, min_clubs: int = 5) -> List[Tuple[str, float]]:
        """
        Rank countries by composite strength score.
        
        Args:
            min_clubs: Minimum clubs required.
            
        Returns:
            List of (country, score) tuples sorted by score (lowest = strongest).
        """
        scores = []
        for country, stats in self.countries.items():
            if stats.total_clubs >= min_clubs:
                score = self.get_country_strength_score(country)
                if score is not None:
                    scores.append((country, score))
        
        return sorted(scores, key=lambda x: x[1])
    
    def get_global_stats(self) -> Dict[str, float]:
        """
        Get global statistics across all clubs.
        
        Returns:
            Dictionary with global stats.
        """
        all_clubs = []
        for stats in self.countries.values():
            all_clubs.extend(stats.clubs)
        
        if not all_clubs:
            return {}
        
        points = [c.points for c in all_clubs]
        rankings = [c.ranking for c in all_clubs]
        
        return {
            "total_clubs": len(all_clubs),
            "total_countries": len(self.countries),
            "avg_points": sum(points) / len(points),
            "max_points": max(points),
            "min_points": min(points),
            "avg_ranking": sum(rankings) / len(rankings),
            "improving_clubs": sum(1 for c in all_clubs if c.improved),
            "declining_clubs": sum(1 for c in all_clubs if c.declined),
        }


def create_analytics(countries: Dict[str, CountryStats]) -> LeagueAnalytics:
    """
    Factory function to create analytics engine.
    
    Args:
        countries: Country statistics dictionary.
        
    Returns:
        LeagueAnalytics instance.
    """
    return LeagueAnalytics(countries)
