#!/usr/bin/env python3
"""
Script para descargar logos de equipos y guardarlos como assets locales
"""

import json
import os
import sys
import requests
from urllib.parse import urlparse

def download_image(url, filepath):
    """Descarga una imagen desde una URL y la guarda en el filepath especificado"""
    try:
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        }
        response = requests.get(url, headers=headers, timeout=10)
        response.raise_for_status()
        
        # Crear directorio si no existe
        os.makedirs(os.path.dirname(filepath), exist_ok=True)
        
        with open(filepath, 'wb') as f:
            f.write(response.content)
        
        return True
    except Exception as e:
        print(f"❌ Error descargando {url}: {e}")
        return False

def main():
    print("🖼️ Descargando logos de equipos como assets locales...")
    
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
    logos_dir = os.path.join(assets_dir, "team_logos")
    teams_file = os.path.join(static_data_dir, "teams_2025_26.json")
    
    # Crear directorio para logos
    os.makedirs(logos_dir, exist_ok=True)
    print(f"📁 Directorio de logos: {logos_dir}")
    
    # Descargar logos
    print("📥 Descargando logos...")
    downloaded_logos = {}
    
    for team_code, url in team_logos.items():
        print(f"📥 Descargando logo para {team_code}...")
        
        # Generar nombre de archivo local
        filename = f"{team_code.lower()}_logo.png"
        filepath = os.path.join(logos_dir, filename)
        
        if download_image(url, filepath):
            # Ruta para usar en Android (asset://)
            android_path = f"file:///android_asset/team_logos/{filename}"
            downloaded_logos[team_code] = android_path
            print(f"✅ {team_code}: {filename}")
        else:
            print(f"❌ {team_code}: Error descargando")
    
    # Cargar datos de equipos
    print("\n📝 Actualizando datos de equipos...")
    with open(teams_file, 'r', encoding='utf-8') as f:
        teams_data = json.load(f)
    
    # Actualizar URLs de logos a rutas locales
    updated_count = 0
    for team in teams_data['teams']:
        team_code = team.get('code', team.get('id', ''))
        
        if team_code in downloaded_logos:
            old_logo = team.get('logoUrl', '')
            team['logoUrl'] = downloaded_logos[team_code]
            if old_logo != team['logoUrl']:
                updated_count += 1
                print(f"✅ {team['name']}: Logo local asignado")
    
    # Guardar datos actualizados
    with open(teams_file, 'w', encoding='utf-8') as f:
        json.dump(teams_data, f, ensure_ascii=False, indent=2)
    
    print("\n" + "=" * 60)
    print("✅ LOGOS DESCARGADOS Y CONFIGURADOS")
    print(f"📊 Resumen:")
    print(f"   🖼️ {len(downloaded_logos)} logos descargados como assets")
    print(f"   📝 {updated_count} equipos actualizados")
    print(f"   📁 Guardados en: app/src/main/assets/team_logos/")
    print(f"   🔗 URLs actualizadas a rutas locales (file:///android_asset/...)")
    
    print(f"\n📂 Archivos creados:")
    for team_code in downloaded_logos:
        filename = f"{team_code.lower()}_logo.png"
        print(f"   • {filename}")
    
    print(f"\n🎯 Beneficios:")
    print(f"   ⚡ Carga instantánea sin red")
    print(f"   📱 Funciona offline")
    print(f"   🔋 Menor uso de datos")
    print(f"   ✅ Mayor confiabilidad")
    
    return True

if __name__ == "__main__":
    success = main()
    if success:
        print("\n🎉 ¡Logos descargados y configurados como assets!")
        print("Los equipos ahora tendrán logos locales instantáneos.")
    else:
        print("\n💥 Error descargando logos")
    
    sys.exit(0 if success else 1)
