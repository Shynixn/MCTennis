package com.github.shynixn.mctennis.contract

import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.Team
import org.bukkit.entity.Player

interface TennisGame {
    /**
     * Amount of bounces.
     */
    var bounceCounter: Int

    /**
     * Player who was the last one to hit the ball.
     */
    var lastHitPlayer: Player?

    /**
     * Gets the arena.
     */
    val arena: TennisArena

    /**
     * Gets the team from a player.
     * Throws an exception if the player isn't in this game.
     */
    fun getTeamFromPlayer(player: Player): Team

    /**
     * Lets the given player score a point for the given team.
     * The player does not have to be in this team.
     */
    fun scorePoint(player: Player, team: Team)

    /**
     * Sends a message to all players in game.
     */
    fun sendMessageToPlayers(message: String)
}
