#!/bin/bash

echo "🏀 Probando el endpoint correcto que encontraste..."

BASE_URL="https://api-live.euroleague.net"

echo ""
echo "=== Probando endpoint de report específico ==="
echo "Game 1 report (E2025):"
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v3/competitions/E/seasons/E2025/games/1/report" | head -10

echo ""
echo "Game 2 report (E2025):"
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v3/competitions/E/seasons/E2025/games/2/report" | head -10

echo ""
echo "=== Probando endpoint de games para obtener lista ==="
echo "Lista de games (E2025):"
curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v3/competitions/E/seasons/E2025/games" | head -10

echo ""
echo "=== Probando con diferentes gameCodes ==="
for i in {1..5}; do
  echo "Game $i report:"
  curl -s -w "HTTP Status: %{http_code}\n" "$BASE_URL/v3/competitions/E/seasons/E2025/games/$i/report" | head -3
  echo ""
done

echo "✅ Pruebas del endpoint correcto completadas."
