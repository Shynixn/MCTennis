package com.github.shynixn.mctennis.impl.listener

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.enumeration.GameState
import com.github.shynixn.mcutils.common.SoundService
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.toVector
import com.github.shynixn.mcutils.common.toVector3d
import com.google.inject.Inject
import kotlinx.coroutines.delay
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.plugin.Plugin

class DoubleJumpListener @Inject constructor(
    private val gameService: GameService,
    private val plugin: Plugin,
    private val soundService: SoundService
) :
    Listener {
    private val doubleJumpPlayers = HashSet<Player>()
    private val movingDirection = HashMap<Player, Vector3d>()

    /**
     * Cleanup.
     */
    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        if (movingDirection.containsKey(event.player)) {
            movingDirection.remove(event.player)
        }
    }

    /**
     * Gets called when a player moves. Allows the executing player to start flying
     * for double jump calculation if the action is enabled and the player is in a game.
     */
    @EventHandler
    fun onPlayerMoveEvent(event: PlayerMoveEvent) {
        if (!event.player.isOnGround) {
            return
        }

        val game = gameService.getByPlayer(event.player) ?: return

        if (game.gameState != GameState.RUNNING_PLAYING) {
            return
        }

        if (game.arena.doubleJumpMeta.isDoubleJumpEnabled) {
            event.player.allowFlight = true

            if (event.to != null) {
                movingDirection[event.player] = event.to!!.clone().subtract(event.from).toVector3d().normalize()
            }
        }
    }

    /**
     * Gets called when a player doule presses the space key to start flying. Performs a double
     * jump action if the player is in a game and double jump is available.
     */
    @EventHandler
    fun onPlayerToggleFlightEvent(event: PlayerToggleFlightEvent) {
        if (event.player.gameMode == GameMode.CREATIVE || event.player.gameMode == GameMode.SPECTATOR) {
            return
        }

        val game = gameService.getByPlayer(event.player) ?: return

        if (game.gameState != GameState.RUNNING_PLAYING) {
            return
        }

        if (!game.arena.doubleJumpMeta.isDoubleJumpEnabled) {
            return
        }

        val doubleJumpMeta = game.arena.doubleJumpMeta
        val player = event.player
        player.allowFlight = false
        player.isFlying = false
        event.isCancelled = true

        if (doubleJumpPlayers.contains(player)) {
            return
        }

        if (!movingDirection.containsKey(player)) {
            return
        }

        val directionVelocity = movingDirection[player]!!
        val velocity = directionVelocity.clone()
            .normalize()
            .multiply(doubleJumpMeta.doubleJumpHorizontalStrength)
        velocity.y = doubleJumpMeta.doubleJumpVerticalStrength

        try {
            player.velocity = velocity.toVector()
        } catch (e: IllegalArgumentException) {
            // Ignore finite exception.
        }

        soundService.playSound(player.location, player.world.players, doubleJumpMeta.doubleJumpSound)
        //  particleService.playParticle(player.location, meta.particleEffect, player.world.players)

        doubleJumpPlayers.add(player)
        plugin.launch {
            delay(1000)
            doubleJumpPlayers.remove(player)
        }
    }
}
