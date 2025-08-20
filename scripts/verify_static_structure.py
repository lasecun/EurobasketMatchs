#!/usr/bin/env python3
"""
Script para verificar y diagnosticar la estructura de archivos estáticos
"""

import json
import os

def verify_file_structure():
    print("🔍 Verificando estructura de archivos estáticos...")
    
    # Verificar teams_2025_26.json
    teams_file = "app/src/main/assets/static_data/teams_2025_26.json"
    print(f"\n📋 Verificando {teams_file}...")
    
    try:
        with open(teams_file, 'r', encoding='utf-8') as f:
            teams_data = json.load(f)
        
        print(f"✅ JSON válido")
        print(f"📊 Campos en el nivel raíz: {list(teams_data.keys())}")
        
        # Verificar estructura esperada por StaticDataManager
        expected_fields = ["version", "lastUpdated", "teams"]
        missing_fields = [field for field in expected_fields if field not in teams_data]
        
        if missing_fields:
            print(f"❌ Faltan campos requeridos: {missing_fields}")
        else:
            print(f"✅ Todos los campos requeridos presentes")
            
        if "teams" in teams_data:
            print(f"✅ Total equipos: {len(teams_data['teams'])}")
        else:
            print(f"❌ No se encontró el campo 'teams'")
            
    except Exception as e:
        print(f"❌ Error leyendo archivo: {e}")
    
    # Verificar matches_calendar_2025_26.json
    matches_file = "app/src/main/assets/static_data/matches_calendar_2025_26.json"
    print(f"\n📋 Verificando {matches_file}...")
    
    try:
        with open(matches_file, 'r', encoding='utf-8') as f:
            matches_data = json.load(f)
        
        print(f"✅ JSON válido")
        print(f"📊 Campos en el nivel raíz: {list(matches_data.keys())}")
        
        # Verificar estructura esperada por StaticDataManager
        expected_fields = ["version", "lastUpdated", "season", "totalRounds", "description", "note", "matches"]
        missing_fields = [field for field in expected_fields if field not in matches_data]
        
        if missing_fields:
            print(f"❌ Faltan campos requeridos: {missing_fields}")
        else:
            print(f"✅ Todos los campos requeridos presentes")
            
        if "matches" in matches_data:
            print(f"✅ Total partidos: {len(matches_data['matches'])}")
            
            # Verificar estructura de un partido
            if matches_data["matches"]:
                sample_match = matches_data["matches"][0]
                expected_match_fields = ["id", "round", "homeTeamCode", "awayTeamCode", "venue", "season", "status", "dateTime", "homeScore", "awayScore"]
                match_missing = [field for field in expected_match_fields if field not in sample_match]
                
                if match_missing:
                    print(f"❌ Faltan campos en partidos: {match_missing}")
                    print(f"📊 Campos presentes en partido: {list(sample_match.keys())}")
                else:
                    print(f"✅ Estructura de partidos correcta")
        else:
            print(f"❌ No se encontró el campo 'matches'")
            
    except Exception as e:
        print(f"❌ Error leyendo archivo: {e}")
    
    # Verificar data_version.json
    version_file = "app/src/main/assets/static_data/data_version.json"
    print(f"\n📋 Verificando {version_file}...")
    
    try:
        with open(version_file, 'r', encoding='utf-8') as f:
            version_data = json.load(f)
        
        print(f"✅ JSON válido")
        print(f"📊 Campos en el nivel raíz: {list(version_data.keys())}")
        
        # Verificar estructura esperada por StaticDataManager
        expected_fields = ["version", "lastUpdated", "description", "staticDataVersions", "lastStaticDataUpdate", "dynamicDataVersions", "syncConfig"]
        missing_fields = [field for field in expected_fields if field not in version_data]
        
        if missing_fields:
            print(f"❌ Faltan campos requeridos: {missing_fields}")
        else:
            print(f"✅ Todos los campos requeridos presentes")
            
    except Exception as e:
        print(f"❌ Error leyendo archivo: {e}")

if __name__ == "__main__":
    verify_file_structure()
