package com.github.shynixn.mctennis.impl.service

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.contract.BedrockService
import com.github.shynixn.mctennis.impl.physic.PhysicObjectDispatcher
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

class BedrockServiceImpl constructor(private val plugin: Plugin, private val physicDispatcher : PhysicObjectDispatcher) : BedrockService {
    /**
     * All bedrock players.
     */
    override val bedRockPlayers: HashSet<Player> = HashSet()

    /**
     * All java players.
     */
    override val javaPlayers: HashSet<Player> = HashSet()

    /**
     * Geyser plugin.
     */
    private var geyserSpigotPlugin: DependencyGeyserSpigotServiceImpl? = null

    init {
        Bukkit.getOnlinePlayers().forEach { e ->
            plugin.launch(physicDispatcher) {
                applyPlayerToGroup(e)
            }
        }

        Bukkit.getPluginManager().registerEvents(this, plugin)
        if (Bukkit.getPluginManager().getPlugin("Geyser-Spigot") != null) {
            geyserSpigotPlugin =  DependencyGeyserSpigotServiceImpl()
        }
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        plugin.launch(physicDispatcher) {
            if (javaPlayers.contains(event.player)) {
                javaPlayers.remove(event.player)
            }
        }
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        plugin.launch(physicDispatcher) {
            applyPlayerToGroup(event.player)
        }
    }

    private fun applyPlayerToGroup(player: Player) {
        if (isBedRockPlayer(player)) {
            plugin.launch(physicDispatcher) {
                bedRockPlayers.add(player)
            }
        } else {
            plugin.launch(physicDispatcher) {
                javaPlayers.add(player)
            }
        }
    }

    /**
     * Gets if the given player is playing on a bedrock client.
     */
    private fun isBedRockPlayer(player: Player): Boolean {
        if (geyserSpigotPlugin != null) {
            return geyserSpigotPlugin!!.isBedrockPlayer(player)
        }

        return false
    }
}
