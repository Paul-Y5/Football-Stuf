# UEFA Draw Simulator

<div align="center">

![UEFA Draw](https://img.shields.io/badge/UEFA-Draw%20Simulator-blue?style=for-the-badge)
![Python](https://img.shields.io/badge/Python-3.10+-green?style=for-the-badge&logo=python)
![TOML](https://img.shields.io/badge/Config-TOML-orange?style=for-the-badge)

**Faithful implementation of the official UEFA draw algorithm for European competitions**

</div>

---

## Overview

This simulator implements the real UEFA draw procedures for:

- **Champions League** — League phase (36 teams, 8 opponents each)
- **Europa League** — Full draw with pot system
- **Conference League** — Complete simulation (6 matches per team)

### Key Features

| Feature | Description |
|---------|-------------|
| Country Protection | Teams from same country cannot face each other |
| Pot System | Teams divided by UEFA coefficient |
| Home/Away Balance | 4 home + 4 away matches per team |
| Max 2 per Country | Maximum 2 opponents from same country |
| Backtracking | Handles impossible draw situations |
| MRV Heuristic | Optimized recursive algorithm |

---

## Data Format: TOML vs JSON

This project serves as a **comparative test between TOML and JSON** for configuration data.

### Why TOML?

| Feature | TOML | JSON |
|---------|------|------|
| Comments | Native support | Not supported |
| Readability | More human-friendly | More verbose |
| Manual editing | Easy | Error-prone |
| Trailing commas | Allowed | Not allowed |

### File Locations

```
data/
├── teams_2025.toml  # Preferred format (human-readable)
└── teams_2025.json  # Alternative format (traditional)
```

The loader automatically prefers TOML when available.

---

## Quick Start

```bash
# Run the simulator
python draw_simulator.py

# Or with specific competition
python draw_simulator.py --competition champions_league
python draw_simulator.py --competition europa_league
python draw_simulator.py --competition conference_league

# Load from TOML file
python draw_simulator.py --input data/teams_2025.toml

# Run all three competitions
python draw_simulator.py --all-competitions
```

---

## Project Structure

```
Sorteio_Jogos/
├── draw_simulator.py    # Main draw algorithm
├── models.py            # Data classes (Club, Fixture, etc.)
├── constraints.py       # UEFA rules implementation
├── data_loader.py       # TOML/JSON/CSV/TXT loader
├── data/
│   ├── teams_2025.toml  # Team database (TOML - preferred)
│   └── teams_2025.json  # Team database (JSON - fallback)
├── tests/               # Unit tests
│   ├── test_draw.py
│   ├── test_constraints.py
│   └── test_data_loader.py
├── legacy/              # Original prototype files
│   ├── sorteio.py
│   └── fileCreate.py
├── pytest.ini           # Test configuration
└── README.md
```

---

## How It Works

### 1. Pot System (Champions League 2024/25+)

```
Pot 1: Teams 1-9 (coefficient + title holders)
Pot 2: Teams 10-18
Pot 3: Teams 19-27
Pot 4: Teams 28-36
```

### 2. Draw Rules

Each team receives **8 opponents**:
- 2 from each pot (including own pot)
- 4 home matches + 4 away matches
- No team from same country
- Maximum 2 teams from same country total

### 3. Optimized Backtracking Algorithm

When a draw becomes impossible (no valid opponent):
1. **MRV Heuristic**: Always pick the most constrained club first
2. **Forward Checking**: Prune invalid assignments early
3. Undo last assignment and try alternative
4. Repeat until valid draw found

---

## Example Output

```
UEFA Champions League 2025/26 Draw
=====================================

[Pot 3]
  FC Porto (POR)
    [H] Home: Real Madrid, Bayern Munich, Inter Milan, Ajax
    [A] Away: Man City, PSG, Dortmund, Celtic

[Pot 2]
  Benfica (POR)
    [H] Home: Liverpool, Juventus, Leipzig, Celtic
    [A] Away: Barcelona, Chelsea, Monaco, Salzburg
```

---

## Interactive Mode

```bash
python draw_simulator.py --interactive
```

Features:
- Step-by-step draw visualization
- Real-time constraint checking
- Export results to JSON

---

## Statistics Mode

```bash
python draw_simulator.py --simulate 10000
```

Run Monte Carlo simulations to analyze:
- Most common matchups
- Draw difficulty by team
- Country distribution patterns

---

## Running Tests

```bash
# Install test dependencies
pip install -r requirements-dev.txt

# Run tests
pytest tests/ -v
```

---

## TOML Configuration Example

```toml
[champions_league]
name = "UEFA Champions League 2025/26"
teams_count = 36
matches_per_team = 8

[[champions_league.pots.pot1]]
name = "Real Madrid"
country = "ESP"
coefficient = 136.0

[[champions_league.pots.pot1]]
name = "Manchester City"
country = "ENG"
coefficient = 128.0
```

---

## References

- [UEFA Official Draw Procedures](https://www.uefa.com/uefachampionsleague/draws/)
- [New Champions League Format 2024/25](https://www.uefa.com/uefachampionsleague/news/)

---

## License

MIT License — See main repository for details.
