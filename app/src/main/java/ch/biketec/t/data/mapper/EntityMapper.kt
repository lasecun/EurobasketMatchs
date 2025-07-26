package ch.biketec.t.data.mapper

import ch.biketec.t.data.datasource.local.entity.MatchEntity
import ch.biketec.t.data.datasource.local.entity.StandingEntity
import ch.biketec.t.data.datasource.local.entity.TeamEntity
import ch.biketec.t.domain.entity.Match
import ch.biketec.t.domain.entity.SeasonType
import ch.biketec.t.domain.entity.Standing
import ch.biketec.t.domain.entity.Team

// Team mappers
fun TeamEntity.toDomain(): Team {
    return Team(
        id = id,
        name = name,
        shortName = shortName,
        city = city,
        country = country,
        logoUrl = logoUrl,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        isFavorite = isFavorite
    )
}

fun Team.toEntity(): TeamEntity {
    return TeamEntity(
        id = id,
        name = name,
        shortName = shortName,
        city = city,
        country = country,
        logoUrl = logoUrl,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        isFavorite = isFavorite
    )
}

// Match mappers
fun MatchEntity.toDomain(homeTeam: Team, awayTeam: Team): Match {
    return Match(
        id = id,
        homeTeam = homeTeam,
        awayTeam = awayTeam,
        dateTime = dateTime,
        arena = arena,
        city = city,
        round = round,
        seasonType = seasonType,
        status = status,
        homeScore = homeScore,
        awayScore = awayScore
    )
}

fun Match.toEntity(): MatchEntity {
    return MatchEntity(
        id = id,
        homeTeamId = homeTeam.id,
        awayTeamId = awayTeam.id,
        dateTime = dateTime,
        arena = arena,
        city = city,
        round = round,
        seasonType = seasonType,
        status = status,
        homeScore = homeScore,
        awayScore = awayScore
    )
}

// Standing mappers
fun StandingEntity.toDomain(team: Team): Standing {
    return Standing(
        team = team,
        position = position,
        gamesPlayed = gamesPlayed,
        wins = wins,
        losses = losses,
        pointsFor = pointsFor,
        pointsAgainst = pointsAgainst,
        winPercentage = if (gamesPlayed > 0) wins.toDouble() / gamesPlayed * 100 else 0.0
    )
}

fun Standing.toEntity(): StandingEntity {
    return StandingEntity(
        id = "${team.id}_${SeasonType.REGULAR_SEASON}",
        teamId = team.id,
        position = position,
        gamesPlayed = gamesPlayed,
        wins = wins,
        losses = losses,
        pointsFor = pointsFor,
        pointsAgainst = pointsAgainst,
        seasonType = SeasonType.REGULAR_SEASON
    )
}
