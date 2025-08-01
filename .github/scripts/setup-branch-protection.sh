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

# Intentar configurar reglas básicas
echo "🔧 Aplicando reglas de protección..."

# Configuración básica de protección
gh api repos/lasecun/EurobasketMatchs/branches/main/protection \
  --method PUT \
  --field required_status_checks='{"strict":true,"checks":[]}' \
  --field enforce_admins=true \
  --field required_pull_request_reviews='{"required_approving_review_count":1,"dismiss_stale_reviews":true,"require_code_owner_reviews":false}' \
  --field restrictions=null \
  --field allow_force_pushes=false \
  --field allow_deletions=false

if [ $? -eq 0 ]; then
    echo "✅ Reglas de protección aplicadas correctamente"
    echo "📋 Verificando configuración..."
    
    # Verificar configuración
    gh api repos/lasecun/EurobasketMatchs/branches/main/protection --jq '.required_pull_request_reviews.required_approving_review_count'
    
    echo ""
    echo "🎉 ¡Configuración completada!"
    echo "📖 Revisar: https://github.com/lasecun/EurobasketMatchs/settings/branches"
else
    echo "❌ Error al aplicar reglas de protección"
    echo "🔧 Configurar manualmente en:"
    echo "   https://github.com/lasecun/EurobasketMatchs/settings/branches"
    exit 1
fi
