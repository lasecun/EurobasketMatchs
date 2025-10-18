#!/bin/bash

echo "🏀 Probando endpoints de la API de EuroLeague..."

# URL base de la API
BASE_URL="https://api-live.euroleague.net"

# Parámetros actuales
COMPETITION="E"
SEASON="E2025"

echo ""
echo "=== 1. Probando endpoint base ==="
curl -I "$BASE_URL/"

echo ""
echo "=== 2. Probando endpoint de competiciones ==="
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v3/competitions" | head -5

echo ""
echo "=== 3. Probando endpoint de temporadas ==="
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v3/competitions/$COMPETITION/seasons/$SEASON" | head -5

echo ""
echo "=== 4. Probando endpoint de partidos (actual que falla) ==="
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v3/competitions/$COMPETITION/seasons/$SEASON/games" | head -5

echo ""
echo "=== 5. Probando con temporada anterior (E2024) ==="
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v3/competitions/$COMPETITION/seasons/E2024/games" | head -5

echo ""
echo "=== 6. Probando endpoint de equipos ==="
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v3/competitions/$COMPETITION/seasons/$SEASON/clubs" | head -5

echo ""
echo "=== 7. Probando diferentes versiones de API ==="
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v2/competitions/$COMPETITION/seasons/$SEASON/games" | head -5
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v1/competitions/$COMPETITION/seasons/$SEASON/games" | head -5

echo ""
echo "=== 8. Probando sin parámetros opcionales ==="
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v3/competitions/$COMPETITION/games" | head -5

echo ""
echo "=== 9. Probando endpoint de documentación ==="
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/docs" | head -5
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/swagger" | head -5

echo ""
echo "✅ Pruebas completadas. Revisa los códigos HTTP para encontrar endpoints que funcionen."
