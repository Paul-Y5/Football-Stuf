"""
Interactive Dashboard for Football Club Rankings
=================================================

Main dashboard combining all visualizations and analytics.
"""
from typing import Optional
from data_loader import load_data, DataLoader
from analytics import LeagueAnalytics, create_analytics

# Try to import visualizations (optional)
try:
    from visualizations import Visualizer, create_visualizer, PLOTLY_AVAILABLE, MATPLOTLIB_AVAILABLE
    VIZ_AVAILABLE = PLOTLY_AVAILABLE or MATPLOTLIB_AVAILABLE
except ImportError:
    VIZ_AVAILABLE = False
    PLOTLY_AVAILABLE = False


class Dashboard:
    """
    Interactive dashboard for exploring football club rankings.
    
    Combines data loading, analytics, and visualizations.
    """
    
    def __init__(self, data_path: Optional[str] = None):
        """
        Initialize dashboard with data.
        
        Args:
            data_path: Optional path to CSV file.
        """
        print("Loading data...")
        self.loader = load_data(data_path)
        self.analytics = create_analytics(self.loader.countries)
        
        # Visualizer is optional
        self.viz = None
        if VIZ_AVAILABLE:
            self.viz = create_visualizer(self.analytics, use_plotly=PLOTLY_AVAILABLE)
            print("[VIZ] Visualizations enabled")
        else:
            print("[VIZ] Visualizations disabled (install plotly or matplotlib)")
        
        print(f"Loaded {len(self.loader.clubs)} clubs from {len(self.loader.countries)} countries")
    
    def show_global_stats(self) -> None:
        """Display global statistics."""
        stats = self.analytics.get_global_stats()
        
        print("\n" + "=" * 60)
        print("GLOBAL STATISTICS")
        print("=" * 60)
        print(f"  Total Clubs:      {stats['total_clubs']:,}")
        print(f"  Total Countries:  {stats['total_countries']}")
        print(f"  Average Points:   {stats['avg_points']:.1f}")
        print(f"  Max Points:       {stats['max_points']}")
        print(f"  Min Points:       {stats['min_points']}")
        print(f"  Improving Clubs:  {stats['improving_clubs']:,}")
        print(f"  Declining Clubs:  {stats['declining_clubs']:,}")
        print("=" * 60)
    
    def show_top_countries(self, n: int = 10, min_clubs: int = 5) -> None:
        """
        Display top countries by strength.
        
        Args:
            n: Number of countries to show.
            min_clubs: Minimum clubs required.
        """
        print(f"\n{'=' * 70}")
        print(f"TOP {n} STRONGEST FOOTBALL COUNTRIES (min {min_clubs} clubs)")
        print("=" * 70)
        print(f"{'Rank':<6}{'Country':<20}{'Score':<10}{'Clubs':<8}{'Avg Pts':<10}{'Top 100':<8}")
        print("-" * 70)
        
        scores = self.analytics.rank_by_strength(min_clubs)[:n]
        
        for i, (country, score) in enumerate(scores, 1):
            stats = self.loader.get_country(country)
            print(f"{i:<6}{country:<20}{score:<10.1f}{stats.total_clubs:<8}"
                  f"{stats.avg_points:<10.1f}{stats.top_100_count:<8}")
        
        print("=" * 70)
    
    def show_top_clubs(self, n: int = 20) -> None:
        """
        Display top N clubs worldwide.
        
        Args:
            n: Number of clubs to show.
        """
        print(f"\n{'=' * 80}")
        print(f"TOP {n} CLUBS WORLDWIDE")
        print("=" * 80)
        print(f"{'Rank':<8}{'Club':<30}{'Country':<15}{'Points':<10}{'Change':<10}")
        print("-" * 80)
        
        for club in self.loader.get_top_clubs(n):
            change = f"{club.year_change:+d}" if club.year_change != 0 else "="
            print(f"{club.ranking:<8}{club.name:<30}{club.country:<15}"
                  f"{club.points:<10}{change:<10}")
        
        print("=" * 80)
    
    def show_country_details(self, country: str) -> None:
        """
        Display detailed statistics for a country.
        
        Args:
            country: Country name.
        """
        stats = self.loader.get_country(country)
        if stats is None:
            print(f"Country not found: {country}")
            return
        
        print(f"\n{'=' * 60}")
        print(f"COUNTRY PROFILE: {country.upper()}")
        print("=" * 60)
        print(f"  Total Clubs:        {stats.total_clubs}")
        print(f"  Average Ranking:    {stats.avg_ranking:.1f}")
        print(f"  Average Points:     {stats.avg_points:.1f}")
        print(f"  Std Dev (Compet.):  {stats.std_dev_points:.1f}")
        print(f"  Median Ranking:     {stats.median_ranking:.1f}")
        print(f"  Clubs in Top 10:    {stats.top_10_count}")
        print(f"  Clubs in Top 50:    {stats.top_50_count}")
        print(f"  Clubs in Top 100:   {stats.top_100_count}")
        
        if stats.best_club:
            print(f"\n  Best Club:  #{stats.best_club.ranking} {stats.best_club.name}")
        if stats.worst_club:
            print(f"  Worst Club: #{stats.worst_club.ranking} {stats.worst_club.name}")
        
        print("\n  Top 10 Clubs:")
        print("  " + "-" * 50)
        for club in stats.get_top_n(10):
            print(f"    #{club.ranking:<5} {club.name:<30} {club.points} pts")
        
        print("=" * 60)
    
    def compare_countries(self, country_a: str, country_b: str) -> None:
        """
        Compare two countries side by side.
        
        Args:
            country_a: First country.
            country_b: Second country.
        """
        comparison = self.analytics.compare(country_a, country_b)
        if comparison is None:
            print("One or both countries not found")
            return
        
        a = comparison.country_a
        b = comparison.country_b
        
        print(f"\n{'=' * 70}")
        print(f"LEAGUE COMPARISON: {country_a.upper()} vs {country_b.upper()}")
        print("=" * 70)
        print(f"{'Metric':<25}{country_a:<20}{country_b:<20}{'Winner':<15}")
        print("-" * 70)
        
        # Total clubs
        winner_clubs = country_a if a.total_clubs > b.total_clubs else country_b
        print(f"{'Total Clubs':<25}{a.total_clubs:<20}{b.total_clubs:<20}{winner_clubs:<15}")
        
        # Avg ranking (lower is better)
        print(f"{'Avg Ranking':<25}{a.avg_ranking:<20.1f}{b.avg_ranking:<20.1f}"
              f"{comparison.winner_by_ranking:<15}")
        
        # Avg points (higher is better)
        winner_pts = country_a if a.avg_points > b.avg_points else country_b
        print(f"{'Avg Points':<25}{a.avg_points:<20.1f}{b.avg_points:<20.1f}{winner_pts:<15}")
        
        # Top 100 clubs
        print(f"{'Clubs in Top 100':<25}{a.top_100_count:<20}{b.top_100_count:<20}"
              f"{comparison.winner_by_top_clubs:<15}")
        
        # Competitiveness (lower std dev is more competitive)
        print(f"{'Std Dev (Compet.)':<25}{a.std_dev_points:<20.1f}{b.std_dev_points:<20.1f}"
              f"{comparison.more_competitive:<15}")
        
        # Best club
        best_a = a.best_club.ranking if a.best_club else 9999
        best_b = b.best_club.ranking if b.best_club else 9999
        winner_best = country_a if best_a < best_b else country_b
        print(f"{'Best Club Rank':<25}{best_a:<20}{best_b:<20}{winner_best:<15}")
        
        print("=" * 70)
    
    def plot_top_clubs(self, country: str, n: int = 10) -> None:
        """
        Show bar chart of top N clubs from a country.
        
        Args:
            country: Country name.
            n: Number of clubs.
        """
        if self.viz is None:
            print("Visualizations not available. Install plotly or matplotlib.")
            return
        try:
            self.viz.top_n_clubs_by_country(country, n)
        except ValueError as e:
            print(f"Error: {e}")
    
    def plot_country_comparison(self, countries: list, metric: str = "avg_points") -> None:
        """
        Show comparison chart for multiple countries.
        
        Args:
            countries: List of country names.
            metric: Metric to compare.
        """
        if self.viz is None:
            print("Visualizations not available. Install plotly or matplotlib.")
            return
        try:
            self.viz.country_comparison(countries, metric)
        except ValueError as e:
            print(f"Error: {e}")
    
    def plot_world_map(self, min_clubs: int = 5) -> None:
        """
        Show world strength map.
        
        Args:
            min_clubs: Minimum clubs to include.
        """
        if self.viz is None:
            print("Visualizations not available. Install plotly or matplotlib.")
            return
        try:
            self.viz.world_strength_map(min_clubs)
        except Exception as e:
            print(f"Error: {e}")
    
    def plot_distribution(self, countries: list) -> None:
        """
        Show ranking distribution for countries.
        
        Args:
            countries: List of country names.
        """
        if self.viz is None:
            print("Visualizations not available. Install plotly or matplotlib.")
            return
        try:
            self.viz.ranking_distribution(countries)
        except ValueError as e:
            print(f"Error: {e}")
    
    def plot_trends(self, country: str, n: int = 15) -> None:
        """
        Show trend analysis for a country.
        
        Args:
            country: Country name.
            n: Number of clubs per direction.
        """
        if self.viz is None:
            print("Visualizations not available. Install plotly or matplotlib.")
            return
        try:
            self.viz.trend_analysis(country, n)
        except ValueError as e:
            print(f"Error: {e}")
    
    def plot_competitiveness(self, min_clubs: int = 10) -> None:
        """
        Show league quality vs competitiveness scatter.
        
        Args:
            min_clubs: Minimum clubs per country.
        """
        if self.viz is None:
            print("Visualizations not available. Install plotly or matplotlib.")
            return
        try:
            self.viz.league_competitiveness_scatter(min_clubs)
        except ValueError as e:
            print(f"Error: {e}")
    
    def interactive_menu(self) -> None:
        """Run interactive menu."""
        while True:
            print("\n" + "=" * 50)
            print("FOOTBALL RANKINGS DASHBOARD")
            print("=" * 50)
            print("[1] Global Statistics")
            print("[2] Top Countries")
            print("[3] Top Clubs Worldwide")
            print("[4] Country Details")
            print("[5] Compare Two Countries")
            print("[6] Plot: Top Clubs (Country)")
            print("[7] Plot: Country Comparison")
            print("[8] Plot: World Map")
            print("[9] Plot: Ranking Distribution")
            print("[10] Plot: Trends (Country)")
            print("[11] Plot: Competitiveness Scatter")
            print("[0] Exit")
            print("-" * 50)
            
            choice = input("Choose option: ").strip()
            
            try:
                if choice == "0":
                    print("Goodbye!")
                    break
                elif choice == "1":
                    self.show_global_stats()
                elif choice == "2":
                    n = int(input("How many countries? [10]: ") or "10")
                    self.show_top_countries(n)
                elif choice == "3":
                    n = int(input("How many clubs? [20]: ") or "20")
                    self.show_top_clubs(n)
                elif choice == "4":
                    country = input("Country name: ")
                    self.show_country_details(country)
                elif choice == "5":
                    a = input("First country: ")
                    b = input("Second country: ")
                    self.compare_countries(a, b)
                elif choice == "6":
                    country = input("Country: ")
                    n = int(input("How many clubs? [10]: ") or "10")
                    self.plot_top_clubs(country, n)
                elif choice == "7":
                    countries = input("Countries (comma-separated): ").split(",")
                    countries = [c.strip() for c in countries]
                    metric = input("Metric [avg_points]: ") or "avg_points"
                    self.plot_country_comparison(countries, metric)
                elif choice == "8":
                    self.plot_world_map()
                elif choice == "9":
                    countries = input("Countries (comma-separated): ").split(",")
                    countries = [c.strip() for c in countries]
                    self.plot_distribution(countries)
                elif choice == "10":
                    country = input("Country: ")
                    self.plot_trends(country)
                elif choice == "11":
                    self.plot_competitiveness()
                else:
                    print("Invalid option")
            except Exception as e:
                print(f"Error: {e}")


def main():
    """Main entry point."""
    dashboard = Dashboard()
    dashboard.interactive_menu()


if __name__ == "__main__":
    main()
