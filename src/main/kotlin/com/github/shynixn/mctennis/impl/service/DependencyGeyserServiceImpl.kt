package com.github.shynixn.mctennis.impl.service

import com.github.shynixn.mctennis.contract.BedrockService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.geysermc.api.Geyser

class DependencyGeyserServiceImpl : Listener,
    BedrockService {
    private val bedRockStateCache = HashMap<Player, Boolean>()

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
        if (bedRockStateCache.containsKey(event.player)) {
            bedRockStateCache.remove(event.player)
        }
        if (javaPlayers.contains(event.player)) {
            javaPlayers.remove(event.player)
        }
        if (bedRockPlayers.contains(event.player)) {
            bedRockPlayers.remove(event.player)
        }
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        if (isBedRockPlayer(event.player)) {
            bedRockPlayers.add(event.player)
        } else {
            javaPlayers.add(event.player)
        }
    }

    /**
     * Gets if the given player is playing on a bedrock client.
     */
    private fun isBedRockPlayer(player: Player): Boolean {
        if (!bedRockStateCache.containsKey(player)) {
            val connection = Geyser.api().connectionByUuid(player.uniqueId)
            bedRockStateCache[player] = connection != null
        }

        return bedRockStateCache[player]!!
    }
}
