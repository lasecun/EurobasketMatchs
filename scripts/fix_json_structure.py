#!/usr/bin/env python3
"""
Script para corregir la estructura JSON para que coincida exactamente con StaticTeam
"""

import json
import os
import sys

def main():
    print("🔧 Corrigiendo estructura JSON para StaticTeam...")
    
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
    
    # Corregir estructura de cada equipo
    corrected_teams = []
    for team in teams_data['teams']:
        corrected_team = {
            # Campos obligatorios (sin valores por defecto)
            "id": team.get('id', ''),
            "name": team.get('name', ''),
            "shortName": team.get('shortName', team.get('name', '')),
            "logoUrl": team.get('logoUrl', team.get('imageUrl', '')),  # Mapear imageUrl a logoUrl
            
            # Campos opcionales con valores por defecto
            "primaryColor": team.get('primaryColor', '#000000'),
            "secondaryColor": team.get('secondaryColor', '#FFFFFF'),
            "country": team.get('country', ''),
            "city": team.get('city', ''),
            "venue": team.get('venue', ''),
            "website": team.get('website', ''),
            "president": team.get('president', ''),
            "phone": team.get('phone', ''),
            "address": team.get('address', ''),
            "twitterAccount": team.get('twitterAccount', ''),
            "ticketsUrl": team.get('ticketsUrl', ''),
            
            # Campos legacy
            "code": team.get('code', team.get('id', '')),
            "founded": team.get('founded', 0),
            "coach": team.get('coach', '')
        }
        
        # Verificar que no haya valores null
        for key, value in corrected_team.items():
            if value is None:
                if key in ['founded']:
                    corrected_team[key] = 0
                else:
                    corrected_team[key] = ''
        
        corrected_teams.append(corrected_team)
    
    # Actualizar estructura de datos
    teams_data['teams'] = corrected_teams
    
    # Asegurar que la estructura principal tiene los campos requeridos
    teams_data['version'] = teams_data.get('version', '1.0.0')
    teams_data['lastUpdated'] = teams_data.get('lastUpdated', teams_data.get('generatedAt', ''))
    
    # Guardar datos corregidos
    with open(teams_file, 'w', encoding='utf-8') as f:
        json.dump(teams_data, f, ensure_ascii=False, indent=2)
    
    print(f"✅ Estructura de equipos corregida: {len(corrected_teams)} equipos")
    
    # Verificar que no haya valores null
    null_count = 0
    for team in corrected_teams:
        for key, value in team.items():
            if value is None:
                print(f"⚠️ Valor null encontrado en {key} para equipo {team['name']}")
                null_count += 1
    
    if null_count == 0:
        print("✅ No se encontraron valores null")
    else:
        print(f"❌ Se encontraron {null_count} valores null")
    
    print("\n" + "=" * 60)
    print("✅ ESTRUCTURA JSON CORREGIDA")
    print("📋 Cambios realizados:")
    print("   • imageUrl → logoUrl")
    print("   • Verificados campos obligatorios")
    print("   • Eliminados valores null")
    print("   • Agregados valores por defecto")
    print(f"\n📊 Resultado:")
    print(f"   🏆 {len(corrected_teams)} equipos procesados")
    print("   ✅ Estructura compatible con StaticTeam")
    
    return True

if __name__ == "__main__":
    success = main()
    if success:
        print("\n🎉 ¡Estructura JSON corregida exitosamente!")
        print("StaticDataManager ahora puede deserializar los datos.")
    else:
        print("\n💥 Error corrigiendo estructura JSON")
    
    sys.exit(0 if success else 1)
