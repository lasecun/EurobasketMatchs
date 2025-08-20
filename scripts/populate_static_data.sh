#!/bin/bash

# Script para poblar datos estáticos de EuroLeague 2025-26
# Este script descarga todos los datos desde la API oficial y los prepara
# para que estén disponibles desde la primera instalación de la app.

set -e  # Salir si hay algún error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

#!/bin/bash

# Script para poblar datos estáticos de EuroLeague
# Ejecuta el script Python para descargar y generar los datos estáticos

set -e

echo "🏀 Poblando datos estáticos de EuroLeague..."
echo "=================================================="

# Verificar que Python3 esté disponible
if ! command -v python3 &> /dev/null; then
    echo "❌ Error: Python3 no está instalado"
    exit 1
fi

# Ir al directorio del proyecto
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

echo "📂 Directorio del proyecto: $PROJECT_ROOT"

# Ejecutar el script Python
echo "🐍 Ejecutando script Python..."
if python3 scripts/populate_game_center_data.py; then
    echo ""
    echo "✅ ¡Datos estáticos poblados exitosamente!"
    echo "📁 Los datos están ahora disponibles en app/src/main/assets/static_data.json"
    echo ""
    echo "ℹ️  La aplicación ahora tiene todos los equipos y partidos"
    echo "    precargados para funcionar sin conexión inicial."
    echo ""
    echo "🎉 ¡Proceso completado!"
else
    echo ""
    echo "❌ Error al poblar los datos estáticos"
    exit 1
fi

# Verificar que estamos en el directorio correcto
if [ ! -d "app" ]; then
    log_error "Debes ejecutar este script desde la raíz del proyecto"
    exit 1
fi

# Crear directorio scripts si no existe
mkdir -p scripts

log_info "🚀 Iniciando población de datos estáticos EuroLeague 2025-26"
echo

# Verificar Python
if ! command -v python3 &> /dev/null; then
    log_error "Python 3 no está instalado"
    exit 1
fi

# Verificar requests
if ! python3 -c "import requests" 2>/dev/null; then
    log_warning "Instalando requests..."
    pip3 install requests
fi

# Ejecutar el script de población
log_info "📥 Descargando datos desde API EuroLeague..."
if python3 scripts/populate_static_data.py; then
    log_success "¡Datos estáticos poblados correctamente!"
    echo
    log_info "📁 Archivos generados:"
    echo "   - app/src/main/assets/static_data/teams_2025_26.json"
    echo "   - app/src/main/assets/static_data/matches_calendar_2025_26.json"
    echo
    log_success "🎉 ¡Aplicación lista! Los usuarios tendrán todos los datos desde la instalación."
else
    log_error "Falló la población de datos"
    exit 1
fi
