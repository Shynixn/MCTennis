package com.github.shynixn.mctennis.entity

import com.github.shynixn.mcutils.Vector3d
import com.github.shynixn.mcutils.arena.api.Arena

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
     * Game time.
     */
    var gameTime : Int = 300

    /**
     * Leave spawnpoint.
     */
    var leaveSpawnpoint: Vector3d = Vector3d()

    /**
     * Gets the redteam meta.
     */
    var redTeamMeta : TeamMetadata = TeamMetadata()

    /**
     * BlueTeam.
     */
    var blueTeamMeta : TeamMetadata = TeamMetadata()
}
