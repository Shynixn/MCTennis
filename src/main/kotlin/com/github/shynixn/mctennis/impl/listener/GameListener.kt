package com.github.shynixn.mctennis.impl.listener

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.enumeration.GameState
import com.github.shynixn.mctennis.event.GameEndEvent
import com.google.inject.Inject
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

class GameListener @Inject constructor(private val gameService: GameService, private val plugin: Plugin) : Listener {
    /**
     * Handles leaving state.
     */
    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        val game = gameService.getByPlayer(event.player) ?: return
        game.leave(event.player)
    }

    /**
     * Handles game end event.
     */
    @EventHandler
    fun onGameEndEvent(event: GameEndEvent) {
        val game = event.game
        plugin.launch {
            gameService.reload(game.arena)
        }
    }

    /**
     * Cancels player move while serving.
     */
    @EventHandler
    fun onCancelPlayerMoveWhileServing(event: PlayerMoveEvent) {
        val player = event.player
        val game = gameService.getByPlayer(event.player) ?: return

        if (game.gameState == GameState.RUNNING_SERVING) {
            val newLocation = event.from
            newLocation.yaw = event.to!!.yaw
            newLocation.pitch = event.to!!.pitch
            player.teleport(newLocation)
        }
    }
}
