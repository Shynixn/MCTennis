package com.github.shynixn.mctennis.entity

import com.github.shynixn.mcutils.Vector3d
import com.github.shynixn.mcutils.arena.api.Arena
import org.bukkit.inventory.ItemStack

class TennisArena : Arena {
    /**
     * Unique Identifier of the arena.
     */
    override var name: String = ""

    /**
     * Display Name of the arena.
     */
    override var displayName: String = ""

    /**
     * Gets if the arena is enabled.
     */
    override var isEnabled: Boolean = false

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
     * All red player spawnpoints.
     */
    var redPlayerSpawnpoints = ArrayList<Vector3d>()

    /**
     * All blue player spawnpoints.
     */
    var bluePlayerSpawnpoints = ArrayList<Vector3d>()

    /**
     * Leave spawnpoint.
     */
    var leaveSpawnpoint: Vector3d = Vector3d()

    /**
     * Red team spawnpoint.
     */
    var redTeamLobbySpawnpoint: Vector3d = Vector3d()

    /**
     * Blue team spawnpoint.
     */
    var blueTeamLobbySpawnpoint: Vector3d = Vector3d()


    var redTeamInventoryContents: Array<ItemStack?> = emptyArray()

    var blueTeamInventoryContents: Array<ItemStack?> = emptyArray()
}
