package com.github.shynixn.mctennis.contract

import org.bukkit.entity.Player
import org.bukkit.event.Listener

interface BedrockService : Listener {
    /**
     * All bedrock players.
     */
    val bedRockPlayers: HashSet<Player>

    /**
     * All java players.
     */
    val javaPlayers: HashSet<Player>
}
