#!/usr/bin/env python3
"""
Script para actualizar las URLs de logos en teams_2025_26.json para usar assets locales
"""

import json
import os

def main():
    print("🔄 Actualizando URLs de logos para usar assets locales...")
    
    # Leer el archivo JSON actual
    teams_file = "app/src/main/assets/static_data/teams_2025_26.json"
    
    with open(teams_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # Mapeo de códigos de equipo a archivos de logos
    logo_mapping = {
        "IST": "file:///android_asset/team_logos/ist_logo.png",
        "MCO": "file:///android_asset/team_logos/mco_logo.png",
        "BAS": "file:///android_asset/team_logos/bkn_logo.png",  # Baskonia
        "RED": "file:///android_asset/team_logos/red_logo.png",
        "DUB": "file:///android_asset/team_logos/dub_logo.png",
        "MIL": "file:///android_asset/team_logos/mil_logo.png",
        "BAR": "file:///android_asset/team_logos/bar_logo.png",
        "MUN": "file:///android_asset/team_logos/mun_logo.png",
        "ULK": "file:///android_asset/team_logos/ulk_logo.png",
        "HTA": "file:///android_asset/team_logos/hta_logo.png",
        "ASV": "file:///android_asset/team_logos/asv_logo.png",
        "TEL": "file:///android_asset/team_logos/tel_logo.png",
        "OLY": "file:///android_asset/team_logos/oly_logo.png",
        "PAN": "file:///android_asset/team_logos/pan_logo.png",
        "PRS": "file:///android_asset/team_logos/prs_logo.png",
        "PAR": "file:///android_asset/team_logos/par_logo.png",
        "MAD": "file:///android_asset/team_logos/mad_logo.png",
        "PAM": "file:///android_asset/team_logos/pam_logo.png",
        "VIR": "file:///android_asset/team_logos/vir_logo.png",
        "ZAL": "file:///android_asset/team_logos/zal_logo.png"
    }
    
    updated_count = 0
    
    # Actualizar cada equipo
    for team in data["teams"]:
        team_code = team.get("code") or team.get("id")
        
        if team_code in logo_mapping:
            # Intentar ambos nombres de campo para compatibilidad
            logo_field = "logoUrl" if "logoUrl" in team else "imageUrl"
            old_url = team.get(logo_field, "")
            new_url = logo_mapping[team_code]
            
            team[logo_field] = new_url
            updated_count += 1
            
            print(f"✅ {team_code} - {team['name']}: {new_url}")
        else:
            print(f"❌ {team_code} - {team['name']}: No logo encontrado")
    
    # Guardar el archivo actualizado
    with open(teams_file, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
    
    print(f"\n📊 Resumen:")
    print(f"✅ Equipos actualizados: {updated_count}")
    print(f"📁 Archivo: {teams_file}")
    print(f"\n🎉 ¡URLs de logos actualizadas exitosamente!")
    print("Los equipos ahora usarán los logos oficiales descargados como assets locales.")

if __name__ == "__main__":
    main()
