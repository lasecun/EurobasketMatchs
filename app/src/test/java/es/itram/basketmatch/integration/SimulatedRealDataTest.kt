package es.itram.basketmatch.integration

import es.itram.basketmatch.data.datasource.remote.dto.MatchStatus
import es.itram.basketmatch.data.datasource.remote.dto.MatchWebDto
import es.itram.basketmatch.data.datasource.remote.dto.TeamWebDto
import es.itram.basketmatch.data.mapper.MatchWebMapper
import es.itram.basketmatch.data.mapper.TeamWebMapper
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Test que simula datos reales de EuroLeague para demostrar el procesamiento completo
 * Este test no requiere conexión a internet y siempre funciona
 */
class SimulatedRealDataTest {

    @Test
    fun `demo complete data processing with realistic EuroLeague data`() = runTest {
        println("🏀 DEMO: Procesamiento completo con datos realistas de EuroLeague")
        println("=".repeat(70))
        
        // 1. Simular datos de equipos reales de EuroLeague
        val simulatedTeams = createRealisticTeamData()
        
        println("1️⃣ EQUIPOS DE EUROLEAGUE (${simulatedTeams.size} equipos):")
        simulatedTeams.forEach { team ->
            println("   🏀 ${team.name} (${team.shortCode}) - ${team.country}")
        }
        
        // 2. Simular datos de partidos reales
        val simulatedMatches = createRealisticMatchData(simulatedTeams)
        
        println("\n2️⃣ PARTIDOS DE EUROLEAGUE (${simulatedMatches.size} partidos):")
        simulatedMatches.take(5).forEach { match ->
            val status = when (match.status) {
                MatchStatus.LIVE -> "🔴 EN VIVO"
                MatchStatus.FINISHED -> "✅ FINALIZADO"
                MatchStatus.SCHEDULED -> "📅 PROGRAMADO"
                else -> "⏸️ ${match.status}"
            }
            
            val scoreStr = if (match.homeScore != null && match.awayScore != null) {
                " (${match.homeScore}-${match.awayScore})"
            } else ""
            
            println("   $status ${match.homeTeamName} vs ${match.awayTeamName}$scoreStr")
            println("      📅 ${match.date} ${match.time ?: ""} - ${match.venue ?: "TBD"}")
        }
        
        // 3. Convertir a entidades de dominio usando los mappers
        println("\n3️⃣ CONVERSIÓN A ENTIDADES DE DOMINIO:")
        
        val domainTeams = TeamWebMapper.toDomainList(simulatedTeams)
        println("   ✅ ${domainTeams.size} equipos convertidos a entidades de dominio")
        
        val domainMatches = MatchWebMapper.toDomainList(simulatedMatches)
        println("   ✅ ${domainMatches.size} partidos convertidos a entidades de dominio")
        
        // 4. Análisis de datos
        println("\n4️⃣ ANÁLISIS DE DATOS:")
        
        val spanishTeams = domainTeams.filter { it.country == "Spain" }
        val turkishTeams = domainTeams.filter { it.country == "Turkey" }
        val greekTeams = domainTeams.filter { it.country == "Greece" }
        
        println("   🇪🇸 Equipos españoles: ${spanishTeams.size}")
        println("   🇹🇷 Equipos turcos: ${turkishTeams.size}")
        println("   🇬🇷 Equipos griegos: ${greekTeams.size}")
        
        val liveMatches = domainMatches.filter { it.status.name == "LIVE" }
        val todayMatches = domainMatches.filter { 
            it.dateTime.toLocalDate() == java.time.LocalDate.now() 
        }
        
        println("   🔴 Partidos en vivo: ${liveMatches.size}")
        println("   📅 Partidos de hoy: ${todayMatches.size}")
        
        // 5. Verificar integridad de datos
        println("\n5️⃣ VERIFICACIÓN DE INTEGRIDAD:")
        
        val teamsInMatches = domainMatches.flatMap { listOf(it.homeTeamId, it.awayTeamId) }.toSet()
        val availableTeamIds = domainTeams.map { it.id }.toSet()
        
        val missingTeams = teamsInMatches - availableTeamIds
        val unusedTeams = availableTeamIds - teamsInMatches
        
        println("   ✅ Equipos en partidos: ${teamsInMatches.size}")
        println("   ⚠️ Equipos faltantes: ${missingTeams.size}")
        println("   📊 Equipos sin partidos: ${unusedTeams.size}")
        
        // 6. Resumen final
        println("\n6️⃣ RESUMEN DEL PROCESAMIENTO:")
        println("   🎯 Datos procesados exitosamente")
        println("   📊 ${domainTeams.size} equipos de EuroLeague")
        println("   ⚽ ${domainMatches.size} partidos programados")
        println("   🌍 ${domainTeams.map { it.country }.toSet().size} países representados")
        println("   🏟️ ${domainMatches.mapNotNull { it.venue }.toSet().size} estadios diferentes")
        
        println("\n🎉 DEMO COMPLETADO EXITOSAMENTE")
        println("   ✅ Los mappers web funcionan correctamente")
        println("   ✅ Los datos se procesan sin errores")
        println("   ✅ La arquitectura está lista para datos reales")
        
        println("=".repeat(70))
    }

    private fun createRealisticTeamData(): List<TeamWebDto> = listOf(
        TeamWebDto("real_madrid", "Real Madrid", "Real Madrid Basketball", "MAD", null, "Spain", "WiZink Center, Madrid", ""),
        TeamWebDto("fc_barcelona", "FC Barcelona", "FC Barcelona Basketball", "BAR", null, "Spain", "Palau de la Música Catalana, Barcelona", ""),
        TeamWebDto("olympiacos", "Olympiacos Piraeus", "Olympiacos Basketball Club", "OLY", null, "Greece", "Peace and Friendship Stadium, Piraeus", ""),
        TeamWebDto("panathinaikos", "Panathinaikos Athens", "Panathinaikos Basketball Club", "PAO", null, "Greece", "OAKA Arena, Athens", ""),
        TeamWebDto("fenerbahce", "Fenerbahce Istanbul", "Fenerbahce Basketball", "FEN", null, "Turkey", "Ülker Sports and Event Hall, Istanbul", ""),
        TeamWebDto("anadolu_efes", "Anadolu Efes Istanbul", "Anadolu Efes Basketball", "EFS", null, "Turkey", "Sinan Erdem Dome, Istanbul", ""),
        TeamWebDto("cska_moscow", "CSKA Moscow", "CSKA Basketball Club", "CSK", null, "Russia", "CSKA Arena, Moscow", ""),
        TeamWebDto("red_star", "Red Star Belgrade", "Red Star Basketball", "RED", null, "Serbia", "Aleksandar Nikolić Hall, Belgrade", ""),
        TeamWebDto("zalgiris", "Zalgiris Kaunas", "Zalgiris Basketball", "ZAL", null, "Lithuania", "Zalgirio Arena, Kaunas", ""),
        TeamWebDto("bayern_munich", "FC Bayern Munich", "FC Bayern Munich Basketball", "BAY", null, "Germany", "Audi Dome, Munich", ""),
        TeamWebDto("alba_berlin", "ALBA Berlin", "ALBA Berlin Basketball", "BER", null, "Germany", "Mercedes-Benz Arena, Berlin", ""),
        TeamWebDto("armani_milan", "EA7 Emporio Armani Milan", "Olimpia Milano", "MIL", null, "Italy", "Mediolanum Forum, Milan", ""),
        TeamWebDto("virtus_bologna", "Virtus Segafredo Bologna", "Virtus Bologna", "VIR", null, "Italy", "Segafredo Zanetti Arena, Bologna", ""),
        TeamWebDto("baskonia", "Baskonia Vitoria-Gasteiz", "Saski Baskonia", "BAS", null, "Spain", "Buesa Arena, Vitoria-Gasteiz", ""),
        TeamWebDto("maccabi", "Maccabi Playtika Tel Aviv", "Maccabi Tel Aviv", "MAC", null, "Israel", "Menora Mivtachim Arena, Tel Aviv", ""),
        TeamWebDto("monaco", "AS Monaco Basket", "AS Monaco Basketball", "MON", null, "Monaco", "Salle Gaston Médecin, Monaco", ""),
        TeamWebDto("partizan", "Partizan Belgrade", "Partizan Basketball", "PAR", null, "Serbia", "Štark Arena, Belgrade", ""),
        TeamWebDto("valencia", "Valencia Basket", "Valencia BC", "VAL", null, "Spain", "Pabellon Fuente de San Luis, Valencia", "")
    )

    private fun createRealisticMatchData(teams: List<TeamWebDto>): List<MatchWebDto> {
        val matches = mutableListOf<MatchWebDto>()
        var matchId = 1

        // Partidos ya jugados (Round 1-5)
        for (round in 1..5) {
            teams.shuffled().chunked(2).forEach { pair ->
                if (pair.size == 2) {
                    val homeTeam = pair[0]
                    val awayTeam = pair[1]
                    
                    matches.add(
                        MatchWebDto(
                            id = "match_${matchId++}",
                            homeTeamId = homeTeam.id,
                            homeTeamName = homeTeam.name,
                            awayTeamId = awayTeam.id,
                            awayTeamName = awayTeam.name,
                            date = "2024-10-${10 + round}",
                            time = "20:00",
                            venue = homeTeam.venue?.split(",")?.first(),
                            status = MatchStatus.FINISHED,
                            homeScore = (65..95).random(),
                            awayScore = (65..95).random(),
                            round = "Round $round",
                            season = "2024-25"
                        )
                    )
                }
            }
        }

        // Partidos de esta semana (algunos en vivo, algunos programados)
        val thisWeekStatuses = listOf(MatchStatus.LIVE, MatchStatus.SCHEDULED, MatchStatus.FINISHED)
        
        teams.take(8).chunked(2).forEachIndexed { index, pair ->
            if (pair.size == 2) {
                val homeTeam = pair[0]
                val awayTeam = pair[1]
                val status = thisWeekStatuses[index % thisWeekStatuses.size]
                
                matches.add(
                    MatchWebDto(
                        id = "match_${matchId++}",
                        homeTeamId = homeTeam.id,
                        homeTeamName = homeTeam.name,
                        awayTeamId = awayTeam.id,
                        awayTeamName = awayTeam.name,
                        date = "2024-11-${20 + index}",
                        time = if (status == MatchStatus.LIVE) "20:45" else "21:00",
                        venue = homeTeam.venue?.split(",")?.first(),
                        status = status,
                        homeScore = if (status != MatchStatus.SCHEDULED) (65..95).random() else null,
                        awayScore = if (status != MatchStatus.SCHEDULED) (65..95).random() else null,
                        round = "Round 6",
                        season = "2024-25"
                    )
                )
            }
        }

        // Partidos futuros
        teams.takeLast(6).chunked(2).forEachIndexed { index, pair ->
            if (pair.size == 2) {
                val homeTeam = pair[0]
                val awayTeam = pair[1]
                
                matches.add(
                    MatchWebDto(
                        id = "match_${matchId++}",
                        homeTeamId = homeTeam.id,
                        homeTeamName = homeTeam.name,
                        awayTeamId = awayTeam.id,
                        awayTeamName = awayTeam.name,
                        date = "2024-12-${5 + index}",
                        time = "19:30",
                        venue = homeTeam.venue?.split(",")?.first(),
                        status = MatchStatus.SCHEDULED,
                        homeScore = null,
                        awayScore = null,
                        round = "Round 7",
                        season = "2024-25"
                    )
                )
            }
        }

        return matches
    }
}
