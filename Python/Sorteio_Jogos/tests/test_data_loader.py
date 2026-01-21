#!/usr/bin/env python3
"""
Tests for Data Loader Module
============================
"""
import pytest
import json
import tempfile
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent))

from data_loader import DataLoader, CompetitionData
from models import Competition


class TestDataLoader:
    """Tests for data loading functionality."""
    
    @pytest.fixture
    def loader(self):
        return DataLoader()
    
    @pytest.fixture
    def sample_json_data(self):
        return {
            "champions_league": {
                "name": "Test Champions League",
                "teams_count": 4,
                "matches_per_team": 8,
                "home_matches": 4,
                "away_matches": 4,
                "pots": {
                    "1": [
                        {"name": "Team A", "country": "ESP", "coefficient": 100.0},
                        {"name": "Team B", "country": "ENG", "coefficient": 90.0}
                    ],
                    "2": [
                        {"name": "Team C", "country": "GER", "coefficient": 80.0},
                        {"name": "Team D", "country": "FRA", "coefficient": 70.0}
                    ]
                }
            }
        }
    
    def test_load_json(self, loader, sample_json_data):
        """Test loading from JSON."""
        with tempfile.NamedTemporaryFile(mode='w', suffix='.json', delete=False) as f:
            json.dump(sample_json_data, f)
            temp_path = f.name
        
        try:
            competitions = loader.load_json(temp_path)
            
            assert Competition.CHAMPIONS_LEAGUE in competitions
            data = competitions[Competition.CHAMPIONS_LEAGUE]
            
            assert data.name == "Test Champions League"
            assert data.teams_count == 4
            assert len(data.pots[1]) == 2
            assert len(data.pots[2]) == 2
            
            clubs = data.all_clubs
            assert len(clubs) == 4
            assert any(c.name == "Team A" for c in clubs)
        finally:
            Path(temp_path).unlink()
    
    def test_load_csv(self, loader):
        """Test loading from CSV."""
        csv_content = """name,country,pot,coefficient,competition
Real Madrid,ESP,1,100.0,CHAMPIONS_LEAGUE
Barcelona,ESP,2,90.0,CHAMPIONS_LEAGUE
Man City,ENG,1,95.0,CHAMPIONS_LEAGUE
"""
        with tempfile.NamedTemporaryFile(mode='w', suffix='.csv', delete=False) as f:
            f.write(csv_content)
            temp_path = f.name
        
        try:
            clubs = loader.load_csv(temp_path)
            assert len(clubs) == 3
            assert any(c.name == "Real Madrid" for c in clubs)
            assert any(c.country == "ENG" for c in clubs)
        finally:
            Path(temp_path).unlink()
    
    def test_load_csv_filtered(self, loader):
        """Test loading CSV with competition filter."""
        csv_content = """name,country,pot,coefficient,competition
Team A,ESP,1,100.0,CHAMPIONS_LEAGUE
Team B,ESP,1,90.0,EUROPA_LEAGUE
Team C,ESP,1,80.0,CONFERENCE_LEAGUE
"""
        with tempfile.NamedTemporaryFile(mode='w', suffix='.csv', delete=False) as f:
            f.write(csv_content)
            temp_path = f.name
        
        try:
            clubs = loader.load_csv(temp_path, Competition.CHAMPIONS_LEAGUE)
            assert len(clubs) == 1
            assert clubs[0].name == "Team A"
        finally:
            Path(temp_path).unlink()
    
    def test_load_txt_legacy(self, loader):
        """Test loading legacy TXT format."""
        txt_content = """# Header comment
Real Madrid|ESP|1|1|LC
Barcelona|ESP|2|2|LC
Roma|ITA|1|1|LE
Chelsea|ENG|1|1|LCE
"""
        with tempfile.NamedTemporaryFile(mode='w', suffix='.txt', delete=False) as f:
            f.write(txt_content)
            temp_path = f.name
        
        try:
            competitions = loader.load_txt(temp_path)
            
            assert Competition.CHAMPIONS_LEAGUE in competitions
            assert Competition.EUROPA_LEAGUE in competitions
            assert Competition.CONFERENCE_LEAGUE in competitions
            
            ucl_clubs = competitions[Competition.CHAMPIONS_LEAGUE]
            assert len(ucl_clubs) == 2
        finally:
            Path(temp_path).unlink()
    
    def test_load_from_pots(self, loader):
        """Test loading from dictionary pots."""
        pots = {
            1: [{"name": "A", "country": "ESP"}],
            2: [{"name": "B", "country": "ENG"}],
            3: [{"name": "C", "country": "GER"}],
            4: [{"name": "D", "country": "FRA"}]
        }
        
        clubs = loader.load_from_pots(pots)
        
        assert len(clubs) == 4
        assert clubs[0].pot == 1
        assert clubs[3].pot == 4
    
    def test_competition_code_parsing(self, loader):
        """Test competition code parsing."""
        codes = [
            ("LC", Competition.CHAMPIONS_LEAGUE),
            ("UCL", Competition.CHAMPIONS_LEAGUE),
            ("champions_league", Competition.CHAMPIONS_LEAGUE),
            ("LE", Competition.EUROPA_LEAGUE),
            ("UEL", Competition.EUROPA_LEAGUE),
            ("LCE", Competition.CONFERENCE_LEAGUE),
            ("UECL", Competition.CONFERENCE_LEAGUE),
        ]
        
        for code, expected in codes:
            result = loader._parse_competition_code(code)
            assert result == expected, f"Failed for {code}"
    
    def test_default_clubs_champions_league(self, loader):
        """Test default Champions League clubs."""
        clubs = loader._default_champions_league()
        
        assert len(clubs) == 36
        
        # Check pot distribution - clubs have pot attribute set
        pot1 = [c for c in clubs if c.pot == 1]
        pot2 = [c for c in clubs if c.pot == 2]
        pot3 = [c for c in clubs if c.pot == 3]
        pot4 = [c for c in clubs if c.pot == 4]
        
        assert len(pot1) == 9, f"Expected 9 in pot1, got {len(pot1)}"
        assert len(pot2) == 9, f"Expected 9 in pot2, got {len(pot2)}"
        assert len(pot3) == 9, f"Expected 9 in pot3, got {len(pot3)}"
        assert len(pot4) == 9, f"Expected 9 in pot4, got {len(pot4)}"


class TestCompetitionData:
    """Tests for CompetitionData dataclass."""
    
    def test_all_clubs_property(self):
        """Test all_clubs combines pots correctly."""
        from models import Club
        
        pots = {
            1: [Club("A", "ESP", 1), Club("B", "ENG", 1)],
            2: [Club("C", "GER", 2), Club("D", "FRA", 2)]
        }
        
        data = CompetitionData(
            name="Test",
            competition=Competition.CHAMPIONS_LEAGUE,
            teams_count=4,
            matches_per_team=8,
            home_matches=4,
            away_matches=4,
            pots=pots
        )
        
        clubs = data.all_clubs
        assert len(clubs) == 4
        assert clubs[0].name == "A"  # Pot 1 first
        assert clubs[2].name == "C"  # Then pot 2


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
