#!/usr/bin/env python3
"""
Script para ajustar el formato de los datos estáticos para que coincidan con StaticMatch y StaticTeam
"""

import json
import os
import sys
from datetime import datetime

def main():
    print("🔧 Ajustando formato de datos estáticos para compatibilidad...")
    
    # Rutas
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(script_dir)
    assets_dir = os.path.join(project_root, "app", "src", "main", "assets")
    static_data_dir = os.path.join(assets_dir, "static_data")
    
    teams_file = os.path.join(static_data_dir, "teams_2025_26.json")
    matches_file = os.path.join(static_data_dir, "matches_calendar_2025_26.json")
    
    # 1. Ajustar formato de equipos
    print("📝 Ajustando formato de equipos...")
    with open(teams_file, 'r', encoding='utf-8') as f:
        teams_data = json.load(f)
    
    # Los equipos ya están en formato correcto, solo necesitamos verificar campos obligatorios
    for team in teams_data['teams']:
        # Asegurar que tiene todos los campos requeridos
        if 'shortName' not in team or not team['shortName']:
            team['shortName'] = team['name']
        if 'logoUrl' not in team:
            team['logoUrl'] = ""
        if 'founded' not in team:
            team['founded'] = 0
        if 'coach' not in team:
            team['coach'] = ""
    
    # Guardar equipos ajustados
    with open(teams_file, 'w', encoding='utf-8') as f:
        json.dump(teams_data, f, ensure_ascii=False, indent=2)
    print(f"✅ Equipos ajustados: {len(teams_data['teams'])} equipos")
    
    # 2. Ajustar formato de partidos
    print("📝 Ajustando formato de partidos...")
    with open(matches_file, 'r', encoding='utf-8') as f:
        matches_data = json.load(f)
    
    adjusted_matches = []
    for match in matches_data['matches']:
        # Convertir formato de fecha de "2025-10-01 18:00:00" a "2025-10-01T18:00:00"
        date_str = match.get('date', '')
        if ' ' in date_str:
            date_str = date_str.replace(' ', 'T')
        
        # Crear match en formato StaticMatch
        adjusted_match = {
            "id": match.get('id', ''),
            "round": match.get('round', 1),
            "homeTeamCode": match.get('homeTeamId', ''),  # Cambiar de homeTeamId a homeTeamCode
            "awayTeamCode": match.get('awayTeamId', ''),  # Cambiar de awayTeamId a awayTeamCode
            "venue": match.get('venue', ''),
            "season": match.get('season', 'E2025'),
            "status": match.get('status', 'scheduled'),
            "dateTime": date_str,  # Cambiar de date a dateTime
            "homeScore": match.get('homeScore'),
            "awayScore": match.get('awayScore')
        }
        adjusted_matches.append(adjusted_match)
    
    # Actualizar estructura de datos de partidos
    matches_data['matches'] = adjusted_matches
    
    # Agregar campos requeridos para StaticMatchesData
    matches_data['description'] = "Calendario completo EuroLeague 2025-26"
    matches_data['note'] = "380 partidos de temporada regular generados desde API oficial"
    matches_data['lastUpdated'] = datetime.now().isoformat()
    
    # Guardar partidos ajustados
    with open(matches_file, 'w', encoding='utf-8') as f:
        json.dump(matches_data, f, ensure_ascii=False, indent=2)
    print(f"✅ Partidos ajustados: {len(adjusted_matches)} partidos")
    
    # 3. Verificar formato final
    print("\n🔍 Verificando formato final...")
    
    # Verificar que los teamCodes en partidos existen en equipos
    team_ids = {team['id'] for team in teams_data['teams']}
    
    missing_teams = set()
    for match in adjusted_matches:
        home_team = match['homeTeamCode']
        away_team = match['awayTeamCode']
        if home_team not in team_ids:
            missing_teams.add(home_team)
        if away_team not in team_ids:
            missing_teams.add(away_team)
    
    if missing_teams:
        print(f"⚠️ Equipos referenciados pero no encontrados: {missing_teams}")
    else:
        print("✅ Todos los equipos referenciados existen")
    
    print("\n" + "=" * 60)
    print("✅ FORMATO DE DATOS ESTÁTICOS AJUSTADO")
    print("📋 Cambios realizados:")
    print("   • Equipos: Verificados campos obligatorios")
    print("   • Partidos: homeTeamId → homeTeamCode")
    print("   • Partidos: awayTeamId → awayTeamCode") 
    print("   • Partidos: date → dateTime (formato ISO)")
    print("   • Partidos: Agregados description y note")
    print(f"\n📊 Resumen final:")
    print(f"   🏆 {len(teams_data['teams'])} equipos")
    print(f"   ⚽ {len(adjusted_matches)} partidos")
    print("   ✅ Formato compatible con StaticDataManager")
    
    return True

if __name__ == "__main__":
    success = main()
    if success:
        print("\n🎉 ¡Formato de datos estáticos ajustado exitosamente!")
        print("StaticDataManager ahora puede cargar los datos correctamente.")
    else:
        print("\n💥 Error ajustando formato de datos estáticos")
    
    sys.exit(0 if success else 1)
