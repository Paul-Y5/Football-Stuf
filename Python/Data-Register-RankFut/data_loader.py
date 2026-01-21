"""
Data Loader for Football Club Rankings
======================================

Load and parse club ranking data from CSV files.
"""
import csv
from pathlib import Path
from typing import List, Dict, Optional
from models import Club, TrendDirection, CountryStats


class DataLoader:
    """
    Load club ranking data from CSV files.
    
    Supports the Soccer_Football Clubs Ranking.csv format.
    """
    
    def __init__(self, filepath: Optional[str] = None):
        """
        Initialize loader with optional filepath.
        
        Args:
            filepath: Path to CSV file. If None, uses default.
        """
        if filepath is None:
            filepath = Path(__file__).parent / "Soccer_Football Clubs Ranking.csv"
        self.filepath = Path(filepath)
        self._clubs: List[Club] = []
        self._countries: Dict[str, CountryStats] = {}
    
    def load(self) -> List[Club]:
        """
        Load clubs from CSV file.
        
        Returns:
            List of Club objects.
            
        Raises:
            FileNotFoundError: If file doesn't exist.
            ValueError: If file format is invalid.
        """
        if not self.filepath.exists():
            raise FileNotFoundError(f"File not found: {self.filepath}")
        
        self._clubs = []
        seen = set()  # Track duplicates
        
        with open(self.filepath, 'r', encoding='utf-8') as f:
            reader = csv.reader(f)
            header = next(reader, None)
            
            if header is None:
                raise ValueError("Empty CSV file")
            
            for row in reader:
                if len(row) < 7:
                    continue
                
                try:
                    club = self._parse_row(row)
                    
                    # Skip duplicates (same club and country)
                    key = (club.name, club.country)
                    if key in seen:
                        continue
                    seen.add(key)
                    
                    self._clubs.append(club)
                except (ValueError, IndexError) as e:
                    # Skip malformed rows
                    continue
        
        # Build country stats
        self._build_country_stats()
        
        return self._clubs
    
    def _parse_row(self, row: List[str]) -> Club:
        """
        Parse a CSV row into a Club object.
        
        Args:
            row: CSV row as list of strings.
            
        Returns:
            Club object.
        """
        ranking = int(row[0])
        name = row[1].strip()
        country = row[2].strip()
        points = int(row[3])
        
        # Handle year change (can be negative with - symbol)
        year_change_str = row[4].strip()
        year_change = int(year_change_str) if year_change_str else 0
        
        previous_points = int(row[5])
        
        # Parse trend symbol
        symbol = row[6].strip()
        if symbol == '+':
            trend = TrendDirection.UP
        elif symbol == '-':
            trend = TrendDirection.DOWN
            # If trend is down, year_change should be negative
            if year_change > 0:
                year_change = -year_change
        else:
            trend = TrendDirection.STABLE
        
        return Club(
            ranking=ranking,
            name=name,
            country=country,
            points=points,
            year_change=year_change,
            previous_points=previous_points,
            trend=trend
        )
    
    def _build_country_stats(self) -> None:
        """Build country statistics from loaded clubs."""
        self._countries = {}
        
        for club in self._clubs:
            if club.country not in self._countries:
                self._countries[club.country] = CountryStats(
                    country=club.country,
                    clubs=[]
                )
            self._countries[club.country].clubs.append(club)
    
    @property
    def clubs(self) -> List[Club]:
        """Get all loaded clubs."""
        return self._clubs
    
    @property
    def countries(self) -> Dict[str, CountryStats]:
        """Get country statistics."""
        return self._countries
    
    def get_country(self, country: str) -> Optional[CountryStats]:
        """
        Get statistics for a specific country.
        
        Args:
            country: Country name.
            
        Returns:
            CountryStats or None if not found.
        """
        return self._countries.get(country)
    
    def get_top_clubs(self, n: int = 100) -> List[Club]:
        """
        Get top N clubs worldwide.
        
        Args:
            n: Number of clubs to return.
            
        Returns:
            List of top N clubs sorted by ranking.
        """
        return sorted(self._clubs, key=lambda c: c.ranking)[:n]
    
    def get_clubs_by_country(self, country: str) -> List[Club]:
        """
        Get all clubs from a specific country.
        
        Args:
            country: Country name.
            
        Returns:
            List of clubs sorted by ranking.
        """
        stats = self.get_country(country)
        if stats is None:
            return []
        return sorted(stats.clubs, key=lambda c: c.ranking)
    
    def search_clubs(self, query: str) -> List[Club]:
        """
        Search clubs by name (case-insensitive).
        
        Args:
            query: Search string.
            
        Returns:
            List of matching clubs.
        """
        query = query.lower()
        return [c for c in self._clubs if query in c.name.lower()]
    
    def get_improving_clubs(self, min_change: int = 100) -> List[Club]:
        """
        Get clubs that improved significantly.
        
        Args:
            min_change: Minimum position improvement.
            
        Returns:
            List of improving clubs sorted by improvement.
        """
        improving = [c for c in self._clubs 
                    if c.improved and c.year_change >= min_change]
        return sorted(improving, key=lambda c: c.year_change, reverse=True)
    
    def get_declining_clubs(self, min_change: int = 100) -> List[Club]:
        """
        Get clubs that declined significantly.
        
        Args:
            min_change: Minimum position decline (as positive number).
            
        Returns:
            List of declining clubs sorted by decline.
        """
        declining = [c for c in self._clubs 
                    if c.declined and abs(c.year_change) >= min_change]
        return sorted(declining, key=lambda c: c.year_change)


def load_data(filepath: Optional[str] = None) -> DataLoader:
    """
    Convenience function to load data.
    
    Args:
        filepath: Optional path to CSV file.
        
    Returns:
        DataLoader with data loaded.
    """
    loader = DataLoader(filepath)
    loader.load()
    return loader


if __name__ == "__main__":
    # Quick test
    loader = load_data()
    print(f"Loaded {len(loader.clubs)} clubs from {len(loader.countries)} countries")
    print("\nTop 10 clubs:")
    for club in loader.get_top_clubs(10):
        print(f"  {club}")
    
    print("\nTop 5 countries by average ranking:")
    sorted_countries = sorted(
        loader.countries.values(),
        key=lambda c: c.avg_ranking
    )[:5]
    for cs in sorted_countries:
        print(f"  {cs}")
