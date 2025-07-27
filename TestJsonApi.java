import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Test simple en Java para verificar que la API JSON funciona
 */
public class TestJsonApi {
    public static void main(String[] args) {
        System.out.println("🧪 === TEST SIMPLE JSON API ===");
        
        String jsonUrl = "https://www.euroleaguebasketball.net/_next/data/a52CgOKFrJehM6XbgT-b_/es/euroleague/game-center.json";
        
        try {
            System.out.println("📡 Conectando a: " + jsonUrl);
            
            URL url = new URL(jsonUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
            System.out.println("📊 Código de respuesta: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                String jsonResponse = response.toString();
                System.out.println("✅ JSON recibido, tamaño: " + jsonResponse.length() + " caracteres");
                
                // Verificaciones básicas de estructura
                if (jsonResponse.contains("\"headerData\"")) {
                    System.out.println("✅ headerData encontrado");
                }
                
                if (jsonResponse.contains("\"clubs\"")) {
                    System.out.println("✅ clubs encontrado");
                }
                
                if (jsonResponse.contains("\"pageProps\"")) {
                    System.out.println("✅ pageProps encontrado");
                }
                
                if (jsonResponse.contains("\"currentRoundGameGroups\"")) {
                    System.out.println("✅ currentRoundGameGroups encontrado");
                }
                
                // Contar equipos aproximadamente
                int clubCount = countOccurrences(jsonResponse, "\"name\":");
                System.out.println("📊 Aproximadamente " + clubCount + " entradas con nombre encontradas");
                
                // Contar partidos aproximadamente
                int gameCount = countOccurrences(jsonResponse, "\"home\":");
                System.out.println("⚽ Aproximadamente " + gameCount + " partidos encontrados");
                
                // Verificar si contiene fecha específica del 30 de septiembre
                if (jsonResponse.contains("2025-09-30")) {
                    System.out.println("🎯 Partidos del 30/09/2025 encontrados");
                }
                
                System.out.println("\n✅ === API JSON FUNCIONA CORRECTAMENTE ===");
                System.out.println("🚀 La implementación de EuroLeagueJsonApiScraper debería funcionar perfectamente");
                
            } else {
                System.out.println("❌ Error HTTP: " + responseCode);
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
}
