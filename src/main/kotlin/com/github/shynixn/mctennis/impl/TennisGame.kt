package com.github.shynixn.mctennis.impl

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.MCTennisLanguage
import com.github.shynixn.mctennis.entity.PlayerData
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.GameState
import com.github.shynixn.mctennis.enumeration.JoinResult
import com.github.shynixn.mctennis.enumeration.LeaveResult
import com.github.shynixn.mctennis.enumeration.Team
import com.github.shynixn.mcutils.ball.api.Ball
import com.github.shynixn.mcutils.common.toLocation
import kotlinx.coroutines.delay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*

class TennisGame(val arena: TennisArena) {
    companion object {
        private val random = Random()
    }

    private var redTeamCounter = 0
    private var blueTeamCounter = 0
    private var servingTeam = Team.RED

    init {
        if (random.nextInt(100) < 50) {
            servingTeam = Team.BLUE
        }
    }

    /**
     * Dependency.
     */
    lateinit var plugin: Plugin

    /**
     * Gets if this game is no longer useable.
     */
    var isDisposed = false

    /**
     * Holds the gamestate.
     */
    var gameState: GameState = GameState.LOBBY

    /**
     * Tennis ball.
     */
    var ball: Ball? = null

    /**
     * Player who was the last one to hit the ball.
     */
    var lastHitPlayer: Player? = null

    /**
     * The target field which requires bouncing before the next player shoots back.
     * This is useful for cases, where the ball does not hit far enough (hitField != targetField) -> error.
     * The ball comes up more than once in the target field. Does not come up one in the field.
     */
    var targetField: Team = Team.BLUE

    /**
     * Amount of bounces of the ball in the target fields.
     */
    var targetFieldCounter = 0

    /**
     * Team players.
     */
    val teamRedPlayers = ArrayList<Player>()

    /**
     * Team players.
     */
    val teamBluePlayers = ArrayList<Player>()

    /**
     * All Players.
     */
    val cachedData = HashMap<Player, PlayerData>()

    /**
     * Joins the given player.
     */
    fun join(player: Player, team: Team? = null): JoinResult {
        // Make sure a team is selected.
        var targetTeam = Team.RED

        if (team == null) {
            if (teamBluePlayers.size < teamRedPlayers.size) {
                targetTeam = Team.BLUE
            }
        } else {
            targetTeam = team
        }

        // Check if teams are full.
        if (teamBluePlayers.size >= arena.maxPlayersPerTeam && teamRedPlayers.size >= arena.maxPlayersPerTeam) {
            return JoinResult.GAME_FULL
        }

        if (targetTeam == Team.BLUE && teamBluePlayers.size >= arena.maxPlayersPerTeam) {
            return JoinResult.TEAM_FULL
        }

        if (targetTeam == Team.RED && teamRedPlayers.size >= arena.maxPlayersPerTeam) {
            return JoinResult.TEAM_FULL
        }

        // Join team
        val joinResult = if (targetTeam == Team.RED) {
            teamRedPlayers.add(player)
            player.teleport(arena.redTeamMeta.lobbySpawnpoint.toLocation())
            JoinResult.SUCCESS_RED
        } else {
            teamBluePlayers.add(player)
            player.teleport(arena.blueTeamMeta.lobbySpawnpoint.toLocation())
            JoinResult.SUCCESS_BLUE
        }

        // Store inventory once in game world. Lobby and game world can be different -> may cause problems with per world plugins.
        val playerData = PlayerData()
        cachedData[player] = playerData

        if (teamBluePlayers.size >= arena.minPlayersPerTeam && teamRedPlayers.size >= arena.minPlayersPerTeam) {
            plugin.launch {
                startGame()
            }
        }

        return joinResult
    }

    /**
     * Leaves the given player.
     */
    fun leave(player: Player): LeaveResult {
        if (!cachedData.containsKey(player)) {
            return LeaveResult.NOT_IN_MATCH
        }

        // Restore armor contents
        val playerData = cachedData[player]!!
        if (playerData.inventoryContents != null) {
            player.inventory.contents =
                playerData.inventoryContents!!.clone()
            player.inventory.setArmorContents(playerData.armorContents!!.clone())
            player.updateInventory()
        }

        // Then teleport
        val spawnpoint = arena.leaveSpawnpoint.toLocation()
        player.teleport(spawnpoint)

        cachedData.remove(player)
        if (teamRedPlayers.contains(player)) {
            teamRedPlayers.remove(player)
        }
        if (teamBluePlayers.contains(player)) {
            teamBluePlayers.remove(player)
        }

        return LeaveResult.SUCCESS
    }

    /**
     * Lets the given player score for the given team.
     */
    suspend fun score(player: Player, team: Team) {
        if (team == Team.RED) {
            redTeamCounter++
        } else {
            blueTeamCounter++
        }

        sendMessageToPlayers(MCTennisLanguage.playerScoredMessage.format(player.name, team.name))
        delay(3000)

        for (i in 0 until teamRedPlayers.size) {
            val teamPlayer = teamRedPlayers[i]
            val spawnpoint = arena.redTeamMeta.spawnpoints[i]
            teamPlayer.teleport(spawnpoint.toLocation())
        }
        for (i in 0 until teamBluePlayers.size) {
            val teamPlayer = teamBluePlayers[i]
            val spawnpoint = arena.blueTeamMeta.spawnpoints[i]
            teamPlayer.teleport(spawnpoint.toLocation())
        }
    }

    /**
     * Starts the game.
     */
    private suspend fun startGame() {
        // Wait in lobby.
        for (i in 0 until arena.timeToStart) {
            val remaining = arena.timeToStart - i
            sendMessageToPlayers(MCTennisLanguage.gameStartingMessage.format(remaining))
            delay(1000L)

            if (!arena.isEnabled) {
                sendMessageToPlayers(MCTennisLanguage.gameStartCancelledMessage)
                dispose()
                return
            }

            if (teamBluePlayers.size <= arena.minPlayersPerTeam || teamRedPlayers.size <= arena.minPlayersPerTeam) {
                sendMessageToPlayers("Not enough players! Game start was cancelled.")
                dispose()
                return
            }
        }

        // Move to arena.
        for (i in 0 until teamRedPlayers.size) {
            val player = teamRedPlayers[i]
            val spawnpoint = arena.redTeamMeta.spawnpoints[i]
            player.teleport(spawnpoint.toLocation())
        }
        for (i in 0 until teamBluePlayers.size) {
            val player = teamBluePlayers[i]
            val spawnpoint = arena.blueTeamMeta.spawnpoints[i]
            player.teleport(spawnpoint.toLocation())
        }

        // Store cache data.
        delay(250)
        for (player in cachedData.keys) {
            val cacheData = cachedData[player]!!
            cacheData.armorContents = player.inventory.armorContents.clone()
            cacheData.inventoryContents = player.inventory.contents.clone()

            val teamMeta = if (teamBluePlayers.contains(player)) {
                arena.blueTeamMeta
            } else {
                arena.redTeamMeta
            }

            player.inventory.contents =
                teamMeta.inventoryContents.clone()
            player.inventory.setArmorContents(teamMeta.armorInventoryContents.clone())
        }

        runGame()
    }

    /**
     * Runs the game.
     */
    private suspend fun runGame() {
        for (i in 0 until arena.gameTime) {
            val remaining = arena.gameTime - i

            if (remaining == 30) {
                sendMessageToPlayers("30 seconds remaining.")
            }

            if (remaining <= 10) {
                sendMessageToPlayers("$remaining second(s) remaining.")
            }

            if (!arena.isEnabled) {
                sendMessageToPlayers("Game was cancelled!")
                dispose()
                return
            }

            if (teamBluePlayers.size == 0) {
                winTeam(Team.RED)
                return
            }

            if (teamRedPlayers.size == 0) {
                winTeam(Team.BLUE)
                return
            }

            delay(1000L)
        }
    }

    /**
     * Gets called when a team has won the game.
     */
    private suspend fun winTeam(team: Team? = null) {
        when (team) {
            null -> {
                sendMessageToPlayers("Game has ended in a draw.")
            }
            Team.RED -> {
                sendMessageToPlayers("Team red has won the match.")
            }
            else -> {
                sendMessageToPlayers("Team blue has won the match.")
            }
        }

        delay(5000)
        dispose()
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * `try`-with-resources statement.
     * However, implementers of this interface are strongly encouraged
     * to make their `close` methods idempotent.

     */
    fun dispose() {
        for (player in cachedData.keys.toTypedArray()) {
            leave(player)
        }

        teamRedPlayers.clear()
        teamBluePlayers.clear()
        cachedData.clear()
        isDisposed = true
    }

    /**
     * Gets all players.
     */
    fun getPlayers(): List<Player> {
        val players = ArrayList<Player>()
        players.addAll(teamBluePlayers)
        players.addAll(teamRedPlayers)
        return players
    }

    private fun sendMessageToPlayers(message: String) {
        if (message.isEmpty()) {
            return
        }

        for (player in teamRedPlayers) {
            player.sendMessage(message)
        }
        for (player in teamBluePlayers) {
            player.sendMessage(message)
        }
    }

    /**
     * Gets the tennis score for correct cases.
     */
    private fun getFullScoreText(): String {
        if (redTeamCounter == 3 && blueTeamCounter == 3) {
            return "Deuce"
        }
        if (redTeamCounter >= 3 && blueTeamCounter >= 3) {
            return if (servingTeam == Team.RED && redTeamCounter > blueTeamCounter) {
                "Ad-In"
            } else if (servingTeam == Team.BLUE && blueTeamCounter > redTeamCounter) {
                "Ad-In"
            } else {
                "Ad-Out"
            }
        }

        val redScore = getScore(redTeamCounter)
        val blueScore = getScore(blueTeamCounter)
        return "$redScore - $blueScore"
    }

    private fun getScore(points: Int): String {
        when (points) {
            0 -> {
                return "0"
            }
            1 -> {
                return "15"
            }
            2 -> {
                return "30"
            }
            3 -> {
                return "40"
            }
            else -> throw RuntimeException("Score $redTeamCounter $blueTeamCounter cannot be converted!")
        }
    }
}
