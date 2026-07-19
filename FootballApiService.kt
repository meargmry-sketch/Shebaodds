package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class FootballFixtureResponse(
    @Json(name = "response") val response: List<FixtureItem>?
)

@JsonClass(generateAdapter = true)
data class FixtureItem(
    @Json(name = "fixture") val fixture: FixtureDetails,
    @Json(name = "teams") val teams: TeamDetails,
    @Json(name = "goals") val goals: GoalDetails
)

@JsonClass(generateAdapter = true)
data class FixtureDetails(
    @Json(name = "id") val id: Int,
    @Json(name = "date") val date: String,
    @Json(name = "status") val status: StatusDetails
)

@JsonClass(generateAdapter = true)
data class StatusDetails(
    @Json(name = "short") val short: String
)

@JsonClass(generateAdapter = true)
data class TeamDetails(
    @Json(name = "home") val home: TeamInfo,
    @Json(name = "away") val away: TeamInfo
)

@JsonClass(generateAdapter = true)
data class TeamInfo(
    @Json(name = "name") val name: String,
    @Json(name = "logo") val logo: String
)

@JsonClass(generateAdapter = true)
data class GoalDetails(
    @Json(name = "home") val home: Int?,
    @Json(name = "away") val away: Int?
)

interface FootballApiService {
    @GET("fixtures")
    suspend fun getLeagueFixtures(
        @Header("x-apisports-key") apiKey: String,
        @Query("league") leagueId: Int,
        @Query("season") seasonYear: Int
    ): Response<FootballFixtureResponse>
}
