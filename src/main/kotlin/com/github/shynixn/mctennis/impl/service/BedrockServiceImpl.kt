package com.github.shynixn.mctennis.impl.service

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.contract.BedrockService
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcher
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import java.util.logging.Level

class BedrockServiceImpl @Inject constructor(
    private val plugin: Plugin,
    private val physicDispatcher: PhysicObjectDispatcher
) :
    BedrockService {
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
        Bukkit.getPluginManager().registerEvents(this, plugin)

        try {
            if (Bukkit.getPluginManager().getPlugin("Geyser-Spigot") != null) {
                geyserSpigotPlugin = DependencyGeyserSpigotServiceImpl()
            }
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Cannot load Geyser-Spigot integration.")
        }

        Bukkit.getOnlinePlayers().forEach { e ->
            plugin.launch(physicDispatcher) {
                applyPlayerToGroup(e)
            }
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
        try {
            if (geyserSpigotPlugin != null) {
                return geyserSpigotPlugin!!.isBedrockPlayer(player)
            }
        } catch (e: Exception) {
            // Sometimes the integration fails. Ignore in this case.
        }

        return false
    }
}
