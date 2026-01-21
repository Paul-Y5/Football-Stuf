#!/usr/bin/env python3
"""
Data Loader Module
==================

Carrega equipas de ficheiros TOML, JSON, CSV ou TXT para as competicoes UEFA.
Suporta Champions League, Europa League e Conference League.

Inspirado na funcionalidade do ficheiro legacy/sorteio.py

NOTA: Este modulo serve como teste comparativo entre formatos de dados:
- TOML: Formato preferido para configuracao manual (mais legivel, suporta comentarios)
- JSON: Formato tradicional para dados estruturados (mais verboso)
- CSV: Formato tabular simples
- TXT: Formato legacy pipe-delimited

Vantagens do TOML sobre JSON:
1. Suporte nativo para comentarios
2. Sintaxe mais limpa e legivel
3. Melhor para ficheiros editados manualmente
4. Tipagem mais explicita
"""
import json
import csv
from pathlib import Path
from typing import Optional, Union
from dataclasses import dataclass

# TOML support - tomllib built-in desde Python 3.11
try:
    import tomllib  # Python 3.11+
except ImportError:
    try:
        import tomli as tomllib  # Fallback para versoes anteriores
    except ImportError:
        tomllib = None

from models import Club, Competition


@dataclass
class CompetitionData:
    """Dados completos de uma competição."""
    name: str
    competition: Competition
    teams_count: int
    matches_per_team: int
    home_matches: int
    away_matches: int
    pots: dict[int, list[Club]]
    
    @property
    def all_clubs(self) -> list[Club]:
        """Retorna todas as equipas de todos os potes."""
        clubs = []
        for pot_num, pot_clubs in sorted(self.pots.items()):
            clubs.extend(pot_clubs)
        return clubs


class DataLoader:
    """
    Carregador de dados para competicoes UEFA.
    
    Suporta multiplos formatos (teste comparativo TOML vs JSON):
    - TOML: Formato preferido para configuracao (Python 3.11+ ou tomli)
    - JSON: Formato estruturado tradicional
    - CSV: Lista de equipas com colunas
    - TXT: Formato pipe-delimited (legacy)
    """
    
    # Mapeamento de códigos de competição
    COMPETITION_CODES = {
        "LC": Competition.CHAMPIONS_LEAGUE,
        "UCL": Competition.CHAMPIONS_LEAGUE,
        "CHAMPIONS": Competition.CHAMPIONS_LEAGUE,
        "CHAMPIONS_LEAGUE": Competition.CHAMPIONS_LEAGUE,
        "LE": Competition.EUROPA_LEAGUE,
        "UEL": Competition.EUROPA_LEAGUE,
        "EUROPA": Competition.EUROPA_LEAGUE,
        "EUROPA_LEAGUE": Competition.EUROPA_LEAGUE,
        "LCE": Competition.CONFERENCE_LEAGUE,
        "UECL": Competition.CONFERENCE_LEAGUE,
        "CONFERENCE": Competition.CONFERENCE_LEAGUE,
        "CONFERENCE_LEAGUE": Competition.CONFERENCE_LEAGUE,
    }
    
    def __init__(self, data_dir: Optional[Path] = None):
        """
        Inicializa o carregador.
        
        Args:
            data_dir: Directorio com os ficheiros de dados.
                      Por defeito: ./data/
        """
        if data_dir is None:
            data_dir = Path(__file__).parent / "data"
        self.data_dir = Path(data_dir)
    
    def load_toml(self, filepath: Union[str, Path]) -> dict[Competition, CompetitionData]:
        """
        Carrega competicoes de ficheiro TOML.
        
        TOML e o formato preferido para configuracao manual devido a:
        - Suporte para comentarios
        - Sintaxe mais legivel que JSON
        - Tipagem mais clara
        
        Args:
            filepath: Caminho para o ficheiro TOML
        
        Returns:
            Dicionario com dados de cada competicao
        
        Raises:
            ImportError: Se tomllib/tomli nao estiver disponivel
        """
        if tomllib is None:
            raise ImportError(
                "TOML support requires Python 3.11+ or 'tomli' package. "
                "Install with: pip install tomli"
            )
        
        filepath = Path(filepath)
        
        with open(filepath, "rb") as f:
            data = tomllib.load(f)
        
        competitions = {}
        
        for comp_key, comp_data in data.items():
            competition = self._parse_competition_code(comp_key)
            if competition is None:
                continue
            
            pots = {}
            pots_data = comp_data.get("pots", {})
            
            # TOML format: pots.pot1, pots.pot2, etc.
            for pot_key, teams in pots_data.items():
                pot_num = int(pot_key.replace("pot", ""))
                pots[pot_num] = [
                    Club(
                        name=team["name"],
                        country=team["country"],
                        pot=pot_num,
                        coefficient=team.get("coefficient", 0.0)
                    )
                    for team in teams
                ]
            
            competitions[competition] = CompetitionData(
                name=comp_data.get("name", comp_key),
                competition=competition,
                teams_count=comp_data.get("teams_count", 36),
                matches_per_team=comp_data.get("matches_per_team", 8),
                home_matches=comp_data.get("home_matches", 4),
                away_matches=comp_data.get("away_matches", 4),
                pots=pots
            )
        
        return competitions
    
    def load_json(self, filepath: Union[str, Path]) -> dict[Competition, CompetitionData]:
        """
        Carrega competições de ficheiro JSON.
        
        Args:
            filepath: Caminho para o ficheiro JSON
        
        Returns:
            Dicionário com dados de cada competição
        """
        filepath = Path(filepath)
        
        with open(filepath, "r", encoding="utf-8") as f:
            data = json.load(f)
        
        competitions = {}
        
        for comp_key, comp_data in data.items():
            competition = self._parse_competition_code(comp_key)
            if competition is None:
                continue
            
            pots = {}
            for pot_str, teams in comp_data.get("pots", {}).items():
                pot_num = int(pot_str)
                pots[pot_num] = [
                    Club(
                        name=team["name"],
                        country=team["country"],
                        pot=pot_num,
                        coefficient=team.get("coefficient", 0.0)
                    )
                    for team in teams
                ]
            
            competitions[competition] = CompetitionData(
                name=comp_data.get("name", comp_key),
                competition=competition,
                teams_count=comp_data.get("teams_count", 36),
                matches_per_team=comp_data.get("matches_per_team", 8),
                home_matches=comp_data.get("home_matches", 4),
                away_matches=comp_data.get("away_matches", 4),
                pots=pots
            )
        
        return competitions
    
    def load_csv(self, filepath: Union[str, Path], 
                 competition: Optional[Competition] = None) -> list[Club]:
        """
        Carrega equipas de ficheiro CSV.
        
        Formato esperado:
        name,country,pot,coefficient,competition
        Real Madrid,ESP,1,136.0,CHAMPIONS_LEAGUE
        
        Args:
            filepath: Caminho para o ficheiro CSV
            competition: Filtrar por competição (opcional)
        
        Returns:
            Lista de clubes
        """
        filepath = Path(filepath)
        clubs = []
        
        with open(filepath, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            
            for row in reader:
                # Verificar competição se filtro definido
                row_comp = self._parse_competition_code(row.get("competition", ""))
                if competition and row_comp != competition:
                    continue
                
                club = Club(
                    name=row["name"],
                    country=row["country"],
                    pot=int(row.get("pot", 1)),
                    coefficient=float(row.get("coefficient", 0))
                )
                clubs.append(club)
        
        return clubs
    
    def load_txt(self, filepath: Union[str, Path]) -> dict[Competition, list[Club]]:
        """
        Carrega equipas de ficheiro TXT (formato legacy pipe-delimited).
        
        Formato esperado (do ficheiro original):
        Nome|País|Pote|Ranking|Competição
        Real Madrid|ESP|1|1|LC
        
        Args:
            filepath: Caminho para o ficheiro TXT
        
        Returns:
            Dicionário com clubes por competição
        """
        filepath = Path(filepath)
        competitions: dict[Competition, list[Club]] = {}
        
        with open(filepath, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith("#"):
                    continue
                
                parts = line.split("|")
                if len(parts) < 4:
                    continue
                
                name = parts[0].strip()
                country = parts[1].strip()
                pot = int(parts[2]) if parts[2].isdigit() else 1
                
                # Competição pode estar na posição 4
                comp_code = parts[4].strip() if len(parts) > 4 else "LC"
                competition = self._parse_competition_code(comp_code)
                
                if competition is None:
                    competition = Competition.CHAMPIONS_LEAGUE
                
                if competition not in competitions:
                    competitions[competition] = []
                
                club = Club(name=name, country=country, pot=pot)
                competitions[competition].append(club)
        
        return competitions
    
    def load_from_pots(self, pots: dict[int, list[dict]]) -> list[Club]:
        """
        Carrega equipas diretamente de dicionário de potes.
        
        Args:
            pots: Dicionário {pot_number: [{"name": ..., "country": ...}, ...]}
        
        Returns:
            Lista de clubes
        """
        clubs = []
        
        for pot_num, teams in pots.items():
            for team in teams:
                club = Club(
                    name=team["name"],
                    country=team["country"],
                    pot=pot_num,
                    coefficient=team.get("coefficient", 0.0)
                )
                clubs.append(club)
        
        return clubs
    
    def load_default(self, competition: Competition = Competition.CHAMPIONS_LEAGUE) -> list[Club]:
        """
        Carrega o ficheiro de dados por defeito.
        
        Ordem de preferencia: TOML > JSON > hardcoded
        
        Args:
            competition: Competicao a carregar
        
        Returns:
            Lista de clubes
        """
        # Preferir TOML (mais legivel, suporta comentarios)
        toml_file = self.data_dir / "teams_2025.toml"
        json_file = self.data_dir / "teams_2025.json"
        
        if toml_file.exists() and tomllib is not None:
            try:
                competitions = self.load_toml(toml_file)
                if competition in competitions:
                    return competitions[competition].all_clubs
            except Exception:
                pass  # Fallback para JSON
        
        if json_file.exists():
            competitions = self.load_json(json_file)
            if competition in competitions:
                return competitions[competition].all_clubs
        
        # Fallback para dados hardcoded
        return self._get_default_clubs(competition)
    
    def _parse_competition_code(self, code: str) -> Optional[Competition]:
        """Converte código de competição para enum."""
        code = code.upper().strip().replace(" ", "_")
        return self.COMPETITION_CODES.get(code)
    
    def _get_default_clubs(self, competition: Competition) -> list[Club]:
        """Retorna clubes por defeito (hardcoded)."""
        if competition == Competition.CHAMPIONS_LEAGUE:
            return self._default_champions_league()
        elif competition == Competition.EUROPA_LEAGUE:
            return self._default_europa_league()
        else:
            return self._default_conference_league()
    
    def _default_champions_league(self) -> list[Club]:
        """Champions League por defeito."""
        # Pot 1
        pot1 = [
            Club("Real Madrid", "ESP", 1, 136.0),
            Club("Manchester City", "ENG", 1, 128.0),
            Club("Bayern Munich", "GER", 1, 124.0),
            Club("PSG", "FRA", 1, 104.0),
            Club("Liverpool", "ENG", 1, 99.0),
            Club("Inter Milan", "ITA", 1, 98.0),
            Club("Borussia Dortmund", "GER", 1, 91.0),
            Club("Barcelona", "ESP", 1, 88.0),
            Club("Leverkusen", "GER", 1, 80.0),
        ]
        
        # Pot 2
        pot2 = [
            Club("Atletico Madrid", "ESP", 2, 78.0),
            Club("Juventus", "ITA", 2, 76.0),
            Club("Benfica", "POR", 2, 75.0),
            Club("Arsenal", "ENG", 2, 72.0),
            Club("Club Brugge", "BEL", 2, 64.0),
            Club("Shakhtar", "UKR", 2, 62.0),
            Club("AC Milan", "ITA", 2, 60.0),
            Club("Atalanta", "ITA", 2, 58.0),
            Club("Sporting CP", "POR", 2, 55.0),
        ]
        
        # Pot 3
        pot3 = [
            Club("FC Porto", "POR", 3, 53.0),
            Club("Feyenoord", "NED", 3, 50.0),
            Club("PSV", "NED", 3, 48.0),
            Club("Celtic", "SCO", 3, 44.0),
            Club("Monaco", "FRA", 3, 42.0),
            Club("Aston Villa", "ENG", 3, 40.0),
            Club("Bologna", "ITA", 3, 38.0),
            Club("Lille", "FRA", 3, 36.0),
            Club("Girona", "ESP", 3, 35.0),
        ]
        
        # Pot 4
        pot4 = [
            Club("Stuttgart", "GER", 4, 33.0),
            Club("Sturm Graz", "AUT", 4, 30.0),
            Club("Brest", "FRA", 4, 28.0),
            Club("Salzburg", "AUT", 4, 26.0),
            Club("Red Star", "SRB", 4, 24.0),
            Club("Young Boys", "SUI", 4, 22.0),
            Club("Dinamo Zagreb", "CRO", 4, 20.0),
            Club("Sparta Prague", "CZE", 4, 18.0),
            Club("Slovan Bratislava", "SVK", 4, 16.0),
        ]
        
        return pot1 + pot2 + pot3 + pot4
    
    def _default_europa_league(self) -> list[Club]:
        """Europa League por defeito (exemplo reduzido)."""
        return [
            Club("Roma", "ITA", 1, 85.0),
            Club("Man United", "ENG", 1, 82.0),
            Club("Lazio", "ITA", 1, 68.0),
            Club("Tottenham", "ENG", 1, 66.0),
            Club("Ajax", "NED", 1, 64.0),
            Club("Rangers", "SCO", 1, 52.0),
            Club("Frankfurt", "GER", 1, 50.0),
            Club("Porto", "POR", 1, 48.0),
            Club("Nice", "FRA", 1, 45.0),
            # ... mais equipas
        ]
    
    def _default_conference_league(self) -> list[Club]:
        """Conference League por defeito (exemplo reduzido)."""
        return [
            Club("Chelsea", "ENG", 1, 70.0),
            Club("Copenhagen", "DEN", 1, 38.0),
            Club("Gent", "BEL", 1, 36.0),
            Club("Fiorentina", "ITA", 1, 34.0),
            Club("LASK", "AUT", 1, 32.0),
            Club("Partizan", "SRB", 1, 30.0),
            Club("Djurgarden", "SWE", 1, 28.0),
            Club("Vitoria SC", "POR", 1, 26.0),
            Club("Hearts", "SCO", 1, 24.0),
            # ... mais equipas
        ]


def create_teams_from_input() -> dict[int, list[dict]]:
    """
    Interface interativa para criar equipas por input.
    
    Returns:
        Dicionario com potes e equipas
    """
    print("=" * 60)
    print("UEFA Draw - Inserir Equipas")
    print("=" * 60)
    
    pots = {1: [], 2: [], 3: [], 4: []}
    
    for pot_num in range(1, 5):
        print(f"\n[Pote {pot_num}]")
        print("(Digite 'fim' para terminar este pote)")
        
        while True:
            team_input = input(f"  Equipa {len(pots[pot_num]) + 1}: ").strip()
            
            if team_input.lower() in ["fim", "done", "end", ""]:
                break
            
            # Formato: Nome, Pais
            parts = team_input.split(",")
            if len(parts) >= 2:
                name = parts[0].strip()
                country = parts[1].strip().upper()[:3]
            else:
                name = team_input
                country = input("    Pais (3 letras): ").strip().upper()[:3]
            
            pots[pot_num].append({
                "name": name,
                "country": country
            })
            print(f"    + {name} ({country}) adicionado")
    
    return pots


if __name__ == "__main__":
    # Demonstracao - Teste comparativo TOML vs JSON
    loader = DataLoader()
    
    print("=" * 60)
    print("Data Loader - Demo (TOML vs JSON)")
    print("=" * 60)
    
    # Tentar carregar TOML primeiro
    toml_file = loader.data_dir / "teams_2025.toml"
    json_file = loader.data_dir / "teams_2025.json"
    
    if toml_file.exists() and tomllib is not None:
        print("\n[TOML] A carregar ficheiro TOML...")
        competitions = loader.load_toml(toml_file)
        print(f"[OK] Carregado: {toml_file.name}")
    elif json_file.exists():
        print("\n[JSON] A carregar ficheiro JSON...")
        competitions = loader.load_json(json_file)
        print(f"[OK] Carregado: {json_file.name}")
    else:
        print("[ERRO] Nenhum ficheiro de dados encontrado!")
        competitions = {}
    
    for comp, data in competitions.items():
        print(f"\n{data.name}:")
        print(f"  Equipas: {data.teams_count}")
        print(f"  Jogos por equipa: {data.matches_per_team}")
        
        for pot_num, clubs in data.pots.items():
            print(f"\n  Pote {pot_num}:")
            for club in clubs[:3]:  # Mostrar apenas 3
                print(f"    - {club.name} ({club.country})")
            if len(clubs) > 3:
                print(f"    ... e mais {len(clubs) - 3} equipas")
