package com.github.shynixn.mctennis.impl.listener

import com.github.shynixn.mctennis.contract.GameService
import com.google.inject.Inject
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class GameListener @Inject constructor(private val gameService: GameService) : Listener {
    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        val game = gameService.getByPlayer(event.player) ?: return
        game.leave(event.player)
    }
}
