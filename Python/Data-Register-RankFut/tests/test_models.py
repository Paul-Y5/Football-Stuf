"""
Test Models
===========

Unit tests for data models.
"""
import pytest
from models import Club, TrendDirection, CountryStats


class TestClub:
    """Tests for Club dataclass."""
    
    def test_club_creation(self):
        """Test basic club creation."""
        club = Club(
            ranking=1,
            name="Real Madrid",
            country="Spain",
            points=1900,
            year_change=5,
            previous_points=1850,
            trend=TrendDirection.UP
        )
        
        assert club.ranking == 1
        assert club.name == "Real Madrid"
        assert club.country == "Spain"
        assert club.points == 1900
        assert club.trend == TrendDirection.UP
    
    def test_points_change(self):
        """Test points change calculation."""
        club = Club(
            ranking=1,
            name="Test",
            country="Test",
            points=1900,
            year_change=5,
            previous_points=1850,
            trend=TrendDirection.UP
        )
        
        assert club.points_change == 50
    
    def test_improved_property(self):
        """Test improved property."""
        improving = Club(
            ranking=1, name="A", country="X",
            points=100, year_change=10, previous_points=90,
            trend=TrendDirection.UP
        )
        declining = Club(
            ranking=2, name="B", country="X",
            points=100, year_change=-10, previous_points=110,
            trend=TrendDirection.DOWN
        )
        
        assert improving.improved is True
        assert declining.improved is False
    
    def test_declined_property(self):
        """Test declined property."""
        declining = Club(
            ranking=1, name="A", country="X",
            points=100, year_change=-10, previous_points=110,
            trend=TrendDirection.DOWN
        )
        
        assert declining.declined is True
    
    def test_str_representation(self):
        """Test string representation."""
        club = Club(
            ranking=5,
            name="Barcelona",
            country="Spain",
            points=1800,
            year_change=0,
            previous_points=1800,
            trend=TrendDirection.STABLE
        )
        
        assert "5" in str(club)
        assert "Barcelona" in str(club)
        assert "Spain" in str(club)


class TestCountryStats:
    """Tests for CountryStats dataclass."""
    
    @pytest.fixture
    def sample_clubs(self):
        """Create sample clubs for testing."""
        return [
            Club(1, "Club A", "Country", 1900, 10, 1890, TrendDirection.UP),
            Club(10, "Club B", "Country", 1700, -5, 1705, TrendDirection.DOWN),
            Club(50, "Club C", "Country", 1500, 0, 1500, TrendDirection.STABLE),
            Club(100, "Club D", "Country", 1400, 20, 1380, TrendDirection.UP),
            Club(200, "Club E", "Country", 1300, -30, 1330, TrendDirection.DOWN),
        ]
    
    @pytest.fixture
    def country_stats(self, sample_clubs):
        """Create CountryStats with sample data."""
        return CountryStats(country="Country", clubs=sample_clubs)
    
    def test_total_clubs(self, country_stats):
        """Test total clubs count."""
        assert country_stats.total_clubs == 5
    
    def test_avg_ranking(self, country_stats):
        """Test average ranking calculation."""
        # (1 + 10 + 50 + 100 + 200) / 5 = 72.2
        assert country_stats.avg_ranking == pytest.approx(72.2, rel=0.01)
    
    def test_avg_points(self, country_stats):
        """Test average points calculation."""
        # (1900 + 1700 + 1500 + 1400 + 1300) / 5 = 1560
        assert country_stats.avg_points == 1560.0
    
    def test_std_dev_points(self, country_stats):
        """Test standard deviation calculation."""
        # Should be > 0 for varied data
        assert country_stats.std_dev_points > 0
        # Known value: sqrt(((1900-1560)^2 + ... + (1300-1560)^2) / 5)
        assert country_stats.std_dev_points == pytest.approx(215.4, rel=0.01)
    
    def test_best_club(self, country_stats):
        """Test best club selection."""
        assert country_stats.best_club.name == "Club A"
        assert country_stats.best_club.ranking == 1
    
    def test_worst_club(self, country_stats):
        """Test worst club selection."""
        assert country_stats.worst_club.name == "Club E"
        assert country_stats.worst_club.ranking == 200
    
    def test_median_ranking_odd(self, country_stats):
        """Test median with odd number of clubs."""
        # Sorted: 1, 10, 50, 100, 200 -> median = 50
        assert country_stats.median_ranking == 50.0
    
    def test_median_ranking_even(self):
        """Test median with even number of clubs."""
        clubs = [
            Club(1, "A", "X", 100, 0, 100, TrendDirection.STABLE),
            Club(10, "B", "X", 100, 0, 100, TrendDirection.STABLE),
            Club(20, "C", "X", 100, 0, 100, TrendDirection.STABLE),
            Club(30, "D", "X", 100, 0, 100, TrendDirection.STABLE),
        ]
        stats = CountryStats(country="X", clubs=clubs)
        # Sorted: 1, 10, 20, 30 -> median = (10 + 20) / 2 = 15
        assert stats.median_ranking == 15.0
    
    def test_top_counts(self, country_stats):
        """Test top N counts."""
        assert country_stats.top_10_count == 2  # ranks 1 and 10
        assert country_stats.top_50_count == 3  # ranks 1, 10, 50
        assert country_stats.top_100_count == 4  # ranks 1, 10, 50, 100
    
    def test_get_top_n(self, country_stats):
        """Test getting top N clubs."""
        top_3 = country_stats.get_top_n(3)
        
        assert len(top_3) == 3
        assert top_3[0].name == "Club A"
        assert top_3[1].name == "Club B"
        assert top_3[2].name == "Club C"
    
    def test_empty_stats(self):
        """Test with empty club list."""
        stats = CountryStats(country="Empty", clubs=[])
        
        assert stats.total_clubs == 0
        assert stats.avg_ranking == 0.0
        assert stats.avg_points == 0.0
        assert stats.std_dev_points == 0.0
        assert stats.best_club is None
        assert stats.worst_club is None
        assert stats.median_ranking == 0.0
    
    def test_single_club_std_dev(self):
        """Test std dev with single club (should be 0)."""
        club = Club(1, "Solo", "X", 1000, 0, 1000, TrendDirection.STABLE)
        stats = CountryStats(country="X", clubs=[club])
        
        assert stats.std_dev_points == 0.0


class TestTrendDirection:
    """Tests for TrendDirection enum."""
    
    def test_values(self):
        """Test enum values."""
        assert TrendDirection.UP.value == "+"
        assert TrendDirection.DOWN.value == "-"
        assert TrendDirection.STABLE.value == "="
