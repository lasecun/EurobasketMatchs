#!/usr/bin/env python3
"""
Script para poblar 100% los datos estáticos de EuroLeague 2025-26

Este script descarga todos los equipos y partidos de la temporada 2025-26
desde la API oficial de EuroLeague y los guarda como a        print("📁 Archivos generados:")
        print(f"   - {TEAMS_FILE}")
        print(f"   - {MATCHES_FILE}")ivos JSON en assets
para que estén     print("🎯 Objetivo: Poblar datos estáticos para temporada 2025-26")isponibles desde la primera instalación de la app.

Uso:
    python3 scripts/populate_static_data.py

El script creará:
- app/src/main/assets/static_data/teams_2025_26.json
- app/src/main/assets/static_data/matches_calendar_2025_26.json
"""

import json
import os
import sys
import requests
from datetime import datetime
from typing import Dict, List, Any

# Configuración
API_BASE_URL = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2"
SEASON_CODE = "E2025"
TOTAL_ROUNDS = 38

# URLs de la API oficial de EuroLeague
BASE_URL = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2"
TEAMS_API_URL = f"{BASE_URL}/competitions/E/seasons/E2025/clubs"
GAMES_API_URL = f"{BASE_URL}/competitions/E/seasons/E2025/games"

# Rutas de archivos
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
ASSETS_DIR = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets", "static_data")
TEAMS_FILE = os.path.join(ASSETS_DIR, "teams_2025_26.json")
MATCHES_FILE = os.path.join(ASSETS_DIR, "matches_calendar_2025_26.json")

class EuroLeagueDataPopulator:
    """Poblador de datos estáticos de EuroLeague"""
    
    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'EuroLeagueApp/1.0',
            'Accept': 'application/json'
        })
        
    def create_assets_directory(self):
        """Crea el directorio assets si no existe"""
        print(f"📁 Creando directorio: {ASSETS_DIR}")
        os.makedirs(ASSETS_DIR, exist_ok=True)
        
    def fetch_teams_from_games(self, games: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """Extrae equipos únicos desde los datos de partidos"""
        print("🏀 Extrayendo equipos desde datos de partidos...")
        
        teams_dict = {}
        
        for game in games:
            # Procesar equipo local
            home_team = game.get('home', {})
            if home_team.get('code'):
                teams_dict[home_team['code']] = {
                    "id": str(home_team.get('code', '')),
                    "name": home_team.get('name', ''),
                    "shortName": home_team.get('abbreviatedName', home_team.get('name', '')),
                    "logoUrl": home_team.get('imageUrls', {}).get('crest', ''),
                    "primaryColor": "#000000",
                    "secondaryColor": "#FFFFFF"
                }
            
            # Procesar equipo visitante
            away_team = game.get('away', {})
            if away_team.get('code'):
                teams_dict[away_team['code']] = {
                    "id": str(away_team.get('code', '')),
                    "name": away_team.get('name', ''),
                    "shortName": away_team.get('abbreviatedName', away_team.get('name', '')),
                    "logoUrl": away_team.get('imageUrls', {}).get('crest', ''),
                    "primaryColor": "#000000",
                    "secondaryColor": "#FFFFFF"
                }
        
        teams_list = list(teams_dict.values())
        print(f"✅ Equipos extraídos: {len(teams_list)}")
        
        return teams_list
        
    def fetch_teams(self) -> List[Dict[str, Any]]:
        """Obtiene todos los equipos primero intentando clubs, luego extrayendo de partidos"""
        print("🏀 Obteniendo equipos desde API EuroLeague...")
        
        # Primero intentar obtener desde clubs
        url = TEAMS_API_URL
        print(f"   URL: {url}")
        
        try:
            response = self.session.get(url, timeout=30)
            response.raise_for_status()
            
            data = response.json()
            clubs = data.get('data', [])
            
            print(f"✅ Equipos obtenidos desde API clubs: {len(clubs)}")
            
            # Convertir a formato StaticTeam con información completa
            static_teams = []
            for club in clubs:
                static_team = {
                    "id": str(club.get('code', '')),
                    "name": club.get('name', ''),
                    "shortName": club.get('abbreviatedName', club.get('name', '')),
                    "logoUrl": club.get('images', {}).get('crest', ''),
                    "primaryColor": club.get('primaryColor', '#000000'),
                    "secondaryColor": club.get('secondaryColor', '#FFFFFF'),
                    "country": club.get('country', {}).get('name', ''),
                    "city": club.get('city', ''),
                    "venue": club.get('venueCode', ''),
                    "website": club.get('website', ''),
                    "president": club.get('president', ''),
                    "phone": club.get('phone', ''),
                    "address": club.get('address', ''),
                    "twitterAccount": club.get('twitterAccount', ''),
                    "ticketsUrl": club.get('ticketsUrl', '')
                }
                static_teams.append(static_team)
            
            return static_teams
            
        except requests.RequestException as e:
            print(f"⚠️ Error obteniendo equipos desde clubs: {e}")
            print("🔄 Intentando extraer equipos desde datos de partidos...")
            
            # Obtener partidos y extraer equipos de ahí
            try:
                response = self.session.get(GAMES_API_URL, timeout=30)
                response.raise_for_status()
                
                data = response.json()
                games = data.get('data', [])
                
                return self.fetch_teams_from_games(games)
                
            except requests.RequestException as e2:
                print(f"❌ Error obteniendo partidos para extraer equipos: {e2}")
                raise e2
            
    def fetch_all_matches(self) -> List[Dict[str, Any]]:
        """Obtiene todos los partidos de la temporada 2025-26"""
        print(f"⚽ Obteniendo todos los partidos de la temporada 2025-26...")
        
        url = GAMES_API_URL
        print(f"   URL: {url}")
        
        try:
            response = self.session.get(url, timeout=30)
            response.raise_for_status()
            
            data = response.json()
            metadata = data.get('metadata', {})
            total_items = metadata.get('totalItems', 0)
            
            print(f"📊 Total partidos en temporada según API: {total_items}")
            
            # La API parece limitada a 50 partidos por llamada
            # Intentemos obtener partidos por jornadas específicas
            all_games = []
            
            # Intentar obtener partidos de diferentes maneras
            print("� Probando diferentes estrategias de obtención...")
            
            # Estrategia 1: Sin parámetros de paginación
            basic_games = data.get('data', [])
            print(f"   📄 Estrategia básica: {len(basic_games)} partidos")
            
            # Estrategia 2: Por jornadas individuales
            for round_num in range(1, 39):  # 38 jornadas
                try:
                    round_url = f"{url}?round={round_num}"
                    round_response = self.session.get(round_url, timeout=30)
                    if round_response.status_code == 200:
                        round_data = round_response.json()
                        round_games = round_data.get('data', [])
                        if round_games:
                            print(f"   📄 Jornada {round_num}: {len(round_games)} partidos")
                            for game in round_games:
                                if not any(g.get('id') == game.get('id') for g in all_games):
                                    all_games.append(game)
                except:
                    continue
            
            # Si no conseguimos muchos partidos por jornadas, usar los básicos
            if len(all_games) < len(basic_games):
                print("   🔄 Usando datos básicos de la API")
                all_games = basic_games
            
            print(f"✅ Total partidos únicos obtenidos: {len(all_games)}")
            
            # Convertir a formato StaticMatch
            all_matches = []
            
            for game in all_games:
                try:
                    # Procesar fecha y hora
                    game_date = game.get('date', '')
                    
                    # La fecha ya viene en formato ISO
                    if game_date:
                        # Convertir de UTC a formato local
                        from datetime import datetime
                        try:
                            dt = datetime.fromisoformat(game_date.replace('Z', '+00:00'))
                            iso_datetime = dt.isoformat()
                        except:
                            iso_datetime = game_date
                    else:
                        iso_datetime = "2025-10-01T20:00:00"
                    
                    # Procesar broadcasters
                    broadcasters = []
                    game_broadcasters = game.get('broadcasters', [])
                    if isinstance(game_broadcasters, list):
                        for broadcaster in game_broadcasters:
                            if isinstance(broadcaster, dict):
                                broadcasters.append(broadcaster.get('name', ''))
                            else:
                                broadcasters.append(str(broadcaster))
                    
                    # Obtener información del partido
                    home_team = game.get('home', {})
                    away_team = game.get('away', {})
                    venue = game.get('venue', {})
                    round_info = game.get('round', {})
                    
                    static_match = {
                        "id": str(game.get('id', '')),
                        "homeTeamId": str(home_team.get('code', '')),
                        "awayTeamId": str(away_team.get('code', '')),
                        "homeTeamName": home_team.get('name', ''),
                        "awayTeamName": away_team.get('name', ''),
                        "dateTime": iso_datetime,
                        "status": game.get('status', 'scheduled'),
                        "round": round_info.get('round', 1),
                        "arena": venue.get('name', ''),
                        "city": venue.get('address', ''),
                        "country": "",
                        "broadcasters": broadcasters,
                        "homeScore": home_team.get('score', None),
                        "awayScore": away_team.get('score', None)
                    }
                    
                    all_matches.append(static_match)
                    
                except Exception as e:
                    print(f"\n⚠️ Error procesando partido {game.get('id', 'unknown')}: {e}")
                    continue
            
            # Ordenar por fecha
            all_matches.sort(key=lambda x: x['dateTime'])
            
            # Mostrar estadísticas por jornada
            rounds_count = {}
            for match in all_matches:
                round_num = match.get('round', 1)
                rounds_count[round_num] = rounds_count.get(round_num, 0) + 1
            
            print(f"📊 Distribución por jornadas:")
            for round_num in sorted(rounds_count.keys()):
                print(f"   Jornada {round_num}: {rounds_count[round_num]} partidos")
            
            return all_matches
            
        except requests.RequestException as e:
            print(f"❌ Error obteniendo partidos: {e}")
            return []
        
    def save_teams_data(self, teams: List[Dict[str, Any]]):
        """Guarda los datos de equipos en formato JSON"""
        print(f"💾 Guardando equipos en: {TEAMS_FILE}")
        
        teams_data = {
            "version": "1.0",
            "season": "2025-26",
            "generatedAt": datetime.now().isoformat(),
            "source": "EuroLeague API",
            "teams": teams
        }
        
        with open(TEAMS_FILE, 'w', encoding='utf-8') as f:
            json.dump(teams_data, f, ensure_ascii=False, indent=2)
            
        print(f"✅ Archivo de equipos guardado: {len(teams)} equipos")
        
    def save_matches_data(self, matches: List[Dict[str, Any]]):
        """Guarda los datos de partidos en formato JSON"""
        print(f"💾 Guardando partidos en: {MATCHES_FILE}")
        
        matches_data = {
            "version": "1.0",
            "season": "2025-26",
            "generatedAt": datetime.now().isoformat(),
            "source": "EuroLeague API",
            "totalRounds": TOTAL_ROUNDS,
            "matches": matches
        }
        
        with open(MATCHES_FILE, 'w', encoding='utf-8') as f:
            json.dump(matches_data, f, ensure_ascii=False, indent=2)
            
        print(f"✅ Archivo de partidos guardado: {len(matches)} partidos")
        
    def generate_summary(self, teams: List[Dict[str, Any]], matches: List[Dict[str, Any]]):
        """Genera un resumen de los datos generados"""
        print("\n" + "="*60)
        print("📊 RESUMEN DE DATOS ESTÁTICOS GENERADOS")
        print("="*60)
        print(f"🏀 Equipos generados: {len(teams)}")
        print(f"⚽ Partidos generados: {len(matches)}")
        print(f"📅 Jornadas cubiertas: {TOTAL_ROUNDS}")
        print(f"🗓️ Generado el: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        
        # Verificar fechas de partidos
        if matches:
            dates = [match['dateTime'][:10] for match in matches if match.get('dateTime')]
            if dates:
                dates.sort()
                print(f"📅 Rango de fechas: {dates[0]} → {dates[-1]}")
        
        print("\n📁 Archivos generados:")
        print(f"   - {TEAMS_FILE}")
        print(f"   - {MATCHES_FILE}")
        
        print("\n✅ ¡Datos estáticos listos para la aplicación!")
        print("   Los usuarios tendrán todos los datos desde la primera instalación.")
        
    def populate_all_data(self):
        """Ejecuta todo el proceso de población de datos"""
        print("🚀 INICIANDO POBLACIÓN DE DATOS ESTÁTICOS EUROLEAGUE 2025-26")
        print("="*70)
        
        try:
            # Crear directorio
            self.create_assets_directory()
            
            # Obtener equipos
            teams = self.fetch_teams()
            
            # Obtener partidos
            matches = self.fetch_all_matches()
            
            # Guardar datos
            self.save_teams_data(teams)
            self.save_matches_data(matches)
            
            # Mostrar resumen
            self.generate_summary(teams, matches)
            
            return True
            
        except Exception as e:
            print(f"\n❌ ERROR CRÍTICO: {e}")
            print("💡 Verifica tu conexión a internet y que la API esté disponible.")
            return False

def main():
    """Función principal"""
    print("EuroLeague Static Data Populator")
    print("================================")
    print(f"🎯 Objetivo: Poblar datos estáticos para temporada 2025-26")
    print(f"🌐 API: {API_BASE_URL}")
    print(f"📁 Destino: {ASSETS_DIR}")
    print()
    
    # Verificar que estamos en el directorio correcto
    if not os.path.exists(os.path.join(PROJECT_ROOT, "app")):
        print("❌ Error: No se encuentra el directorio 'app'.")
        print("   Ejecuta este script desde la raíz del proyecto.")
        sys.exit(1)
    
    # Ejecutar población
    populator = EuroLeagueDataPopulator()
    success = populator.populate_all_data()
    
    if success:
        print("\n🎉 ¡POBLACIÓN COMPLETADA CON ÉXITO!")
        sys.exit(0)
    else:
        print("\n💥 ¡POBLACIÓN FALLÓ!")
        sys.exit(1)

if __name__ == "__main__":
    main()
