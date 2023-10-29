package com.github.shynixn.mctennis.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.repository.Element

@JsonPropertyOrder(value = arrayOf("name", "displayName", "enabled"))
class TennisArena : Element {
    /**
     * Unique Identifier of the arena.
     */
    override var name: String = ""

    /**
     * Display Name of the arena.
     */
    var displayName: String = ""

    /**
     * Gets if the arena is enabled.
     */
    var isEnabled: Boolean = false

    /**
     * Max players per team.
     */
    var maxPlayersPerTeam: Int = 1

    /**
     * Min players per team.
     */
    var minPlayersPerTeam: Int = 1

    /**
     * Time to start in seconds.
     */
    var timeToStart: Int = 10

    /**
     * Game time.
     */
    var gameTime: Int = 300

    /**
     * Amount of sets to win the entire game.
     */
    var setsToWin: Int = 3

    /**
     * Leave spawnpoint.
     */
    @JsonIgnoreProperties(value = arrayOf("blockX", "blockY", "blockZ", "empty", "direction"))
    var leaveSpawnpoint: Vector3d = Vector3d("world")

    /**
     * Gets the redteam meta.
     */
    var redTeamMeta: TeamMetadata = TeamMetadata()

    /**
     * BlueTeam.
     */
    var blueTeamMeta: TeamMetadata = TeamMetadata()

    @JsonProperty("ball")
    var ballSettings: TennisBallSettings = TennisBallSettings()
}
