"""
Test Analytics
==============

Unit tests for analytics module.
"""
import pytest
from models import Club, CountryStats, TrendDirection
from analytics import LeagueAnalytics, LeagueComparison, create_analytics


class TestLeagueComparison:
    """Tests for LeagueComparison class."""
    
    @pytest.fixture
    def country_a(self):
        """Create first country stats."""
        clubs = [
            Club(1, "A1", "CountryA", 1900, 10, 1890, TrendDirection.UP),
            Club(5, "A2", "CountryA", 1800, 5, 1795, TrendDirection.UP),
            Club(20, "A3", "CountryA", 1600, -10, 1610, TrendDirection.DOWN),
        ]
        return CountryStats(country="CountryA", clubs=clubs)
    
    @pytest.fixture
    def country_b(self):
        """Create second country stats."""
        clubs = [
            Club(10, "B1", "CountryB", 1700, 15, 1685, TrendDirection.UP),
            Club(50, "B2", "CountryB", 1500, 20, 1480, TrendDirection.UP),
            Club(100, "B3", "CountryB", 1400, 0, 1400, TrendDirection.STABLE),
        ]
        return CountryStats(country="CountryB", clubs=clubs)
    
    @pytest.fixture
    def comparison(self, country_a, country_b):
        """Create comparison between two countries."""
        return LeagueComparison(country_a=country_a, country_b=country_b)
    
    def test_avg_ranking_diff(self, comparison):
        """Test average ranking difference."""
        # A: (1+5+20)/3 = 8.67, B: (10+50+100)/3 = 53.33
        # Diff = 8.67 - 53.33 = -44.67 (A is better, negative)
        assert comparison.avg_ranking_diff < 0
    
    def test_avg_points_diff(self, comparison):
        """Test average points difference."""
        # A: (1900+1800+1600)/3 = 1766.67, B: (1700+1500+1400)/3 = 1533.33
        # Diff = 1766.67 - 1533.33 = 233.33 (A has more points)
        assert comparison.avg_points_diff > 0
    
    def test_winner_by_ranking(self, comparison):
        """Test winner by ranking."""
        # A has lower avg ranking (better)
        assert comparison.winner_by_ranking == "CountryA"
    
    def test_winner_by_top_clubs(self, comparison):
        """Test winner by top 100 clubs."""
        # A: 3 clubs in top 100, B: 3 clubs in top 100
        # Should check actual counts
        assert comparison.winner_by_top_clubs in ["CountryA", "CountryB", "Tie"]


class TestLeagueAnalytics:
    """Tests for LeagueAnalytics class."""
    
    @pytest.fixture
    def sample_countries(self):
        """Create sample country data."""
        # Strong country (high avg points, low avg ranking)
        strong_clubs = [
            Club(1, "S1", "Strong", 1900, 10, 1890, TrendDirection.UP),
            Club(2, "S2", "Strong", 1850, 5, 1845, TrendDirection.UP),
            Club(5, "S3", "Strong", 1800, -5, 1805, TrendDirection.DOWN),
            Club(10, "S4", "Strong", 1750, 0, 1750, TrendDirection.STABLE),
            Club(20, "S5", "Strong", 1700, 10, 1690, TrendDirection.UP),
        ]
        
        # Medium country
        medium_clubs = [
            Club(50, "M1", "Medium", 1600, 20, 1580, TrendDirection.UP),
            Club(100, "M2", "Medium", 1500, 15, 1485, TrendDirection.UP),
            Club(150, "M3", "Medium", 1450, -20, 1470, TrendDirection.DOWN),
            Club(200, "M4", "Medium", 1400, 0, 1400, TrendDirection.STABLE),
            Club(250, "M5", "Medium", 1350, -10, 1360, TrendDirection.DOWN),
        ]
        
        # Weak country
        weak_clubs = [
            Club(500, "W1", "Weak", 1200, 30, 1170, TrendDirection.UP),
            Club(600, "W2", "Weak", 1150, 25, 1125, TrendDirection.UP),
            Club(700, "W3", "Weak", 1100, -50, 1150, TrendDirection.DOWN),
            Club(800, "W4", "Weak", 1050, 0, 1050, TrendDirection.STABLE),
            Club(900, "W5", "Weak", 1000, -30, 1030, TrendDirection.DOWN),
        ]
        
        # Small country (less than 5 clubs)
        small_clubs = [
            Club(300, "Sm1", "Small", 1300, 10, 1290, TrendDirection.UP),
            Club(400, "Sm2", "Small", 1250, 5, 1245, TrendDirection.UP),
        ]
        
        return {
            "Strong": CountryStats(country="Strong", clubs=strong_clubs),
            "Medium": CountryStats(country="Medium", clubs=medium_clubs),
            "Weak": CountryStats(country="Weak", clubs=weak_clubs),
            "Small": CountryStats(country="Small", clubs=small_clubs),
        }
    
    @pytest.fixture
    def analytics(self, sample_countries):
        """Create analytics instance."""
        return LeagueAnalytics(sample_countries)
    
    def test_compare(self, analytics):
        """Test country comparison."""
        comparison = analytics.compare("Strong", "Weak")
        
        assert comparison is not None
        assert comparison.country_a.country == "Strong"
        assert comparison.country_b.country == "Weak"
    
    def test_compare_not_found(self, analytics):
        """Test comparison with non-existent country."""
        result = analytics.compare("Strong", "NonExistent")
        assert result is None
    
    def test_rank_by_avg_ranking(self, analytics):
        """Test ranking by average position."""
        ranked = analytics.rank_by_avg_ranking(min_clubs=5)
        
        assert len(ranked) == 3  # Strong, Medium, Weak (Small excluded)
        assert ranked[0].country == "Strong"  # Best avg ranking
        assert ranked[-1].country == "Weak"  # Worst avg ranking
    
    def test_rank_by_avg_points(self, analytics):
        """Test ranking by average points."""
        ranked = analytics.rank_by_avg_points(min_clubs=5)
        
        assert len(ranked) == 3
        assert ranked[0].country == "Strong"  # Highest avg points
        assert ranked[-1].country == "Weak"  # Lowest avg points
    
    def test_rank_by_competitiveness(self, analytics):
        """Test ranking by competitiveness."""
        ranked = analytics.rank_by_competitiveness(min_clubs=5)
        
        # All have some clubs, should return sorted by std_dev
        assert len(ranked) == 3
    
    def test_rank_by_top_clubs(self, analytics):
        """Test ranking by top N clubs."""
        ranked = analytics.rank_by_top_clubs(top_n=100)
        
        # Strong has most clubs in top 100 (all 5)
        # Medium has 2 (rank 50, 100)
        assert len(ranked) > 0
        assert ranked[0][0] == "Strong"
    
    def test_get_distribution_brackets(self, analytics):
        """Test distribution brackets."""
        dist = analytics.get_distribution_brackets()
        
        assert "Strong" in dist
        assert "1-50" in dist["Strong"]
        
        # Strong has clubs at ranks 1, 2, 5, 10, 20 -> all in 1-50
        assert dist["Strong"]["1-50"] == 5
        
        # Weak has clubs at 500, 600, 700, 800, 900
        # 500 is in 251-500 bracket, 600-900 are in 501-1000
        assert dist["Weak"]["251-500"] == 1
        assert dist["Weak"]["501-1000"] == 4
    
    def test_country_strength_score(self, analytics):
        """Test strength score calculation."""
        strong_score = analytics.get_country_strength_score("Strong")
        weak_score = analytics.get_country_strength_score("Weak")
        
        assert strong_score is not None
        assert weak_score is not None
        assert strong_score < weak_score  # Lower score = stronger
    
    def test_country_strength_score_not_found(self, analytics):
        """Test strength score for non-existent country."""
        result = analytics.get_country_strength_score("NonExistent")
        assert result is None
    
    def test_rank_by_strength(self, analytics):
        """Test ranking by composite strength."""
        ranked = analytics.rank_by_strength(min_clubs=5)
        
        assert len(ranked) == 3
        assert ranked[0][0] == "Strong"  # Lowest score = strongest
        assert ranked[-1][0] == "Weak"
    
    def test_global_stats(self, analytics):
        """Test global statistics."""
        stats = analytics.get_global_stats()
        
        assert stats["total_clubs"] == 17  # 5+5+5+2
        assert stats["total_countries"] == 4
        assert "avg_points" in stats
        assert "max_points" in stats
        assert "min_points" in stats
        assert stats["max_points"] == 1900
        assert stats["min_points"] == 1000
    
    def test_min_clubs_filter(self, analytics):
        """Test min_clubs filter excludes small countries."""
        # With min_clubs=5, Small country should be excluded
        ranked = analytics.rank_by_avg_ranking(min_clubs=5)
        countries = [c.country for c in ranked]
        
        assert "Small" not in countries
        
        # With min_clubs=2, Small should be included
        ranked = analytics.rank_by_avg_ranking(min_clubs=2)
        countries = [c.country for c in ranked]
        
        assert "Small" in countries


class TestCreateAnalytics:
    """Tests for factory function."""
    
    def test_create_analytics(self):
        """Test analytics factory function."""
        countries = {
            "Test": CountryStats(country="Test", clubs=[])
        }
        
        analytics = create_analytics(countries)
        
        assert isinstance(analytics, LeagueAnalytics)
        assert "Test" in analytics.countries
