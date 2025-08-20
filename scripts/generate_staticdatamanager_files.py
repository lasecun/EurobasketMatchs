#!/usr/bin/env python3
"""
Script para generar archivos estáticos con la estructura exacta que espera StaticDataManager
"""

import json
import os
from datetime import datetime

def main():
    print("🔄 Generando archivos estáticos para StaticDataManager...")
    
    # Cargar datos del archivo principal
    with open('app/src/main/assets/static_data.json', 'r', encoding='utf-8') as f:
        main_data = json.load(f)
    
    teams = main_data.get('teams', [])
    games = main_data.get('games', [])
    
    print(f"✅ Cargados {len(teams)} equipos y {len(games)} partidos")
    
    # Generar teams_2025_26.json con estructura StaticTeamsData
    print("📝 Generando teams_2025_26.json...")
    
    # Transformar equipos para StaticTeam
    static_teams = []
    for team in teams:
        static_team = {
            "id": team.get("id", ""),
            "name": team.get("name", ""),
            "shortName": team.get("shortName", team.get("name", "")),
            "logoUrl": "",  # Se actualizará después
            "primaryColor": team.get("primaryColor", "#000000"),
            "secondaryColor": team.get("secondaryColor", "#FFFFFF"),
            "country": team.get("country") or "",  # Convertir null a string vacío
            "city": team.get("city") or "",        # Convertir null a string vacío
            "venue": team.get("venue") or "",      # Convertir null a string vacío
            "website": team.get("website") or "",  # Convertir null a string vacío
            "president": team.get("president") or "",  # Convertir null a string vacío
            "phone": team.get("phone") or "",      # Convertir null a string vacío
            "address": team.get("address") or "",  # Convertir null a string vacío
            "twitterAccount": team.get("twitterAccount") or "",  # Convertir null a string vacío
            "ticketsUrl": team.get("ticketsUrl") or "",  # Convertir null a string vacío
            "code": team.get("code", team.get("id", "")),
            "founded": team.get("founded", 0),
            "coach": team.get("coach") or ""       # Convertir null a string vacío
        }
        static_teams.append(static_team)
    
    teams_data = {
        "version": "2025-26-v1.0",
        "lastUpdated": datetime.now().isoformat(),
        "teams": static_teams
    }
    
    # Guardar teams_2025_26.json
    teams_file = "app/src/main/assets/static_data/teams_2025_26.json"
    with open(teams_file, 'w', encoding='utf-8') as f:
        json.dump(teams_data, f, ensure_ascii=False, indent=2)
    print(f"✅ Generado: teams_2025_26.json ({len(static_teams)} equipos)")
    
    # Generar matches_calendar_2025_26.json con estructura StaticMatchesData
    print("📝 Generando matches_calendar_2025_26.json...")
    
    # Transformar partidos para StaticMatch
    static_matches = []
    for game in games:
        static_match = {
            "id": game.get("id", ""),
            "round": game.get("round", 1),
            "homeTeamCode": game.get("homeTeamId", ""),
            "awayTeamCode": game.get("awayTeamId", ""),
            "venue": game.get("venue", ""),
            "season": "E2025",
            "status": game.get("status", "confirmed"),
            "dateTime": game.get("date", "").replace(" ", "T"),  # Convertir formato
            "homeScore": game.get("homeScore", 0),
            "awayScore": game.get("awayScore", 0)
        }
        static_matches.append(static_match)
    
    matches_data = {
        "version": "2025-26-v1.0",
        "lastUpdated": datetime.now().isoformat(),
        "season": "2025-26",
        "totalRounds": 38,
        "description": "Calendario completo EuroLeague 2025-26",
        "note": "380 partidos de temporada regular generados desde API oficial",
        "matches": static_matches
    }
    
    # Guardar matches_calendar_2025_26.json
    matches_file = "app/src/main/assets/static_data/matches_calendar_2025_26.json"
    with open(matches_file, 'w', encoding='utf-8') as f:
        json.dump(matches_data, f, ensure_ascii=False, indent=2)
    print(f"✅ Generado: matches_calendar_2025_26.json ({len(static_matches)} partidos)")
    
    # Generar data_version.json con estructura DataVersionInfo
    print("📝 Generando data_version.json...")
    
    version_data = {
        "version": "2025-26-v1.0",
        "lastUpdated": datetime.now().isoformat(),
        "description": "Datos estáticos precargados para EuroLeague 2025-26",
        "staticDataVersions": {
            "teams": "2025-26-v1.0",
            "matches": "2025-26-v1.0"
        },
        "lastStaticDataUpdate": datetime.now().isoformat(),
        "dynamicDataVersions": {
            "teams": "",
            "matches": ""
        },
        "syncConfig": {
            "manualSyncOnly": True,
            "autoSyncInterval": 0,
            "lastAutoSync": "",
            "syncOnStartup": False
        }
    }
    
    # Guardar data_version.json
    version_file = "app/src/main/assets/static_data/data_version.json"
    with open(version_file, 'w', encoding='utf-8') as f:
        json.dump(version_data, f, ensure_ascii=False, indent=2)
    print(f"✅ Generado: data_version.json")
    
    print("\n" + "=" * 60)
    print("✅ ARCHIVOS PARA STATICDATAMANAGER GENERADOS")
    print(f"📁 Directorio: app/src/main/assets/static_data/")
    print(f"📋 Archivos generados:")
    print(f"   • teams_2025_26.json ({len(static_teams)} equipos)")
    print(f"   • matches_calendar_2025_26.json ({len(static_matches)} partidos)")
    print(f"   • data_version.json")
    print(f"\n🎉 ¡Archivos compatibles con StaticDataManager generados exitosamente!")

if __name__ == "__main__":
    main()
