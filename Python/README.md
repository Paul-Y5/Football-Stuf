# Football Data & Simulations

<div align="center">

![Football](https://img.shields.io/badge/Football-Data-green?style=for-the-badge)
![Python](https://img.shields.io/badge/Python-3.12+-blue?style=for-the-badge&logo=python)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

**Football data analysis and UEFA competition simulators**

[Rankings Dashboard](#-data-register-rankfut) | [UEFA Draw Simulator](#-uefa-draw-simulator)

</div>

---

## Overview

This repository contains two Python projects focused on football data analysis and simulations:

- **Data-Register-RankFut** - Interactive dashboard for worldwide club rankings
- **Sorteio_Jogos** - UEFA Champions/Europa/Conference League draw simulator

---

## Projects

### Data-Register-RankFut

Interactive dashboard for analysis and visualization of worldwide football club rankings.

**Features:**
- Load and analyze 2800+ clubs from CSV data
- Country/league comparisons (avg ranking, points, competitiveness)
- Statistical analysis (std deviation, distribution brackets)
- Interactive visualizations (Plotly/Matplotlib)
- World strength map

**Structure:**
```
Data-Register-RankFut/
    models.py              # Club, CountryStats dataclasses
    data_loader.py         # CSV loading and parsing
    analytics.py           # Statistical analysis engine
    visualizations.py      # Charts (Plotly/Matplotlib)
    dashboard.py           # Interactive CLI dashboard
    tests/                 # Unit tests (50 tests)
```

**Usage:**
```bash
cd Data-Register-RankFut
pip install plotly  # Optional, for visualizations
python3 dashboard.py
```

[Full Documentation](./Data-Register-RankFut/README.md)

---

### UEFA Draw Simulator

Faithful implementation of the official UEFA draw algorithm using backtracking with MRV heuristic.

**Features:**
- Champions League (36 teams, 8 matches each)
- Europa League (36 teams, 8 matches each)
- Conference League (36 teams, 6 matches each)
- All UEFA constraints (country protection, pot rules, home/away balance)
- TOML/JSON data format support

**Structure:**
```
Sorteio_Jogos/
    models.py              # Club, Fixture, DrawResult
    draw_simulator.py      # Backtracking algorithm
    data_loader.py         # TOML/JSON/CSV loading
    data/
        teams_2025.toml    # Team configurations
    tests/                 # Unit tests (40 tests)
```

**Usage:**
```bash
cd Sorteio_Jogos
python3 draw_simulator.py
```

[Full Documentation](./Sorteio_Jogos/README.md)

---

## Related Project

**GameAnalytics** - AI-powered player scouting system (separate repository)

> This repository was the starting point and inspiration for the larger GameAnalytics project. The experience gained from building these data analysis tools and UEFA simulations led to the development of a full AI-powered scouting system.

- Player detection with YOLO
- Multi-object tracking
- Scout report generation
- [View Repository](https://github.com/YOUR_USERNAME/GameAnalytics)

---

## Quick Start

```bash
# Clone
git clone https://github.com/YOUR_USERNAME/Football-Stuff.git
cd Football-Stuff/Python

# Rankings Dashboard
cd Data-Register-RankFut
python3 dashboard.py

# UEFA Draw Simulator
cd ../Sorteio_Jogos
python3 draw_simulator.py
```

## Tests

```bash
# Data-Register-RankFut (50 tests)
cd Data-Register-RankFut
python3 -m pytest tests/ -v

# Sorteio_Jogos (40 tests)
cd ../Sorteio_Jogos
python3 -m pytest tests/ -v
```

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Python 3.12+ |
| Data | TOML, JSON, CSV |
| Visualization | Plotly, Matplotlib |
| Testing | pytest |

---

## License

MIT License
