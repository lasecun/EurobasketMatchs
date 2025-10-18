#!/bin/bash

echo "🏀 Probando combinaciones específicas después del error UnsupportedApiVersion..."

BASE_URL="https://api-live.euroleague.net"

echo ""
echo "=== Probando temporadas disponibles ==="
echo "E2024 (temporada 2023-24):"
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v3/competitions/E/seasons/E2024/games" | head -3

echo ""
echo "E2023 (temporada 2022-23):"
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v3/competitions/E/seasons/E2023/games" | head -3

echo ""
echo "=== Probando diferentes versiones de API ==="
echo "API v2 con E2024:"
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v2/competitions/E/seasons/E2024/games" | head -3

echo ""
echo "API v1 con E2024:"
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v1/competitions/E/seasons/E2024/games" | head -3

echo ""
echo "=== Probando estructura alternativa ==="
echo "Sin /seasons/:"
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v3/competitions/E/games" | head -3

echo ""
echo "=== Probando método POST en lugar de GET ==="
echo "POST v3:"
curl -X POST -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v3/competitions/E/seasons/E2024/games" | head -3

echo ""
echo "=== Probando endpoint específico de un partido conocido ==="
echo "Detalle de partido específico (si existe):"
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v3/competitions/E/seasons/E2024/games/1" | head -3

echo ""
echo "=== Verificando qué temporadas están disponibles ==="
echo "Listando temporadas:"
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v3/competitions/E/seasons" | head -5

echo ""
echo "✅ Pruebas específicas completadas."
