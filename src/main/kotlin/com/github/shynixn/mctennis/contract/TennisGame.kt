package com.github.shynixn.mctennis.contract

import com.github.shynixn.mctennis.entity.PlayerData
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.GameState
import com.github.shynixn.mctennis.enumeration.JoinResult
import com.github.shynixn.mctennis.enumeration.LeaveResult
import com.github.shynixn.mctennis.enumeration.Team
import org.bukkit.entity.Player

interface TennisGame {
    /**
     * Amount of bounces the ball has performed.
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
     * Gets the state of the game.
     */
    val gameState: GameState

    /**
     * Gets all players of team red.
     */
    val teamRedPlayers: List<Player>

    /**
     * Gets all players of team blue.
     */
    val teamBluePlayers: List<Player>

    /**
     * Score.
     */
    val teamRedScore: Int

    /**
     * Score.
     */
    val teamBlueScore: Int

    /**
     * Amount of won sets.
     */
    val teamRedSetScore: Int

    /**
     * Amount of won sets.
     */
    val teamBlueSetScore: Int

    /**
     * Gets the team who is serving.
     */
    val servingTeam: Team

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

    /**
     * Gets all players.
     */
    fun getPlayers(): List<Player>

    /**
     * Gets the collected player data.
     */
    fun getPlayerData(player: Player): PlayerData?

    /**
     * Gets the tennis score.
     */
    fun getScoreText(): String

    /**
     * Joins the given player.
     */
    fun join(player: Player, team: Team? = null): JoinResult

    /**
     * Leaves the given player.
     */
    fun leave(player: Player): LeaveResult

    /**
     * Cancels the game.
     */
    fun dispose(sendEvent: Boolean = true)

    /**
     * Gets if the game is full.
     */
    fun isFull(): Boolean
}
