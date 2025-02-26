package com.github.shynixn.mctennis.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.command.CommandMeta
import com.github.shynixn.mcutils.common.command.CommandType

class TeamMetadata {
    /**
     * Right upper corner.
     */
    @JsonProperty("corner1")
    @JsonIgnoreProperties(value = arrayOf("blockX", "blockY", "blockZ", "empty", "direction"))
    var rightUpperCorner: Vector3d = Vector3d("world")

    /**
     * Left lower corner.
     */
    @JsonProperty("corner2")
    @JsonIgnoreProperties(value = arrayOf("blockX", "blockY", "blockZ", "empty", "direction"))
    var leftLowerCorner: Vector3d = Vector3d("world")

    /**
     * All  player spawnpoints.
     */
    @JsonIgnoreProperties(value = arrayOf("blockX", "blockY", "blockZ", "empty", "direction"))
    var spawnpoints = arrayListOf(Vector3d("world"))

    /**
     * Spawnpoint in the team lobby.
     */
    @JsonIgnoreProperties(value = arrayOf("blockX", "blockY", "blockZ", "empty", "direction"))
    var lobbySpawnpoint: Vector3d = Vector3d("world")

    /**
     * Team Name
     */
    var name : String?  = null

    /**
     * ItemStacks during the game.
     */
    var inventoryContents: Array<String?> = emptyArray()

    /**
     * ItemStacks during the game.
     */
    var armorInventoryContents: Array<String?> = emptyArray()

    /**
     * Commands executed on player win.
     */
    var winCommands: List<CommandMeta> = ArrayList()

    /**
     * Commands executed on player loose.
     */
    var looseCommands: List<CommandMeta> = ArrayList()

    /**
     * Commands executed on player raw.
     */
    var drawCommands: List<CommandMeta> = ArrayList()

    /**
     * Commands executed on player join.
     */
    var joinCommands: List<CommandMeta> = listOf(
        CommandMeta(
            CommandType.SERVER_PER_PLAYER,
            "/mctennisscoreboard add mctennis_scoreboard %mctennis_player_name%"
        )
    )

    /**
     * Commands executed on player leave.
     */
    var leaveCommands: List<CommandMeta> = listOf(
        CommandMeta(
            CommandType.SERVER_PER_PLAYER,
            "/mctennisscoreboard remove mctennis_scoreboard %mctennis_player_name%"
        )
    )

    /**
     * Commands which are executed every game tick.
     */
    var tickCommands: List<CommandMeta> = ArrayList()
}
