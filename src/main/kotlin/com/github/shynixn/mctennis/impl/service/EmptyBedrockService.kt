package com.github.shynixn.mctennis.impl.service

import com.github.shynixn.mctennis.contract.BedrockService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class EmptyBedrockService : BedrockService {
    /**
     * All bedrock players.
     */
    override val bedRockPlayers: HashSet<Player> = HashSet()

    /**
     * All java players.
     */
    override val javaPlayers: HashSet<Player> = HashSet()

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        if (javaPlayers.contains(event.player)) {
            javaPlayers.remove(event.player)
        }
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        javaPlayers.add(event.player)
    }
}
