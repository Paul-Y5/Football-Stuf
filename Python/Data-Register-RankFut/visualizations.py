"""
Visualizations for Football Club Rankings
==========================================

Charts and graphs using Plotly for interactive visualizations.
"""
from typing import List, Dict, Optional, Tuple
from models import Club, CountryStats
from analytics import LeagueAnalytics

# Try to import plotly, fall back to matplotlib if not available
try:
    import plotly.express as px
    import plotly.graph_objects as go
    from plotly.subplots import make_subplots
    PLOTLY_AVAILABLE = True
except ImportError:
    PLOTLY_AVAILABLE = False

try:
    import matplotlib.pyplot as plt
    import matplotlib.patches as mpatches
    MATPLOTLIB_AVAILABLE = True
except ImportError:
    MATPLOTLIB_AVAILABLE = False


class Visualizer:
    """
    Create visualizations for club ranking data.
    
    Supports Plotly (interactive) and Matplotlib (static) backends.
    """
    
    def __init__(self, analytics: LeagueAnalytics, use_plotly: bool = True):
        """
        Initialize visualizer.
        
        Args:
            analytics: LeagueAnalytics instance with data.
            use_plotly: Use Plotly if available, else Matplotlib.
        """
        self.analytics = analytics
        self.use_plotly = use_plotly and PLOTLY_AVAILABLE
        
        if not self.use_plotly and not MATPLOTLIB_AVAILABLE:
            raise ImportError("Neither Plotly nor Matplotlib available")
    
    def top_n_clubs_by_country(
        self, 
        country: str, 
        n: int = 10,
        show: bool = True
    ):
        """
        Bar chart of top N clubs from a country.
        
        Args:
            country: Country name.
            n: Number of clubs to show.
            show: Whether to display the chart.
            
        Returns:
            Plotly Figure or Matplotlib Figure.
        """
        stats = self.analytics.countries.get(country)
        if stats is None:
            raise ValueError(f"Country not found: {country}")
        
        clubs = stats.get_top_n(n)
        names = [c.name for c in clubs]
        points = [c.points for c in clubs]
        rankings = [c.ranking for c in clubs]
        
        if self.use_plotly:
            fig = go.Figure()
            fig.add_trace(go.Bar(
                x=names,
                y=points,
                text=[f"#{r}" for r in rankings],
                textposition='outside',
                marker_color='steelblue',
                hovertemplate="<b>%{x}</b><br>Points: %{y}<br>World Rank: %{text}<extra></extra>"
            ))
            fig.update_layout(
                title=f"Top {n} Clubs from {country}",
                xaxis_title="Club",
                yaxis_title="Points",
                xaxis_tickangle=-45,
                template="plotly_white"
            )
            if show:
                fig.show()
            return fig
        else:
            fig, ax = plt.subplots(figsize=(12, 6))
            bars = ax.bar(names, points, color='steelblue')
            ax.set_xlabel('Club')
            ax.set_ylabel('Points')
            ax.set_title(f'Top {n} Clubs from {country}')
            plt.xticks(rotation=45, ha='right')
            
            # Add ranking labels
            for bar, rank in zip(bars, rankings):
                ax.text(bar.get_x() + bar.get_width()/2, bar.get_height(),
                       f'#{rank}', ha='center', va='bottom', fontsize=8)
            
            plt.tight_layout()
            if show:
                plt.show()
            return fig
    
    def country_comparison(
        self,
        countries: List[str],
        metric: str = "avg_points",
        show: bool = True
    ):
        """
        Compare multiple countries by a metric.
        
        Args:
            countries: List of country names.
            metric: Metric to compare (avg_points, avg_ranking, std_dev_points, total_clubs).
            show: Whether to display.
            
        Returns:
            Figure object.
        """
        valid_metrics = {
            "avg_points": ("Average Points", True),  # (label, higher_is_better)
            "avg_ranking": ("Average Ranking", False),
            "std_dev_points": ("Points Std Dev", False),
            "total_clubs": ("Total Clubs", True),
            "top_100_count": ("Clubs in Top 100", True),
        }
        
        if metric not in valid_metrics:
            raise ValueError(f"Invalid metric: {metric}")
        
        label, _ = valid_metrics[metric]
        
        data = []
        for country in countries:
            stats = self.analytics.countries.get(country)
            if stats:
                value = getattr(stats, metric)
                data.append((country, value))
        
        if not data:
            raise ValueError("No valid countries found")
        
        names, values = zip(*data)
        
        if self.use_plotly:
            fig = go.Figure()
            fig.add_trace(go.Bar(
                x=list(names),
                y=list(values),
                marker_color=['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', 
                             '#9467bd', '#8c564b', '#e377c2', '#7f7f7f'][:len(names)],
                text=[f"{v:.1f}" for v in values],
                textposition='outside'
            ))
            fig.update_layout(
                title=f"Country Comparison: {label}",
                xaxis_title="Country",
                yaxis_title=label,
                template="plotly_white"
            )
            if show:
                fig.show()
            return fig
        else:
            fig, ax = plt.subplots(figsize=(10, 6))
            colors = plt.cm.tab10(range(len(names)))
            bars = ax.bar(names, values, color=colors)
            ax.set_xlabel('Country')
            ax.set_ylabel(label)
            ax.set_title(f'Country Comparison: {label}')
            
            for bar, val in zip(bars, values):
                ax.text(bar.get_x() + bar.get_width()/2, bar.get_height(),
                       f'{val:.1f}', ha='center', va='bottom')
            
            plt.tight_layout()
            if show:
                plt.show()
            return fig
    
    def world_strength_map(self, min_clubs: int = 5, show: bool = True):
        """
        Choropleth map showing country strength scores.
        
        Args:
            min_clubs: Minimum clubs to include country.
            show: Whether to display.
            
        Returns:
            Plotly Figure (choropleth requires Plotly).
        """
        if not self.use_plotly:
            raise NotImplementedError("World map requires Plotly")
        
        # Get strength scores
        scores = self.analytics.rank_by_strength(min_clubs)
        
        # Map country names to ISO codes (simplified mapping)
        country_to_iso = {
            "England": "GBR", "Spain": "ESP", "Germany": "DEU", "Italy": "ITA",
            "France": "FRA", "Portugal": "PRT", "Netherlands": "NLD", "Belgium": "BEL",
            "Brazil": "BRA", "Argentina": "ARG", "Mexico": "MEX", "USA": "USA",
            "Scotland": "GBR", "Turkey": "TUR", "Russia": "RUS", "Ukraine": "UKR",
            "Greece": "GRC", "Switzerland": "CHE", "Austria": "AUT", "Denmark": "DNK",
            "Sweden": "SWE", "Norway": "NOR", "Poland": "POL", "Czech Republic": "CZE",
            "Croatia": "HRV", "Serbia": "SRB", "Romania": "ROU", "Hungary": "HUN",
            "Japan": "JPN", "South Korea": "KOR", "China": "CHN", "Australia": "AUS",
            "Colombia": "COL", "Chile": "CHL", "Peru": "PER", "Ecuador": "ECU",
            "Uruguay": "URY", "Paraguay": "PRY", "Venezuela": "VEN", "Bolivia": "BOL",
            "Egypt": "EGY", "Morocco": "MAR", "Tunisia": "TUN", "Algeria": "DZA",
            "South Africa": "ZAF", "Nigeria": "NGA", "Ghana": "GHA", "Cameroon": "CMR",
            "Ivory Coast": "CIV", "Senegal": "SEN", "Saudi Arabia": "SAU", "UAE": "ARE",
            "Iran": "IRN", "Israel": "ISR", "Cyprus": "CYP", "Bulgaria": "BGR",
            "Slovakia": "SVK", "Slovenia": "SVN", "Finland": "FIN", "Iceland": "ISL",
            "Ireland": "IRL", "Wales": "GBR", "Northern Ireland": "GBR",
        }
        
        countries_iso = []
        strength_scores = []
        country_names = []
        
        for country, score in scores:
            iso = country_to_iso.get(country)
            if iso:
                countries_iso.append(iso)
                strength_scores.append(score)
                country_names.append(country)
        
        fig = go.Figure(data=go.Choropleth(
            locations=countries_iso,
            z=strength_scores,
            text=country_names,
            colorscale='RdYlGn_r',  # Reversed: green=low score=strong
            reversescale=False,
            marker_line_color='darkgray',
            marker_line_width=0.5,
            colorbar_title='Strength Score<br>(Lower = Stronger)',
            hovertemplate="<b>%{text}</b><br>Score: %{z:.1f}<extra></extra>"
        ))
        
        fig.update_layout(
            title='World Football Strength Map',
            geo=dict(
                showframe=False,
                showcoastlines=True,
                projection_type='natural earth'
            ),
            template="plotly_white"
        )
        
        if show:
            fig.show()
        return fig
    
    def ranking_distribution(
        self,
        countries: List[str],
        show: bool = True
    ):
        """
        Show distribution of clubs across ranking brackets.
        
        Args:
            countries: Countries to compare.
            show: Whether to display.
            
        Returns:
            Figure object.
        """
        distribution = self.analytics.get_distribution_brackets()
        brackets = ["1-50", "51-100", "101-250", "251-500", "501-1000", "1001+"]
        
        if self.use_plotly:
            fig = go.Figure()
            
            for country in countries:
                if country in distribution:
                    values = [distribution[country][b] for b in brackets]
                    fig.add_trace(go.Bar(
                        name=country,
                        x=brackets,
                        y=values,
                        text=values,
                        textposition='auto'
                    ))
            
            fig.update_layout(
                title="Club Distribution by World Ranking Bracket",
                xaxis_title="Ranking Bracket",
                yaxis_title="Number of Clubs",
                barmode='group',
                template="plotly_white"
            )
            
            if show:
                fig.show()
            return fig
        else:
            fig, ax = plt.subplots(figsize=(12, 6))
            x = range(len(brackets))
            width = 0.8 / len(countries)
            
            for i, country in enumerate(countries):
                if country in distribution:
                    values = [distribution[country][b] for b in brackets]
                    offset = (i - len(countries)/2 + 0.5) * width
                    ax.bar([xi + offset for xi in x], values, width, label=country)
            
            ax.set_xlabel('Ranking Bracket')
            ax.set_ylabel('Number of Clubs')
            ax.set_title('Club Distribution by World Ranking Bracket')
            ax.set_xticks(x)
            ax.set_xticklabels(brackets)
            ax.legend()
            
            plt.tight_layout()
            if show:
                plt.show()
            return fig
    
    def trend_analysis(
        self,
        country: str,
        n: int = 20,
        show: bool = True
    ):
        """
        Show clubs with biggest improvements/declines in a country.
        
        Args:
            country: Country name.
            n: Number of clubs per direction.
            show: Whether to display.
            
        Returns:
            Figure object.
        """
        stats = self.analytics.countries.get(country)
        if stats is None:
            raise ValueError(f"Country not found: {country}")
        
        # Sort by year change
        sorted_clubs = sorted(stats.clubs, key=lambda c: c.year_change, reverse=True)
        
        # Get top improvers and decliners
        improvers = [c for c in sorted_clubs if c.year_change > 0][:n]
        decliners = [c for c in sorted_clubs if c.year_change < 0][-n:]
        
        all_clubs = improvers + decliners
        names = [c.name for c in all_clubs]
        changes = [c.year_change for c in all_clubs]
        colors = ['green' if c > 0 else 'red' for c in changes]
        
        if self.use_plotly:
            fig = go.Figure()
            fig.add_trace(go.Bar(
                x=names,
                y=changes,
                marker_color=colors,
                text=[f"{c:+d}" for c in changes],
                textposition='outside',
                hovertemplate="<b>%{x}</b><br>Change: %{y:+d} positions<extra></extra>"
            ))
            fig.update_layout(
                title=f"Ranking Changes - {country}",
                xaxis_title="Club",
                yaxis_title="Position Change (1 Year)",
                xaxis_tickangle=-45,
                template="plotly_white"
            )
            fig.add_hline(y=0, line_dash="dash", line_color="gray")
            
            if show:
                fig.show()
            return fig
        else:
            fig, ax = plt.subplots(figsize=(14, 6))
            ax.bar(names, changes, color=colors)
            ax.axhline(y=0, color='gray', linestyle='--')
            ax.set_xlabel('Club')
            ax.set_ylabel('Position Change (1 Year)')
            ax.set_title(f'Ranking Changes - {country}')
            plt.xticks(rotation=45, ha='right')
            
            plt.tight_layout()
            if show:
                plt.show()
            return fig
    
    def league_competitiveness_scatter(
        self,
        min_clubs: int = 10,
        show: bool = True
    ):
        """
        Scatter plot: Average Points vs Standard Deviation.
        
        Shows league quality (avg points) vs competitiveness (std dev).
        
        Args:
            min_clubs: Minimum clubs per country.
            show: Whether to display.
            
        Returns:
            Figure object.
        """
        countries_data = [
            (c.country, c.avg_points, c.std_dev_points, c.total_clubs)
            for c in self.analytics.countries.values()
            if c.total_clubs >= min_clubs
        ]
        
        if not countries_data:
            raise ValueError("No countries with enough clubs")
        
        names, avg_pts, std_devs, sizes = zip(*countries_data)
        
        if self.use_plotly:
            fig = px.scatter(
                x=list(avg_pts),
                y=list(std_devs),
                size=list(sizes),
                text=list(names),
                labels={
                    'x': 'Average Points (Quality)',
                    'y': 'Std Dev (Lower = More Competitive)'
                },
                title='League Quality vs Competitiveness',
                size_max=50
            )
            fig.update_traces(
                textposition='top center',
                hovertemplate="<b>%{text}</b><br>Avg Points: %{x:.0f}<br>Std Dev: %{y:.1f}<extra></extra>"
            )
            fig.update_layout(template="plotly_white")
            
            if show:
                fig.show()
            return fig
        else:
            fig, ax = plt.subplots(figsize=(12, 8))
            
            # Normalize sizes for matplotlib
            size_norm = [s * 5 for s in sizes]
            
            scatter = ax.scatter(avg_pts, std_devs, s=size_norm, alpha=0.6)
            
            for name, x, y in zip(names, avg_pts, std_devs):
                ax.annotate(name, (x, y), textcoords="offset points",
                           xytext=(0, 10), ha='center', fontsize=8)
            
            ax.set_xlabel('Average Points (Quality)')
            ax.set_ylabel('Std Dev (Lower = More Competitive)')
            ax.set_title('League Quality vs Competitiveness')
            
            plt.tight_layout()
            if show:
                plt.show()
            return fig


def create_visualizer(analytics: LeagueAnalytics, use_plotly: bool = True) -> Visualizer:
    """
    Factory function to create visualizer.
    
    Args:
        analytics: LeagueAnalytics instance.
        use_plotly: Prefer Plotly if available.
        
    Returns:
        Visualizer instance.
    """
    return Visualizer(analytics, use_plotly)
