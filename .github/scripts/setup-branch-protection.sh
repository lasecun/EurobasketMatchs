#!/bin/bash

# 🛡️ Script para configurar protección de rama main
# Ejecutar desde la raíz del proyecto

echo "🔧 Configurando protección para rama main..."

# Verificar si gh está instalado y autenticado
if ! command -v gh &> /dev/null; then
    echo "❌ GitHub CLI no está instalado"
    echo "📖 Instalar: https://cli.github.com/"
    exit 1
fi

# Verificar autenticación
if ! gh auth status &> /dev/null; then
    echo "❌ No estás autenticado con GitHub CLI"
    echo "🔑 Ejecuta: gh auth login"
    exit 1
fi

echo "✅ GitHub CLI configurado correctamente"

# Intentar configurar reglas básicas con comando simplificado
echo "🔧 Aplicando reglas básicas de protección..."

gh api repos/lasecun/EurobasketMatchs/branches/main/protection \
  --method PUT \
  --field required_pull_request_reviews='{"required_approving_review_count":1}' \
  --field enforce_admins=false \
  --field restrictions=null

if [ $? -eq 0 ]; then
    echo "✅ Reglas básicas aplicadas correctamente"
    echo "🎉 ¡Configuración completada!"
    echo "📖 Revisar: https://github.com/lasecun/EurobasketMatchs/settings/branches"
else
    echo "❌ Error al aplicar reglas de protección (típico error 422)"
    echo ""
    echo "🔧 SOLUCIÓN: Configurar manualmente en GitHub:"
    echo "   1. Ve a: https://github.com/lasecun/EurobasketMatchs/settings/branches"
    echo "   2. Clic en 'Add rule'"
    echo "   3. Branch name pattern: main"
    echo "   4. Marcar: 'Require a pull request before merging'"
    echo "   5. Marcar: 'Require approvals: 1'"
    echo "   6. Guardar con 'Create'"
    echo ""
    exit 1
fi
