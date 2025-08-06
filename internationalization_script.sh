#!/bin/bash

# Script para reemplazar strings hardcoded con referencias a recursos
# Este script actualiza los archivos Kotlin principales

echo "Iniciando proceso de internacionalización..."

# Lista de archivos a procesar
files=(
    "app/src/main/java/es/itram/basketmatch/presentation/component/NoMatchesTodayCard.kt"
    "app/src/main/java/es/itram/basketmatch/presentation/component/MatchCard.kt"
    "app/src/main/java/es/itram/basketmatch/presentation/component/SyncProgressIndicator.kt"
    "app/src/main/java/es/itram/basketmatch/presentation/screen/MainScreen.kt"
    "app/src/main/java/es/itram/basketmatch/presentation/screen/MatchDetailScreen.kt"
    "app/src/main/java/es/itram/basketmatch/presentation/screen/PlayerDetailScreen.kt"
    "app/src/main/java/es/itram/basketmatch/presentation/screen/TeamDetailScreen.kt"
    "app/src/main/java/es/itram/basketmatch/presentation/screen/TeamRosterScreen.kt"
)

# Función para agregar import si no existe
add_import_if_needed() {
    local file="$1"
    local import_line="import es.itram.basketmatch.R"
    local stringresource_import="import androidx.compose.ui.res.stringResource"
    
    if ! grep -q "import es.itram.basketmatch.R" "$file"; then
        # Buscar la línea después de la declaración del paquete y antes de la primera función
        sed -i '' '/^import androidx\.compose\.ui\.Modifier$/a\
import androidx.compose.ui.res.stringResource\
import es.itram.basketmatch.R
' "$file"
    elif ! grep -q "import androidx.compose.ui.res.stringResource" "$file"; then
        sed -i '' '/^import es\.itram\.basketmatch\.R$/i\
import androidx.compose.ui.res.stringResource
' "$file"
    fi
}

# Procesar cada archivo
for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo "Procesando $file..."
        add_import_if_needed "$file"
    else
        echo "Archivo no encontrado: $file"
    fi
done

echo "Proceso de internacionalización iniciado. Se requiere edición manual para completar."
