"""
Test Data Loader
================

Unit tests for data loading functionality.
"""
import pytest
import tempfile
import os
from pathlib import Path

from data_loader import DataLoader, load_data
from models import TrendDirection


class TestDataLoader:
    """Tests for DataLoader class."""
    
    @pytest.fixture
    def sample_csv(self):
        """Create a sample CSV file for testing."""
        content = """ranking,club name ,country,point score,1 year change,previous point scored,symbol change
1,Real Madrid,Spain,1900,10,1890,+
2,Manchester City,England,1850,5,1845,+
3,Bayern Munich,Germany,1820,15,1805,+
50,Benfica,Portugal,1600,20,1580,+
100,Porto,Portugal,1500,30,1470,+
200,Sporting,Portugal,1400,10,1390,+
500,Test Club,Spain,1200,50,1250,-
"""
        with tempfile.NamedTemporaryFile(mode='w', suffix='.csv', 
                                         delete=False, encoding='utf-8') as f:
            f.write(content)
            return f.name
    
    @pytest.fixture
    def loader_with_data(self, sample_csv):
        """Create loader with sample data."""
        loader = DataLoader(sample_csv)
        loader.load()
        yield loader
        os.unlink(sample_csv)
    
    def test_load_clubs(self, loader_with_data):
        """Test loading clubs from CSV."""
        clubs = loader_with_data.clubs
        
        assert len(clubs) == 7
        assert clubs[0].name == "Real Madrid"
        assert clubs[0].ranking == 1
    
    def test_load_countries(self, loader_with_data):
        """Test country stats are built."""
        countries = loader_with_data.countries
        
        assert "Spain" in countries
        assert "England" in countries
        assert "Germany" in countries
        assert "Portugal" in countries
        
        assert countries["Portugal"].total_clubs == 3
        assert countries["Spain"].total_clubs == 2
    
    def test_get_country(self, loader_with_data):
        """Test getting specific country."""
        portugal = loader_with_data.get_country("Portugal")
        
        assert portugal is not None
        assert portugal.total_clubs == 3
        assert portugal.best_club.name == "Benfica"
    
    def test_get_country_not_found(self, loader_with_data):
        """Test getting non-existent country."""
        result = loader_with_data.get_country("NonExistent")
        assert result is None
    
    def test_get_top_clubs(self, loader_with_data):
        """Test getting top N clubs."""
        top_3 = loader_with_data.get_top_clubs(3)
        
        assert len(top_3) == 3
        assert top_3[0].ranking == 1
        assert top_3[1].ranking == 2
        assert top_3[2].ranking == 3
    
    def test_get_clubs_by_country(self, loader_with_data):
        """Test getting clubs by country."""
        portuguese = loader_with_data.get_clubs_by_country("Portugal")
        
        assert len(portuguese) == 3
        assert portuguese[0].name == "Benfica"  # Best ranked
        assert portuguese[-1].name == "Sporting"  # Worst ranked
    
    def test_search_clubs(self, loader_with_data):
        """Test club search."""
        results = loader_with_data.search_clubs("madrid")
        
        assert len(results) == 1
        assert results[0].name == "Real Madrid"
    
    def test_search_clubs_case_insensitive(self, loader_with_data):
        """Test search is case insensitive."""
        results = loader_with_data.search_clubs("BAYERN")
        
        assert len(results) == 1
        assert results[0].name == "Bayern Munich"
    
    def test_trend_parsing(self, loader_with_data):
        """Test trend symbol parsing."""
        spain_clubs = loader_with_data.get_clubs_by_country("Spain")
        
        # Real Madrid has + trend
        real = next(c for c in spain_clubs if c.name == "Real Madrid")
        assert real.trend == TrendDirection.UP
        
        # Test Club has - trend
        test_club = next(c for c in spain_clubs if c.name == "Test Club")
        assert test_club.trend == TrendDirection.DOWN
    
    def test_file_not_found(self):
        """Test error on missing file."""
        loader = DataLoader("/nonexistent/path.csv")
        
        with pytest.raises(FileNotFoundError):
            loader.load()
    
    def test_empty_csv(self):
        """Test handling empty CSV."""
        with tempfile.NamedTemporaryFile(mode='w', suffix='.csv', 
                                         delete=False, encoding='utf-8') as f:
            f.write("")
            path = f.name
        
        try:
            loader = DataLoader(path)
            with pytest.raises(ValueError):
                loader.load()
        finally:
            os.unlink(path)
    
    def test_duplicate_removal(self):
        """Test duplicate clubs are removed."""
        content = """ranking,club name ,country,point score,1 year change,previous point scored,symbol change
1,Club A,Country,1000,0,1000,+
1,Club A,Country,1000,0,1000,+
2,Club B,Country,900,0,900,+
"""
        with tempfile.NamedTemporaryFile(mode='w', suffix='.csv', 
                                         delete=False, encoding='utf-8') as f:
            f.write(content)
            path = f.name
        
        try:
            loader = DataLoader(path)
            loader.load()
            assert len(loader.clubs) == 2  # Duplicate removed
        finally:
            os.unlink(path)
    
    def test_improving_clubs(self, loader_with_data):
        """Test getting improving clubs."""
        improving = loader_with_data.get_improving_clubs(min_change=15)
        
        # Clubs with year_change >= 15 and UP trend
        assert len(improving) >= 2
        assert all(c.improved for c in improving)
        assert all(c.year_change >= 15 for c in improving)
    
    def test_declining_clubs(self, loader_with_data):
        """Test getting declining clubs."""
        declining = loader_with_data.get_declining_clubs(min_change=30)
        
        # Test Club has -50 change
        assert len(declining) >= 1
        assert all(c.declined for c in declining)


class TestLoadDataFunction:
    """Tests for load_data convenience function."""
    
    def test_load_data_returns_loader(self):
        """Test load_data returns DataLoader."""
        # Skip if default file doesn't exist
        default_path = Path(__file__).parent.parent / "Soccer_Football Clubs Ranking.csv"
        if not default_path.exists():
            pytest.skip("Default CSV file not found")
        
        loader = load_data()
        assert isinstance(loader, DataLoader)
        assert len(loader.clubs) > 0
