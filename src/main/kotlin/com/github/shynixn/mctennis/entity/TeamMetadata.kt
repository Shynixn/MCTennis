package com.github.shynixn.mctennis.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.shynixn.mcutils.common.Vector3d

class TeamMetadata {
    /**
     * Team displayName.
     */
    var name: String = ""

    /**
     * Right upper corner.
     */
    @JsonProperty("corner1")
    @JsonIgnoreProperties(value = arrayOf("blockX", "blockY", "blockZ", "empty"))
    var rightUpperCorner: Vector3d = Vector3d("world")

    /**
     * Left lower corner.
     */
    @JsonProperty("corner2")
    @JsonIgnoreProperties(value = arrayOf("blockX", "blockY", "blockZ", "empty"))
    var leftLowerCorner: Vector3d = Vector3d("world")

    /**
     * All  player spawnpoints.
     */
    @JsonIgnoreProperties(value = arrayOf("blockX", "blockY", "blockZ", "empty"))
    var spawnpoints = arrayListOf(Vector3d("world"))

    /**
     * Spawnpoint in the team lobby.
     */
    @JsonIgnoreProperties(value = arrayOf("blockX", "blockY", "blockZ", "empty"))
    var lobbySpawnpoint: Vector3d = Vector3d("world")

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
}
