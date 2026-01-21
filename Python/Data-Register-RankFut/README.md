# Football Club Rankings Dashboard

Dashboard interativo para análise e visualização de rankings de clubes de futebol mundiais.

## Funcionalidades

### Dashboard Interativo
- **Top N clubes por país**: Visualização em gráfico de barras dos melhores clubes de cada país
- **Mapa mundial**: Mapa coroplético mostrando a força de cada país
- **Tendências**: Análise de clubes que subiram/desceram no ranking

### Comparador de Ligas
- **Média de pontos**: Comparação da qualidade média dos clubes por país
- **Desvio padrão**: Medida de competitividade interna (menor = mais competitivo)
- **Distribuição**: Análise de quantos clubes cada país tem em cada faixa de ranking

## Estrutura do Projeto

```
Data-Register-RankFut/
    models.py              # Classes de dados (Club, CountryStats)
    data_loader.py         # Carregamento de CSV
    analytics.py           # Motor de análise estatística
    visualizations.py      # Gráficos (Plotly/Matplotlib)
    dashboard.py           # Dashboard interativo
    tests/
        test_models.py     # Testes para modelos
        test_data_loader.py # Testes para carregamento
        test_analytics.py  # Testes para análise
    Soccer_Football Clubs Ranking.csv  # Dataset
```

## Instalação

```bash
# Dependências básicas
pip install pytest

# Para visualizações interativas (recomendado)
pip install plotly

# Alternativa para gráficos estáticos
pip install matplotlib
```

## Uso

### Dashboard Interativo
```bash
python dashboard.py
```

### Uso Programático
```python
from data_loader import load_data
from analytics import create_analytics
from visualizations import create_visualizer

# Carregar dados
loader = load_data()
print(f"Carregados {len(loader.clubs)} clubes")

# Análise
analytics = create_analytics(loader.countries)

# Top países por força
for country, score in analytics.rank_by_strength()[:10]:
    print(f"{country}: {score:.1f}")

# Comparar dois países
comparison = analytics.compare("Spain", "England")
print(f"Winner by ranking: {comparison.winner_by_ranking}")

# Visualização
viz = create_visualizer(analytics)
viz.top_n_clubs_by_country("Portugal", n=10)
viz.country_comparison(["Spain", "England", "Germany"], metric="avg_points")
```

## Métricas Disponíveis

| Métrica | Descrição |
|---------|-----------|
| `avg_ranking` | Posição média no ranking mundial |
| `avg_points` | Pontuação média dos clubes |
| `std_dev_points` | Desvio padrão (competitividade) |
| `top_100_count` | Clubes no top 100 mundial |
| `total_clubs` | Total de clubes no país |

## Testes

```bash
cd Data-Register-RankFut
python -m pytest tests/ -v
```

## Dataset

O ficheiro `Soccer_Football Clubs Ranking.csv` contém:
- **2800+** clubes de todo o mundo
- Ranking, pontuação, país
- Variação anual de posição
- Tendência (subida/descida)

## Exemplos de Visualizações

### Comparação de Ligas
```python
viz.country_comparison(
    ["Spain", "England", "Germany", "Italy", "France"],
    metric="avg_points"
)
```

### Mapa Mundial de Força
```python
viz.world_strength_map(min_clubs=5)
```

### Scatter: Qualidade vs Competitividade
```python
viz.league_competitiveness_scatter(min_clubs=10)
```
