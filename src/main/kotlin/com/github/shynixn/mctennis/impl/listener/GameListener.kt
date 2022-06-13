package com.github.shynixn.mctennis.impl.listener

import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.enumeration.GameState
import com.google.inject.Inject
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

class GameListener @Inject constructor(private val gameService: GameService) : Listener {
    /**
     * Handles leaving state.
     */
    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        val game = gameService.getByPlayer(event.player) ?: return
        game.leave(event.player)
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
