#!/usr/bin/env python3
"""
Script para generar archivos estáticos separados para StaticDataManager
Convierte el archivo static_data.json en los archivos específicos que espera la app
"""

import json
import os
import sys
from datetime import datetime

def main():
    print("🔄 Generando archivos estáticos separados para StaticDataManager...")
    
    # Rutas
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(script_dir)
    assets_dir = os.path.join(project_root, "app", "src", "main", "assets")
    static_data_file = os.path.join(assets_dir, "static_data.json")
    static_data_dir = os.path.join(assets_dir, "static_data")
    
    # Verificar que existe el archivo fuente
    if not os.path.exists(static_data_file):
        print(f"❌ Error: No existe {static_data_file}")
        print("   Ejecuta primero: python scripts/populate_game_center_data.py")
        return False
    
    # Crear directorio static_data
    os.makedirs(static_data_dir, exist_ok=True)
    
    # Cargar datos del archivo unificado
    print("📥 Cargando datos desde static_data.json...")
    with open(static_data_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    teams = data.get('teams', [])
    games = data.get('games', [])
    
    print(f"✅ Cargados {len(teams)} equipos y {len(games)} partidos")
    
    # Analizar jornadas
    rounds = {}
    for game in games:
        round_num = game.get('round', 0)
        if round_num:
            rounds[round_num] = rounds.get(round_num, 0) + 1
    
    # 1. Generar teams_2025_26.json
    print("📝 Generando teams_2025_26.json...")
    teams_data = {
        "teams": teams,
        "version": "1.0.0",
        "generatedAt": datetime.now().isoformat(),
        "season": "2025-26",
        "source": "EuroLeague Feeds API"
    }
    
    teams_file = os.path.join(static_data_dir, "teams_2025_26.json")
    with open(teams_file, 'w', encoding='utf-8') as f:
        json.dump(teams_data, f, ensure_ascii=False, indent=2)
    print(f"✅ Generado: teams_2025_26.json ({len(teams)} equipos)")
    
    # 2. Generar matches_calendar_2025_26.json
    print("📝 Generando matches_calendar_2025_26.json...")
    matches_data = {
        "matches": games,
        "version": "1.0.0", 
        "generatedAt": datetime.now().isoformat(),
        "season": "2025-26",
        "source": "EuroLeague Feeds API",
        "totalRounds": 38,
        "roundsWithData": sorted(rounds.keys()) if rounds else []
    }
    
    matches_file = os.path.join(static_data_dir, "matches_calendar_2025_26.json")
    with open(matches_file, 'w', encoding='utf-8') as f:
        json.dump(matches_data, f, ensure_ascii=False, indent=2)
    print(f"✅ Generado: matches_calendar_2025_26.json ({len(games)} partidos)")
    
    # 3. Generar data_version.json
    print("📝 Generando data_version.json...")
    version_data = {
        "version": "1.0.0",
        "generatedAt": datetime.now().isoformat(),
        "lastUpdated": datetime.now().isoformat(),
        "season": "2025-26",
        "source": "EuroLeague Feeds API",
        "totalTeams": len(teams),
        "totalMatches": len(games),
        "totalRounds": 38,
        "roundsWithData": sorted(rounds.keys()) if rounds else []
    }
    
    version_file = os.path.join(static_data_dir, "data_version.json")
    with open(version_file, 'w', encoding='utf-8') as f:
        json.dump(version_data, f, ensure_ascii=False, indent=2)
    print(f"✅ Generado: data_version.json")
    
    print("\n" + "=" * 60)
    print("✅ ARCHIVOS ESTÁTICOS SEPARADOS GENERADOS")
    print(f"📁 Directorio: {static_data_dir}")
    print("📋 Archivos generados:")
    print("   • teams_2025_26.json")
    print("   • matches_calendar_2025_26.json") 
    print("   • data_version.json")
    print(f"\n📊 Resumen:")
    print(f"   🏆 {len(teams)} equipos")
    print(f"   ⚽ {len(games)} partidos")
    print(f"   📅 {len(rounds)} jornadas ({min(rounds.keys()) if rounds else 0}-{max(rounds.keys()) if rounds else 0})")
    
    return True

if __name__ == "__main__":
    success = main()
    if success:
        print("\n🎉 ¡Archivos estáticos separados generados exitosamente!")
        print("StaticDataManager ahora puede cargar los datos precargados.")
    else:
        print("\n💥 Error generando archivos estáticos separados")
    
    sys.exit(0 if success else 1)
