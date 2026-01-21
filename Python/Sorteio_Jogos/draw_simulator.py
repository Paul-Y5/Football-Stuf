#!/usr/bin/env python3
"""
UEFA Draw Simulator
===================

Faithful implementation of the official UEFA draw algorithm
for European competitions (Champions League, Europa League, Conference League).

Features:
- Pot system with coefficient rankings
- Country protection rules
- Home/Away balance
- Backtracking for impossible situations
- Optimized recursive algorithm with MRV heuristic
- Support for TOML/JSON/CSV/TXT input files

Data Format Testing:
- TOML: Preferred format for manual configuration (readable, supports comments)
- JSON: Traditional structured format (more verbose)

Usage:
    python draw_simulator.py
    python draw_simulator.py --competition europa_league
    python draw_simulator.py --simulate 1000
    python draw_simulator.py --input data/teams_2025.toml
    python draw_simulator.py --interactive
"""
import random
import json
import argparse
import sys
from functools import lru_cache
from typing import List, Dict, Optional, Tuple, Set, FrozenSet
from pathlib import Path

from models import Club, Competition, DrawResult, DrawState, Fixture
from constraints import (
    DrawConstraints,
    ChampionsLeagueConstraints,
    EuropaLeagueConstraints,
    ConferenceLeagueConstraints
)
from data_loader import DataLoader, CompetitionData, create_teams_from_input


class UEFADrawSimulator:
    """
    Official UEFA draw algorithm implementation.
    
    Simulates the draw for European competitions following
    all official rules and constraints.
    
    Optimizations:
    - MRV (Minimum Remaining Values) heuristic: always pick most constrained club
    - Forward checking: prune invalid assignments early
    - Iterative deepening: avoid deep recursion when possible
    
    Example:
        simulator = UEFADrawSimulator(Competition.CHAMPIONS_LEAGUE)
        result = simulator.draw()
        result.print_summary()
    """
    
    def __init__(
        self,
        competition: Competition = Competition.CHAMPIONS_LEAGUE,
        seed: Optional[int] = None,
        verbose: bool = True
    ):
        """
        Initialize simulator.
        
        Args:
            competition: Which UEFA competition
            seed: Random seed for reproducibility
            verbose: Print progress messages
        """
        self.competition = competition
        self.verbose = verbose
        
        if seed is not None:
            random.seed(seed)
        
        # Set up constraints
        if competition == Competition.CHAMPIONS_LEAGUE:
            self.constraints = ChampionsLeagueConstraints()
        elif competition == Competition.EUROPA_LEAGUE:
            self.constraints = EuropaLeagueConstraints()
        else:
            self.constraints = ConferenceLeagueConstraints()
        
        self.clubs: List[Club] = []
        self.backtrack_count = 0
        self.max_backtrack = 50000
        
        # Optimization: cache for valid opponents
        self._valid_cache: Dict[Tuple[str, str, bool], bool] = {}
        self._opponent_count_cache: Dict[str, int] = {}
        
        # Data loader
        self.data_loader = DataLoader()
    
    def load_clubs(self, data: Optional[List[Dict]] = None) -> None:
        """
        Load clubs for the draw.
        
        Args:
            data: List of club dictionaries, or None to use default
        """
        if data is None:
            data = self._get_default_clubs()
        
        self.clubs = []
        for club_data in data:
            club = Club(
                name=club_data["name"],
                country=club_data["country"],
                coefficient=club_data.get("coefficient", 0),
                pot=club_data.get("pot", 1),
                competition=self.competition
            )
            self.clubs.append(club)
        
        # Clear caches when clubs change
        self._valid_cache.clear()
        self._opponent_count_cache.clear()
        
        if self.verbose:
            print(f"[OK] Loaded {len(self.clubs)} clubs")
    
    def load_from_file(self, filepath: str) -> None:
        """
        Load clubs from TOML, JSON, CSV, or TXT file.
        
        TOML is preferred for manual configuration.
        
        Args:
            filepath: Path to data file
        """
        path = Path(filepath)
        
        if path.suffix == ".toml":
            competitions = self.data_loader.load_toml(path)
            if self.competition in competitions:
                self.clubs = competitions[self.competition].all_clubs
        elif path.suffix == ".json":
            competitions = self.data_loader.load_json(path)
            if self.competition in competitions:
                self.clubs = competitions[self.competition].all_clubs
        elif path.suffix == ".csv":
            self.clubs = self.data_loader.load_csv(path, self.competition)
        elif path.suffix == ".txt":
            competitions = self.data_loader.load_txt(path)
            if self.competition in competitions:
                self.clubs = competitions[self.competition]
        else:
            raise ValueError(f"Unsupported file format: {path.suffix}")
        
        # Clear caches
        self._valid_cache.clear()
        self._opponent_count_cache.clear()
        
        if self.verbose:
            print(f"[OK] Loaded {len(self.clubs)} clubs from {path.name}")
    
    def load_from_pots(self, pots: Dict[int, List[Dict]]) -> None:
        """
        Load clubs from pot dictionary (for interactive input).
        
        Args:
            pots: {pot_number: [{"name": ..., "country": ...}, ...]}
        """
        self.clubs = self.data_loader.load_from_pots(pots)
        
        # Clear caches
        self._valid_cache.clear()
        self._opponent_count_cache.clear()
        
        if self.verbose:
            print(f"[OK] Loaded {len(self.clubs)} clubs from input")
    
    def _get_default_clubs(self) -> List[Dict]:
        """Get default Champions League clubs."""
        # Champions League 2025/26 style
        return [
            # Pot 1
            {"name": "Real Madrid", "country": "ESP", "pot": 1, "coefficient": 136.0},
            {"name": "Manchester City", "country": "ENG", "pot": 1, "coefficient": 128.0},
            {"name": "Bayern Munich", "country": "GER", "pot": 1, "coefficient": 124.0},
            {"name": "PSG", "country": "FRA", "pot": 1, "coefficient": 104.0},
            {"name": "Liverpool", "country": "ENG", "pot": 1, "coefficient": 99.0},
            {"name": "Inter Milan", "country": "ITA", "pot": 1, "coefficient": 98.0},
            {"name": "Borussia Dortmund", "country": "GER", "pot": 1, "coefficient": 91.0},
            {"name": "Barcelona", "country": "ESP", "pot": 1, "coefficient": 88.0},
            {"name": "Leverkusen", "country": "GER", "pot": 1, "coefficient": 80.0},
            # Pot 2
            {"name": "Atletico Madrid", "country": "ESP", "pot": 2, "coefficient": 78.0},
            {"name": "Juventus", "country": "ITA", "pot": 2, "coefficient": 76.0},
            {"name": "Benfica", "country": "POR", "pot": 2, "coefficient": 75.0},
            {"name": "Arsenal", "country": "ENG", "pot": 2, "coefficient": 72.0},
            {"name": "Club Brugge", "country": "BEL", "pot": 2, "coefficient": 64.0},
            {"name": "Shakhtar", "country": "UKR", "pot": 2, "coefficient": 62.0},
            {"name": "AC Milan", "country": "ITA", "pot": 2, "coefficient": 60.0},
            {"name": "Atalanta", "country": "ITA", "pot": 2, "coefficient": 58.0},
            {"name": "Sporting CP", "country": "POR", "pot": 2, "coefficient": 55.0},
            # Pot 3
            {"name": "FC Porto", "country": "POR", "pot": 3, "coefficient": 53.0},
            {"name": "Feyenoord", "country": "NED", "pot": 3, "coefficient": 50.0},
            {"name": "PSV", "country": "NED", "pot": 3, "coefficient": 48.0},
            {"name": "Celtic", "country": "SCO", "pot": 3, "coefficient": 44.0},
            {"name": "Monaco", "country": "FRA", "pot": 3, "coefficient": 42.0},
            {"name": "Aston Villa", "country": "ENG", "pot": 3, "coefficient": 40.0},
            {"name": "Bologna", "country": "ITA", "pot": 3, "coefficient": 38.0},
            {"name": "Lille", "country": "FRA", "pot": 3, "coefficient": 36.0},
            {"name": "Girona", "country": "ESP", "pot": 3, "coefficient": 35.0},
            # Pot 4
            {"name": "Stuttgart", "country": "GER", "pot": 4, "coefficient": 33.0},
            {"name": "Sturm Graz", "country": "AUT", "pot": 4, "coefficient": 30.0},
            {"name": "Brest", "country": "FRA", "pot": 4, "coefficient": 28.0},
            {"name": "Salzburg", "country": "AUT", "pot": 4, "coefficient": 26.0},
            {"name": "Red Star", "country": "SRB", "pot": 4, "coefficient": 24.0},
            {"name": "Young Boys", "country": "SUI", "pot": 4, "coefficient": 22.0},
            {"name": "Dinamo Zagreb", "country": "CRO", "pot": 4, "coefficient": 20.0},
            {"name": "Sparta Prague", "country": "CZE", "pot": 4, "coefficient": 18.0},
            {"name": "Slovan Bratislava", "country": "SVK", "pot": 4, "coefficient": 16.0},
        ]
    
    def draw(self) -> DrawResult:
        """
        Execute the draw.
        
        Returns:
            DrawResult with all assignments
        """
        if not self.clubs:
            self.load_clubs()
        
        # Reset all clubs
        for club in self.clubs:
            club.reset()
        
        self.backtrack_count = 0
        
        # Clear caches for fresh draw
        self._valid_cache.clear()
        self._opponent_count_cache.clear()
        
        if self.verbose:
            print(f"\n[DRAW] Starting {self.competition.value} Draw")
            print("=" * 50)
        
        # Execute draw with optimized backtracking
        success = self._draw_with_backtracking_optimized()
        
        if self.verbose:
            if success:
                print(f"\n[OK] Draw complete! (Backtracked {self.backtrack_count} times)")
            else:
                print(f"\n[FAILED] Draw failed after {self.backtrack_count} backtracks")
        
        # Create result
        result = DrawResult(
            competition=self.competition,
            clubs=self.clubs,
            is_valid=success,
            backtrack_count=self.backtrack_count
        )
        
        # Generate fixtures
        if success:
            result.fixtures = self._generate_fixtures()
        
        return result
    
    def _draw_with_backtracking_optimized(self) -> bool:
        """
        Execute draw with optimized backtracking algorithm.
        
        Optimizations applied:
        1. MRV heuristic: pick most constrained variable first
        2. Forward checking: prune when domain becomes empty
        3. Degree heuristic: break ties by most constraints
        
        Returns:
            True if successful
        """
        # Shuffle clubs within pots
        shuffled = []
        for pot in range(1, 5):
            pot_clubs = [c for c in self.clubs if c.pot == pot]
            random.shuffle(pot_clubs)
            shuffled.extend(pot_clubs)
        
        return self._assign_opponents_optimized(shuffled)
    
    def _assign_opponents_optimized(self, clubs: List[Club]) -> bool:
        """
        Optimized recursive opponent assignment.
        
        Uses MRV heuristic and forward checking for efficiency.
        Tail-recursion friendly design for potential optimization.
        """
        # Early termination check
        if self.backtrack_count > self.max_backtrack:
            return False
        
        # Find all incomplete clubs
        incomplete = [c for c in clubs if not c.is_complete]
        
        # Base case: all complete
        if not incomplete:
            return True
        
        # Forward checking: any club with no valid options?
        for club in incomplete:
            valid_count = self._count_valid_opponents_cached(club, clubs)
            if valid_count == 0:
                return False  # Prune early
        
        # MRV Heuristic: pick club with minimum remaining values
        club = min(incomplete, key=lambda c: self._count_valid_opponents_cached(c, clubs))
        
        # Determine which type of match we need
        needs_home = club.needs_home > 0
        needs_away = club.needs_away > 0
        
        if not needs_home and not needs_away:
            return self._assign_opponents_optimized(clubs)
        
        # Try home first, then away (can be adjusted based on statistics)
        directions = []
        if needs_home:
            directions.append(True)
        if needs_away:
            directions.append(False)
        
        for as_home in directions:
            # Get valid opponents sorted by their constraint level (most constrained first)
            candidates = self._get_valid_opponents_sorted(club, clubs, as_home)
            
            for opponent in candidates:
                # Make assignment
                if as_home:
                    club.home_opponents.append(opponent)
                    opponent.away_opponents.append(club)
                else:
                    club.away_opponents.append(opponent)
                    opponent.home_opponents.append(club)
                
                # Invalidate cache for affected clubs
                self._invalidate_cache(club.name)
                self._invalidate_cache(opponent.name)
                
                if self.verbose:
                    loc = "[H]" if as_home else "[A]"
                    print(f"  {loc} {club.name} vs {opponent.name}")
                
                # Recurse
                if self._assign_opponents_optimized(clubs):
                    return True
                
                # Backtrack
                self.backtrack_count += 1
                if self.verbose and self.backtrack_count % 500 == 0:
                    print(f"  [!] Backtracking... ({self.backtrack_count})")
                
                if as_home:
                    club.home_opponents.remove(opponent)
                    opponent.away_opponents.remove(club)
                else:
                    club.away_opponents.remove(opponent)
                    opponent.home_opponents.remove(club)
                
                # Invalidate cache after backtrack
                self._invalidate_cache(club.name)
                self._invalidate_cache(opponent.name)
        
        return False
    
    def _count_valid_opponents_cached(self, club: Club, all_clubs: List[Club]) -> int:
        """
        Count valid opponents with caching.
        
        Cache is invalidated when assignments change.
        """
        cache_key = f"{club.name}_{len(club.home_opponents)}_{len(club.away_opponents)}"
        
        if cache_key in self._opponent_count_cache:
            return self._opponent_count_cache[cache_key]
        
        count = self._count_valid_opponents(club, all_clubs)
        self._opponent_count_cache[cache_key] = count
        return count
    
    def _invalidate_cache(self, club_name: str) -> None:
        """Invalidate cache entries for a club."""
        keys_to_remove = [k for k in self._opponent_count_cache if club_name in k]
        for k in keys_to_remove:
            del self._opponent_count_cache[k]
    
    def _count_valid_opponents(self, club: Club, all_clubs: List[Club]) -> int:
        """Count valid opponents for a club (non-cached version)."""
        count = 0
        for other in all_clubs:
            if other == club or other.is_complete:
                continue
            if club.needs_home > 0:
                valid, _ = self.constraints.can_pair(club, other, True)
                if valid:
                    count += 1
            if club.needs_away > 0:
                valid, _ = self.constraints.can_pair(club, other, False)
                if valid:
                    count += 1
        return count
    
    def _get_valid_opponents(self, club: Club, all_clubs: List[Club], as_home: bool) -> List[Club]:
        """Get all valid opponents for a match."""
        valid = []
        for other in all_clubs:
            if other == club or other.is_complete:
                continue
            
            can_pair, _ = self.constraints.can_pair(club, other, as_home)
            if can_pair:
                # Also check reverse is possible
                reverse, _ = self.constraints.can_pair(other, club, not as_home)
                if reverse:
                    valid.append(other)
        return valid
    
    def _get_valid_opponents_sorted(self, club: Club, all_clubs: List[Club], as_home: bool) -> List[Club]:
        """
        Get valid opponents sorted by constraint level.
        
        Returns opponents that are most constrained first,
        to fail fast if assignment is impossible.
        """
        valid = self._get_valid_opponents(club, all_clubs, as_home)
        
        # Sort by number of valid opponents (ascending = most constrained first)
        # with randomization to break ties
        random.shuffle(valid)  # Initial shuffle for randomness
        valid.sort(key=lambda c: self._count_valid_opponents(c, all_clubs))
        
        return valid
    
    def _generate_fixtures(self) -> List[Fixture]:
        """Generate fixture list from assignments."""
        fixtures = []
        seen = set()
        
        for club in self.clubs:
            for opp in club.home_opponents:
                key = (club.name, opp.name)
                if key not in seen:
                    fixtures.append(Fixture(home=club, away=opp))
                    seen.add(key)
        
        return fixtures
    
    def simulate(self, n: int = 1000) -> Dict:
        """
        Run Monte Carlo simulation.
        
        Args:
            n: Number of simulations
            
        Returns:
            Statistics dictionary
        """
        self.verbose = False
        
        matchup_counts = {}
        success_count = 0
        total_backtracks = 0
        
        print(f"[SIM] Running {n} simulations...")
        
        for i in range(n):
            result = self.draw()
            
            if result.is_valid:
                success_count += 1
                total_backtracks += result.backtrack_count
                
                # Count matchups
                for fixture in result.fixtures:
                    key = tuple(sorted([fixture.home.name, fixture.away.name]))
                    matchup_counts[key] = matchup_counts.get(key, 0) + 1
            
            if (i + 1) % 100 == 0:
                print(f"  Progress: {i + 1}/{n}")
        
        # Find most/least common matchups
        sorted_matchups = sorted(matchup_counts.items(), key=lambda x: x[1], reverse=True)
        
        return {
            "simulations": n,
            "success_rate": success_count / n,
            "avg_backtracks": total_backtracks / max(success_count, 1),
            "most_common": sorted_matchups[:10],
            "least_common": sorted_matchups[-10:]
        }


def main():
    """Main entry point."""
    parser = argparse.ArgumentParser(description="UEFA Draw Simulator")
    parser.add_argument(
        "--competition", "-c",
        choices=["champions_league", "europa_league", "conference_league"],
        default="champions_league",
        help="Competition to simulate"
    )
    parser.add_argument(
        "--simulate", "-s",
        type=int,
        default=0,
        help="Run N simulations for statistics"
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=None,
        help="Random seed for reproducibility"
    )
    parser.add_argument(
        "--output", "-o",
        type=str,
        default=None,
        help="Output file for results (JSON)"
    )
    parser.add_argument(
        "--input", "-i",
        type=str,
        default=None,
        help="Input file with teams (TOML/JSON/CSV/TXT). TOML preferred."
    )
    parser.add_argument(
        "--interactive",
        action="store_true",
        help="Enter teams interactively via console"
    )
    parser.add_argument(
        "--all-competitions",
        action="store_true",
        help="Run draw for all three competitions"
    )
    
    args = parser.parse_args()
    
    # Map competition
    comp_map = {
        "champions_league": Competition.CHAMPIONS_LEAGUE,
        "europa_league": Competition.EUROPA_LEAGUE,
        "conference_league": Competition.CONFERENCE_LEAGUE
    }
    
    if args.all_competitions:
        # Run all three competitions
        run_all_competitions(args)
    elif args.interactive:
        # Interactive mode
        run_interactive(args)
    else:
        # Single competition
        competition = comp_map[args.competition]
        run_single_draw(competition, args)


def run_single_draw(competition: Competition, args) -> Optional[DrawResult]:
    """Run draw for a single competition."""
    simulator = UEFADrawSimulator(
        competition=competition,
        seed=args.seed,
        verbose=args.simulate == 0
    )
    
    # Load teams from file if provided
    if args.input:
        try:
            simulator.load_from_file(args.input)
        except Exception as e:
            print(f"[ERROR] Error loading file: {e}")
            sys.exit(1)
    
    if args.simulate > 0:
        # Run Monte Carlo simulation
        stats = simulator.simulate(args.simulate)
        
        print(f"\n[STATS] Simulation Results - {competition.value}")
        print("=" * 50)
        print(f"Success Rate: {stats['success_rate']*100:.1f}%")
        print(f"Avg Backtracks: {stats['avg_backtracks']:.1f}")
        print(f"\n[TOP] Most Common Matchups:")
        for (t1, t2), count in stats["most_common"]:
            pct = count / args.simulate * 100
            print(f"  {t1} vs {t2}: {pct:.1f}%")
        return None
    else:
        # Single draw
        result = simulator.draw()
        result.print_summary()
        
        # Validate
        is_valid, violations = simulator.constraints.validate_final_draw(result.clubs)
        if not is_valid:
            print("\n[WARN] Violations found:")
            for v in violations:
                print(f"  - {v}")
        
        # Save if requested
        if args.output:
            with open(args.output, "w") as f:
                json.dump(result.to_dict(), f, indent=2)
            print(f"\n[SAVED] Results saved to {args.output}")
        
        return result


def run_all_competitions(args):
    """Run draw for all three UEFA competitions."""
    print("=" * 60)
    print("UEFA European Draws 2025/26")
    print("=" * 60)
    
    competitions = [
        Competition.CHAMPIONS_LEAGUE,
        Competition.EUROPA_LEAGUE,
        Competition.CONFERENCE_LEAGUE
    ]
    
    results = {}
    
    for comp in competitions:
        print(f"\n{'='*60}")
        print(f"[DRAW] {comp.value}")
        print("=" * 60)
        
        simulator = UEFADrawSimulator(
            competition=comp,
            seed=args.seed,
            verbose=True
        )
        
        # Load from file if provided
        if args.input:
            try:
                simulator.load_from_file(args.input)
            except:
                simulator.load_clubs()  # Fallback to defaults
        else:
            simulator.load_clubs()
        
        result = simulator.draw()
        result.print_summary()
        
        results[comp.value] = result
    
    # Summary
    print("\n" + "=" * 60)
    print("[SUMMARY]")
    print("=" * 60)
    
    for comp_name, result in results.items():
        status = "[OK]" if result.is_valid else "[FAILED]"
        print(f"{status} {comp_name}: {len(result.fixtures)} fixtures ({result.backtrack_count} backtracks)")
    
    # Save all results
    if args.output:
        all_results = {
            name: result.to_dict() for name, result in results.items()
        }
        with open(args.output, "w") as f:
            json.dump(all_results, f, indent=2)
        print(f"\n[SAVED] All results saved to {args.output}")


def run_interactive(args):
    """Run interactive mode for entering teams."""
    print("=" * 60)
    print("UEFA Draw - Modo Interativo")
    print("=" * 60)
    
    # Choose competition
    print("\nCompeticoes disponiveis:")
    print("  1. Champions League")
    print("  2. Europa League")
    print("  3. Conference League")
    
    while True:
        choice = input("\nEscolha (1-3): ").strip()
        if choice == "1":
            competition = Competition.CHAMPIONS_LEAGUE
            break
        elif choice == "2":
            competition = Competition.EUROPA_LEAGUE
            break
        elif choice == "3":
            competition = Competition.CONFERENCE_LEAGUE
            break
        print("[ERROR] Opcao invalida")
    
    print(f"\n[OK] Selecionado: {competition.value}")
    
    # Get teams from user
    pots = create_teams_from_input()
    
    # Count teams
    total = sum(len(teams) for teams in pots.values())
    print(f"\n[INFO] Total: {total} equipas")
    
    if total < 4:
        print("[ERROR] Minimo 4 equipas necessarias")
        return
    
    # Run draw
    simulator = UEFADrawSimulator(
        competition=competition,
        seed=args.seed,
        verbose=True
    )
    
    simulator.load_from_pots(pots)
    result = simulator.draw()
    result.print_summary()
    
    # Save if requested
    if args.output:
        with open(args.output, "w") as f:
            json.dump(result.to_dict(), f, indent=2)
        print(f"\n[SAVED] Results saved to {args.output}")


if __name__ == "__main__":
    main()
