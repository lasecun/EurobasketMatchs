#!/usr/bin/env python3
"""
Script para agregar URLs de logos oficiales de EuroLeague a los equipos
"""

import json
import os
import sys

def main():
    print("🖼️ Agregando URLs de logos oficiales de EuroLeague...")
    
    # Mapeo de códigos de equipo a URLs oficiales de logos de EuroLeague
    team_logos = {
        "IST": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/anadolu-efes-istanbul.png",
        "MCO": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/as-monaco.png", 
        "BKN": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/baskonia-vitoria-gasteiz.png",
        "BAR": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/fc-barcelona.png",
        "MUN": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/fc-bayern-munich.png",
        "ULK": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/fenerbahce-beko-istanbul.png",
        "TEL": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/maccabi-playtika-tel-aviv.png",
        "OLY": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/olympiacos-piraeus.png",
        "PAN": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/panathinaikos-aktor-athens.png",
        "PRS": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/paris-basketball.png",
        "MAD": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/real-madrid.png",
        "ZAL": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/zalgiris-kaunas.png",
        "PAM": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/valencia-basket.png",
        "VIR": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/virtus-segafredo-bologna.png",
        "ASV": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/ldlc-asvel-villeurbanne.png",
        "RED": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/crvena-zvezda-meridianbet-belgrade.png",
        "MIL": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/ea7-emporio-armani-milan.png",
        "ALB": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/alba-berlin.png",
        "PAR": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/partizan-mozzart-bet-belgrade.png",
        "DUB": "https://img.euroleaguebasketball.net/design/ec/logos/clubs/dubai-basketball.png"
    }
    
    # Rutas
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(script_dir)
    assets_dir = os.path.join(project_root, "app", "src", "main", "assets")
    static_data_dir = os.path.join(assets_dir, "static_data")
    teams_file = os.path.join(static_data_dir, "teams_2025_26.json")
    
    # Cargar datos actuales
    print("📥 Cargando datos de equipos...")
    with open(teams_file, 'r', encoding='utf-8') as f:
        teams_data = json.load(f)
    
    # Actualizar logos
    updated_count = 0
    for team in teams_data['teams']:
        team_code = team.get('code', team.get('id', ''))
        
        if team_code in team_logos:
            old_logo = team.get('logoUrl', '')
            team['logoUrl'] = team_logos[team_code]
            if old_logo != team['logoUrl']:
                updated_count += 1
                print(f"✅ {team['name']}: {team_code} → Logo actualizado")
        else:
            print(f"⚠️ {team['name']}: Código {team_code} no encontrado en mapeo de logos")
    
    # Guardar datos actualizados
    with open(teams_file, 'w', encoding='utf-8') as f:
        json.dump(teams_data, f, ensure_ascii=False, indent=2)
    
    print(f"\n📊 Resumen:")
    print(f"   🏆 {len(teams_data['teams'])} equipos procesados")
    print(f"   🖼️ {updated_count} logos actualizados")
    print(f"   ⚠️ {len(teams_data['teams']) - updated_count} equipos sin logo específico")
    
    # Mostrar algunos ejemplos
    print(f"\n🖼️ Ejemplos de logos agregados:")
    for team in teams_data['teams'][:3]:
        if team.get('logoUrl'):
            print(f"   {team['name']}: {team['logoUrl']}")
    
    print("\n" + "=" * 60)
    print("✅ LOGOS DE EQUIPOS ACTUALIZADOS")
    print("📋 Se agregaron URLs oficiales de EuroLeague")
    print("🖼️ Los placeholders ahora mostrarán logos reales")
    
    return True

if __name__ == "__main__":
    success = main()
    if success:
        print("\n🎉 ¡URLs de logos agregadas exitosamente!")
        print("Ahora los equipos tendrán sus logos oficiales.")
    else:
        print("\n💥 Error agregando URLs de logos")
    
    sys.exit(0 if success else 1)
