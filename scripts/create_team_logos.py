#!/usr/bin/env python3
"""
Script para crear logos simples con iniciales de equipos como assets locales
"""

import json
import os
import sys
from PIL import Image, ImageDraw, ImageFont

def create_team_logo(team_code, team_name, primary_color="#000000", secondary_color="#FFFFFF", size=128):
    """Crea un logo simple con las iniciales del equipo"""
    try:
        # Crear imagen
        img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        draw = ImageDraw.Draw(img)
        
        # Convertir colores hex a RGB
        if primary_color.startswith('#'):
            primary_rgb = tuple(int(primary_color[i:i+2], 16) for i in (1, 3, 5))
        else:
            primary_rgb = (0, 0, 0)
            
        if secondary_color.startswith('#'):
            secondary_rgb = tuple(int(secondary_color[i:i+2], 16) for i in (1, 3, 5))
        else:
            secondary_rgb = (255, 255, 255)
        
        # Dibujar círculo de fondo
        margin = 8
        draw.ellipse([margin, margin, size-margin, size-margin], fill=primary_rgb)
        
        # Obtener iniciales (máximo 3 caracteres)
        initials = team_code[:3] if len(team_code) <= 3 else team_code[:2]
        
        # Calcular tamaño de fuente
        font_size = max(20, size // 4)
        
        # Intentar usar fuente del sistema, si no usar la por defecto
        try:
            font = ImageFont.truetype("/System/Library/Fonts/Arial.ttf", font_size)
        except:
            try:
                font = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", font_size)
            except:
                font = ImageFont.load_default()
        
        # Calcular posición del texto centrado
        bbox = draw.textbbox((0, 0), initials, font=font)
        text_width = bbox[2] - bbox[0]
        text_height = bbox[3] - bbox[1]
        
        x = (size - text_width) // 2
        y = (size - text_height) // 2 - 2  # Ajuste visual
        
        # Dibujar texto
        draw.text((x, y), initials, fill=secondary_rgb, font=font)
        
        return img
        
    except Exception as e:
        print(f"❌ Error creando logo para {team_code}: {e}")
        return None

def main():
    print("🎨 Creando logos simples con iniciales de equipos...")
    
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
    
    # Cargar datos de equipos
    print("📥 Cargando datos de equipos...")
    with open(teams_file, 'r', encoding='utf-8') as f:
        teams_data = json.load(f)
    
    # Crear logos para cada equipo
    print("🎨 Creando logos...")
    created_logos = {}
    
    for team in teams_data['teams']:
        team_code = team.get('code', team.get('id', ''))
        team_name = team.get('name', '')
        primary_color = team.get('primaryColor', '#000000')
        secondary_color = team.get('secondaryColor', '#FFFFFF')
        
        print(f"🎨 Creando logo para {team_name} ({team_code})...")
        
        # Crear logo
        logo_img = create_team_logo(team_code, team_name, primary_color, secondary_color)
        
        if logo_img:
            # Guardar imagen
            filename = f"{team_code.lower()}_logo.png"
            filepath = os.path.join(logos_dir, filename)
            logo_img.save(filepath, 'PNG')
            
            # Ruta para Android
            android_path = f"file:///android_asset/team_logos/{filename}"
            created_logos[team_code] = android_path
            print(f"✅ {team_code}: {filename}")
        else:
            print(f"❌ {team_code}: Error creando logo")
    
    # Actualizar URLs de logos
    print("\n📝 Actualizando datos de equipos...")
    updated_count = 0
    for team in teams_data['teams']:
        team_code = team.get('code', team.get('id', ''))
        
        if team_code in created_logos:
            old_logo = team.get('logoUrl', '')
            team['logoUrl'] = created_logos[team_code]
            if old_logo != team['logoUrl']:
                updated_count += 1
                print(f"✅ {team['name']}: Logo local asignado")
    
    # Guardar datos actualizados
    with open(teams_file, 'w', encoding='utf-8') as f:
        json.dump(teams_data, f, ensure_ascii=False, indent=2)
    
    print("\n" + "=" * 60)
    print("✅ LOGOS CREADOS Y CONFIGURADOS")
    print(f"📊 Resumen:")
    print(f"   🎨 {len(created_logos)} logos creados como assets")
    print(f"   📝 {updated_count} equipos actualizados") 
    print(f"   📁 Guardados en: app/src/main/assets/team_logos/")
    print(f"   🔗 URLs actualizadas a rutas locales")
    
    print(f"\n📂 Archivos creados:")
    for team_code in created_logos:
        filename = f"{team_code.lower()}_logo.png"
        print(f"   • {filename}")
    
    print(f"\n🎯 Características:")
    print(f"   🎨 Logos con iniciales del equipo")
    print(f"   🎨 Colores primarios y secundarios del equipo")
    print(f"   ⚡ Carga instantánea")
    print(f"   📱 Funcionamiento offline")
    
    return True

if __name__ == "__main__":
    try:
        import PIL
    except ImportError:
        print("❌ Error: PIL (Pillow) no está instalado")
        print("Instala con: pip install Pillow")
        sys.exit(1)
    
    success = main()
    if success:
        print("\n🎉 ¡Logos creados y configurados como assets!")
        print("Los equipos ahora tendrán logos locales con sus iniciales.")
    else:
        print("\n💥 Error creando logos")
    
    sys.exit(0 if success else 1)
