#!/usr/bin/env python3
"""
Script para descargar logos oficiales desde el sitio web de EuroLeague
"""

import json
import os
import sys
import requests
from urllib.parse import urlparse
import time

def download_image(url, filepath):
    """Descarga una imagen desde una URL y la guarda en el filepath especificado"""
    try:
        headers = {
            'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
            'Accept': 'image/webp,image/apng,image/*,*/*;q=0.8',
            'Accept-Language': 'es-ES,es;q=0.9,en;q=0.8',
            'Accept-Encoding': 'gzip, deflate, br',
            'Connection': 'keep-alive',
            'Upgrade-Insecure-Requests': '1'
        }
        
        print(f"📥 Descargando: {url}")
        response = requests.get(url, headers=headers, timeout=15)
        response.raise_for_status()
        
        # Crear directorio si no existe
        os.makedirs(os.path.dirname(filepath), exist_ok=True)
        
        with open(filepath, 'wb') as f:
            f.write(response.content)
        
        file_size = len(response.content)
        print(f"✅ Guardado: {filepath} ({file_size} bytes)")
        return True
        
    except Exception as e:
        print(f"❌ Error descargando {url}: {e}")
        return False

def fetch_team_page_for_logo(team_code, team_name):
    """Obtiene el logo de un equipo desde su página oficial"""
    try:
        # URL de la página del equipo
        team_url = f"https://www.euroleaguebasketball.net/es/euroleague/teams/{team_name.lower().replace(' ', '-')}/roster/{team_code.lower()}/?season=2025-26"
        
        headers = {
            'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        }
        
        print(f"🔍 Buscando logo en: {team_url}")
        response = requests.get(team_url, headers=headers, timeout=10)
        response.raise_for_status()
        
        # Buscar el logo en el contenido HTML
        content = response.text
        
        # Buscar patrones de logos del CDN de EuroLeague
        import re
        patterns = [
            r'https://media-cdn\.incrowdsports\.com/[^"]+\.png[^"]*',
            r'https://media-cdn\.cortextech\.io/[^"]+\.png[^"]*',
            r'https://img\.euroleaguebasketball\.net/[^"]+\.png[^"]*'
        ]
        
        for pattern in patterns:
            matches = re.findall(pattern, content)
            for match in matches:
                # Filtrar solo los que parezcan logos (no hero images)
                if 'logo' in match.lower() or 'width=90' in match or 'height=90' in match:
                    return match.split('?')[0] + '?width=90&height=90&resizeType=fill&format=png'
        
        return None
        
    except Exception as e:
        print(f"❌ Error obteniendo logo para {team_name}: {e}")
        return None

def main():
    print("🏀 Descargando logos oficiales desde EuroLeague...")
    
    # Mapeo de equipos con sus nombres oficiales exactos para las URLs
    teams_data = {
        "IST": "anadolu-efes-istanbul",
        "MCO": "as-monaco", 
        "BKN": "baskonia-vitoria-gasteiz",
        "RED": "crvena-zvezda-meridianbet-belgrade",
        "DUB": "dubai-basketball",
        "MIL": "ea7-emporio-armani-milan",
        "BAR": "fc-barcelona",
        "MUN": "fc-bayern-munich",
        "ULK": "fenerbahce-beko-istanbul",
        "HTA": "hapoel-ibi-tel-aviv",
        "ASV": "ldlc-asvel-villeurbanne",
        "TEL": "maccabi-rapyd-tel-aviv",
        "OLY": "olympiacos-piraeus",
        "PAN": "panathinaikos-aktor-athens",
        "PRS": "paris-basketball",
        "PAR": "partizan-mozzart-bet-belgrade",
        "MAD": "real-madrid",
        "PAM": "valencia-basket",
        "VIR": "virtus-bologna",
        "ZAL": "zalgiris-kaunas"
    }
    
    # URLs conocidas de algunos logos
    known_logos = {
        "MAD": "https://media-cdn.incrowdsports.com/1a3e1404-4f6f-4ede-9d8b-30eee7cb51b4.png?width=90&height=90&resizeType=fill&format=png"
    }
    
    # Directorio de destino
    assets_dir = "app/src/main/assets/team_logos"
    os.makedirs(assets_dir, exist_ok=True)
    
    downloaded_count = 0
    failed_count = 0
    
    for team_code, team_slug in teams_data.items():
        try:
            # Si tenemos URL conocida, usarla
            if team_code in known_logos:
                logo_url = known_logos[team_code]
            else:
                # Buscar el logo en la página del equipo
                logo_url = fetch_team_page_for_logo(team_code, team_slug)
            
            if logo_url:
                filename = f"{team_code.lower()}_logo.png"
                filepath = os.path.join(assets_dir, filename)
                
                if download_image(logo_url, filepath):
                    downloaded_count += 1
                else:
                    failed_count += 1
            else:
                print(f"❌ No se encontró logo para {team_code}")
                failed_count += 1
            
            # Pausa para no sobrecargar el servidor
            time.sleep(1)
            
        except Exception as e:
            print(f"❌ Error procesando {team_code}: {e}")
            failed_count += 1
    
    print(f"\n📊 Resumen:")
    print(f"✅ Logos descargados: {downloaded_count}")
    print(f"❌ Fallos: {failed_count}")
    print(f"📁 Directorio: {assets_dir}")
    
    if downloaded_count > 0:
        print(f"\n🎉 ¡Logos guardados exitosamente!")
        print("Ahora puedes actualizar el StaticDataManager para usar estos assets locales.")
    else:
        print(f"\n😞 No se pudieron descargar logos. Verifica la conectividad a internet.")

if __name__ == "__main__":
    main()
