#!/usr/bin/env python3
"""
Script para poblar datos estáticos de EuroLeague desde feeds.incrowdsports.com
y la página del Game Center.
"""

import requests
import json
import sys
import os
from typing import Dict, List, Any
from datetime import datetime
import time

# Rutas de archivos
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
ASSETS_DIR = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets")
OUTPUT_FILE = os.path.join(ASSETS_DIR, "static_data.json")

def fetch_json_data(url: str) -> Dict[str, Any]:
    """Obtiene datos JSON de una URL."""
    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        return response.json()
    except requests.RequestException as e:
        print(f"Error al obtener datos de {url}: {e}")
        return {}

def fetch_game_center_data(round_num: int = 1) -> Dict[str, Any]:
    """Obtiene datos del Game Center de EuroLeague."""
    try:
        url = f"https://www.euroleaguebasketball.net/es/euroleague/game-center/?round={round_num}&season=E2025"
        response = requests.get(url, timeout=15)
        response.raise_for_status()
        
        # Extraer JSON del script __NEXT_DATA__
        content = response.text
        start_marker = '<script id="__NEXT_DATA__" type="application/json">'
        end_marker = '</script>'
        
        start_idx = content.find(start_marker)
        if start_idx == -1:
            print("No se encontró el script __NEXT_DATA__")
            return {}
        
        start_idx += len(start_marker)
        end_idx = content.find(end_marker, start_idx)
        if end_idx == -1:
            print("No se encontró el final del script __NEXT_DATA__")
            return {}
        
        json_str = content[start_idx:end_idx]
        return json.loads(json_str)
    except Exception as e:
        print(f"Error al obtener datos del Game Center: {e}")
        return {}

def extract_teams_from_clubs_api() -> List[Dict[str, Any]]:
    """Extrae información completa de equipos desde el API de clubs."""
    clubs_url = "https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/clubs"
    clubs_data = fetch_json_data(clubs_url)
    
    if not clubs_data or 'data' not in clubs_data:
        print("No se pudieron obtener datos de clubs")
        return []
    
    teams = []
    for club in clubs_data['data']:
        # Extraer información completa del equipo
        team = {
            "id": club.get('code', ''),
            "name": club.get('name', ''),
            "shortName": club.get('tvName', club.get('name', '')),
            "code": club.get('code', ''),
            "imageUrl": club.get('imageUrls', ''),
            "primaryColor": club.get('primaryColorHex', '#000000'),
            "secondaryColor": club.get('secondaryColorHex', '#FFFFFF'),
            "country": club.get('country', {}).get('name', ''),
            "city": club.get('city', ''),
            "venue": club.get('venue', {}).get('name', ''),
            "website": club.get('website', ''),
            "president": club.get('president', ''),
            "phone": club.get('phone', ''),
            "address": club.get('address', ''),
            "twitterAccount": club.get('twitterAccount', ''),
            "ticketsUrl": club.get('ticketsUrl', '')
        }
        teams.append(team)
    
    return teams

def extract_all_games_from_feeds_api() -> List[Dict[str, Any]]:
    """Extrae TODOS los partidos de las 38 jornadas usando la API correcta."""
    print("🏀 Extrayendo calendario COMPLETO desde Feeds API...")
    print("📅 Descargando las 38 jornadas de la temporada EuroLeague 2025-26")
    
    all_games = []
    successful_rounds = 0
    failed_rounds = 0
    
    # Extraer todas las jornadas del 1 al 38 usando roundNumber
    for round_num in range(1, 39):  # Jornadas 1 a 38
        print(f"📥 Descargando jornada {round_num:2d}/38...", end=" ")
        
        # URL correcta con roundNumber
        url = f"https://feeds.incrowdsports.com/provider/euroleague-feeds/v2/competitions/E/seasons/E2025/games?teamCode=&phaseTypeCode=RS&roundNumber={round_num}"
        games_data = fetch_json_data(url)
        
        if not games_data or 'data' not in games_data:
            print("❌ Error de conexión")
            failed_rounds += 1
            continue
        
        games = games_data['data']
        if not games:
            print("⏳ Sin datos")
            failed_rounds += 1
            continue
        
        round_games = 0
        for game in games:
            # Convertir fecha
            date_str = game.get('date', '')
            formatted_date = ""
            if date_str:
                try:
                    # Fecha viene en formato "2025-10-03T18:00:00.000Z"
                    dt = datetime.fromisoformat(date_str.replace('Z', '+00:00'))
                    formatted_date = dt.strftime('%Y-%m-%d %H:%M:%S')
                except:
                    formatted_date = date_str
            
            # Extraer información completa del partido
            game_obj = {
                "id": game.get('id', ''),
                "homeTeamId": game.get('home', {}).get('code', ''),
                "awayTeamId": game.get('away', {}).get('code', ''),
                "homeTeamName": game.get('home', {}).get('name', ''),
                "awayTeamName": game.get('away', {}).get('name', ''),
                "date": formatted_date,
                "round": game.get('round', {}).get('round', round_num),
                "homeScore": game.get('home', {}).get('score', 0),
                "awayScore": game.get('away', {}).get('score', 0),
                "status": game.get('status', 'scheduled'),
                "venue": game.get('venue', {}).get('name', ''),
                "venueCapacity": game.get('venue', {}).get('capacity', 0),
                "venueCode": game.get('venue', {}).get('code', ''),
                "gameCode": game.get('code', 0),
                "phaseType": game.get('phaseType', {}).get('code', 'RS'),
                "season": game.get('season', {}).get('code', 'E2025')
            }
            all_games.append(game_obj)
            round_games += 1
        
        print(f"✅ {round_games} partidos")
        successful_rounds += 1
        
        # Pausa pequeña para no sobrecargar el servidor
        time.sleep(0.2)
    
    print(f"\n📊 Resumen de extracción Feeds API:")
    print(f"   ✅ Jornadas exitosas: {successful_rounds}/38")
    print(f"   ❌ Jornadas fallidas: {failed_rounds}/38")
    print(f"   🎯 Total partidos: {len(all_games)}")
    
    return all_games

def extract_games_from_game_center() -> List[Dict[str, Any]]:
    """Extrae partidos desde el Game Center - solo jornadas con datos reales."""
    print("🏀 Extrayendo calendario desde Game Center...")
    print("⚠️  Nota: Solo extrayendo jornadas con datos reales programados")
    
    all_games = []
    unique_games = set()  # Para evitar duplicados
    
    # Probar las primeras jornadas para encontrar datos únicos
    for round_num in range(1, 6):  # Probar jornadas 1-5
        print(f"📥 Verificando jornada {round_num}...", end=" ")
        
        round_data = fetch_game_center_data(round_num)
        if not round_data:
            print("❌ Error de conexión")
            continue
        
        try:
            page_props = round_data['props']['pageProps']
            actual_round = page_props.get('currentRound', round_num)
            
            game_groups = page_props.get('currentRoundGameGroups', [])
            if not game_groups:
                print("⏳ Sin datos")
                continue
            
            round_games = 0
            
            for group in game_groups:
                for game in group.get('games', []):
                    game_id = game.get('id', '')
                    
                    # Evitar duplicados
                    if game_id in unique_games:
                        continue
                    unique_games.add(game_id)
                    
                    # Convertir fecha
                    date_str = game.get('date', '')
                    formatted_date = ""
                    if date_str:
                        try:
                            dt = datetime.fromisoformat(date_str.replace('Z', '+00:00'))
                            formatted_date = dt.strftime('%Y-%m-%d %H:%M:%S')
                        except:
                            formatted_date = date_str
                    
                    # Usar la jornada real del partido
                    actual_game_round = game.get('round', {}).get('round', actual_round)
                    
                    game_obj = {
                        "id": game_id,
                        "homeTeamId": game.get('home', {}).get('code', ''),
                        "awayTeamId": game.get('away', {}).get('code', ''),
                        "homeTeamName": game.get('home', {}).get('name', ''),
                        "awayTeamName": game.get('away', {}).get('name', ''),
                        "date": formatted_date,
                        "round": actual_game_round,
                        "homeScore": game.get('home', {}).get('score', 0),
                        "awayScore": game.get('away', {}).get('score', 0),
                        "status": game.get('status', 'scheduled'),
                        "venue": game.get('venue', {}).get('name', ''),
                        "venueCapacity": game.get('venue', {}).get('capacity', 0),
                        "venueAddress": game.get('venue', {}).get('address', ''),
                        "gameUrl": game.get('url', ''),
                        "gameCode": game.get('code', 0)
                    }
                    all_games.append(game_obj)
                    round_games += 1
            
            if round_games > 0:
                print(f"✅ {round_games} partidos únicos")
            else:
                print("⏳ Sin partidos únicos")
                
        except Exception as e:
            print(f"❌ Error: {str(e)[:50]}...")
            continue
        
        # Pausa pequeña
        time.sleep(0.2)
    
    print(f"\n📊 Resumen de extracción Game Center:")
    print(f"   🎯 Total partidos únicos: {len(all_games)}")
    
    # Analizar jornadas reales
    rounds = {}
    for game in all_games:
        round_num = game.get('round', 0)
        rounds[round_num] = rounds.get(round_num, 0) + 1
    
    if rounds:
        print(f"   📅 Jornadas con datos: {sorted(rounds.keys())}")
        for round_num in sorted(rounds.keys()):
            print(f"      Jornada {round_num}: {rounds[round_num]} partidos")
    
    return all_games

def create_static_data():
    """Crea los archivos de datos estáticos."""
    print("🏀 Poblando datos estáticos de EuroLeague 2025-26")
    print("📡 Fuente: API Feeds oficial con calendario completo")
    print("🎯 Objetivo: Los 380 partidos de la temporada")
    print("=" * 60)
    
    # Crear directorio assets si no existe
    os.makedirs(ASSETS_DIR, exist_ok=True)
    
    print("1️⃣ Obteniendo información de equipos...")
    teams = extract_teams_from_clubs_api()
    
    if not teams:
        print("❌ Error: No se pudieron obtener datos de equipos")
        return False
    
    print(f"✅ Obtenidos {len(teams)} equipos con información rica")
    
    print("\n2️⃣ Obteniendo calendario COMPLETO de partidos...")
    all_games = extract_all_games_from_feeds_api()
    
    if not all_games:
        print("❌ Error: No se pudieron obtener partidos")
        return False
    
    print(f"\n📊 Datos recopilados:")
    print(f"   🏆 Equipos: {len(teams)}")
    print(f"   ⚽ Partidos: {len(all_games)}")
    
    # Analizar jornadas
    rounds = {}
    for game in all_games:
        round_num = game.get('round', 0)
        rounds[round_num] = rounds.get(round_num, 0) + 1
    
    print(f"   � Jornadas: {len(rounds)} ({min(rounds.keys()) if rounds else 0}-{max(rounds.keys()) if rounds else 0})")
    
    # Crear estructura de datos estáticos
    static_data = {
        "teams": teams,
        "games": all_games,
        "lastUpdated": datetime.now().isoformat(),
        "season": "2025-26",
        "source": "EuroLeague Game Center + Feeds API",
        "totalRounds": 38,
        "totalTeams": len(teams),
        "totalGames": len(all_games),
        "roundsWithData": sorted(rounds.keys()) if rounds else []
    }
    
    # Guardar en archivo JSON
    try:
        with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
            json.dump(static_data, f, ensure_ascii=False, indent=2)
        
        print("\n" + "=" * 60)
        print("✅ DATOS ESTÁTICOS GENERADOS EXITOSAMENTE")
        print(f"📁 Archivo: {OUTPUT_FILE}")
        print(f"📊 Contenido:")
        print(f"   🏆 {len(teams)} equipos (colores, países, venues, contactos)")
        print(f"   ⚽ {len(all_games)} partidos")
        
        if rounds:
            print(f"   📅 Jornadas disponibles:")
            for round_num in sorted(rounds.keys()):
                print(f"      Jornada {round_num}: {rounds[round_num]} partidos")
        
        # Calcular cobertura estimada
        available_rounds = len(rounds)
        coverage_rounds = (available_rounds / 38) * 100
        coverage_games = (len(all_games) / 380) * 100
        print(f"   📈 Cobertura: {available_rounds}/38 jornadas ({coverage_rounds:.1f}%)")
        print(f"   📈 Partidos: {len(all_games)}/380 estimados ({coverage_games:.1f}%)")
        
        # Mostrar algunos equipos de ejemplo
        print(f"\n🏆 Ejemplos de equipos:")
        for team in teams[:3]:
            print(f"   {team['name']} ({team['country']}) - {team['city']}")
        
        # Mostrar algunos partidos de ejemplo
        if all_games:
            print(f"\n⚽ Ejemplos de partidos:")
            for game in all_games[:3]:
                home = game.get('homeTeamName', game.get('homeTeamId', ''))
                away = game.get('awayTeamName', game.get('awayTeamId', ''))
                date = game.get('date', '')[:10] if game.get('date') else 'TBD'
                print(f"   {home} vs {away} - {date} (Jornada {game.get('round', '?')})")
        
        return True
        
    except Exception as e:
        print(f"❌ Error guardando archivo: {e}")
        return False

if __name__ == "__main__":
    success = create_static_data()
    if success:
        print("\n🎉 ¡Datos estáticos poblados exitosamente!")
        print("La aplicación ahora tendrá todos los datos precargados en la instalación.")
    else:
        print("\n💥 Error al poblar los datos estáticos")
    
    sys.exit(0 if success else 1)
