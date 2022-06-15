package com.github.shynixn.mctennis.impl

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.MCTennisLanguage
import com.github.shynixn.mctennis.contract.TennisBall
import com.github.shynixn.mctennis.contract.TennisBallFactory
import com.github.shynixn.mctennis.contract.TennisGame
import com.github.shynixn.mctennis.entity.CommandMeta
import com.github.shynixn.mctennis.entity.PlayerData
import com.github.shynixn.mctennis.entity.TeamMetadata
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.*
import com.github.shynixn.mctennis.event.GameEndEvent
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.toLocation
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*

class TennisGameImpl(override val arena: TennisArena, val tennisBallFactory: TennisBallFactory) : TennisGame {
    private var redTeamCounter = 0
    private var blueTeamCounter = 0
    private var servingTeam = Team.RED
    private var isDisposed = false

    /**
     * Tennis ball.
     */
    private var ball: TennisBall? = null

    /**
     * Dependency.
     */
    lateinit var plugin: Plugin

    /**
     * All Players.
     */
    val cachedData = HashMap<Player, PlayerData>()

    /**
     * Amount of bounces.
     */
    override var bounceCounter: Int = 0

    /**
     * Player who was the last one to hit the ball.
     */
    override var lastHitPlayer: Player? = null

    /**
     * Holds the gamestate.
     */
    override var gameState: GameState = GameState.LOBBY_IDLE

    /**
     * Team players.
     */
    override val teamRedPlayers = ArrayList<Player>()

    /**
     * Team players.
     */
    override val teamBluePlayers = ArrayList<Player>()


    /**
     * Joins the given player.
     */
    override fun join(player: Player, team: Team?): JoinResult {
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

        if (gameState != GameState.LOBBY_IDLE && gameState != GameState.LOBBY_COUNTDOWN) {
            return JoinResult.GAME_ALREADY_RUNNING
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
            if (gameState == GameState.LOBBY_IDLE) {
                gameState = GameState.LOBBY_COUNTDOWN
                plugin.launch {
                    startGame()
                }
            }
        }

        executeCommand(arena.joinCommands, listOf(player))
        return joinResult
    }

    /**
     * Leaves the given player.
     */
    override fun leave(player: Player): LeaveResult {
        if (!cachedData.containsKey(player)) {
            return LeaveResult.NOT_IN_MATCH
        }

        executeCommand(arena.leaveCommands, listOf(player))

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
     * Lets the given player score a point for the given team.
     * The player does not have to be in this team.
     */
    override fun scorePoint(player: Player, team: Team) {
        ball!!.allowActions = false

        if (team == Team.RED) {
            redTeamCounter++
        } else {
            blueTeamCounter++
        }

        plugin.launch {
            sendMessageToPlayers(MCTennisLanguage.playerScoredMessage.format(player.name, team.name))
            delay(3000)
            teleportPlayersToSpawnpoint()
            ball?.remove()
            ball = null
            gameState = GameState.RUNNING_SERVING

            if (redTeamCounter > 4 && redTeamCounter - blueTeamCounter >= 2) {
                winTeam(Team.RED)
            } else if (blueTeamCounter > 4 && blueTeamCounter - redTeamCounter >= 2) {
                winTeam(Team.BLUE)
            }
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
                dispose()
                sendMessageToPlayers(MCTennisLanguage.gameStartCancelledMessage)
                return
            }

            if (teamBluePlayers.size < arena.minPlayersPerTeam || teamRedPlayers.size < arena.minPlayersPerTeam) {
                sendMessageToPlayers(MCTennisLanguage.notEnoughPlayersMessage)
                gameState = GameState.LOBBY_IDLE
                return
            }
        }

        gameState = GameState.RUNNING_SERVING

        // Move to arena.
        teleportPlayersToSpawnpoint()

        // Store cache data.
        delay(250)
        for (player in cachedData.keys) {
            val playerData = cachedData[player]!!
            playerData.armorContents = player.inventory.armorContents.clone()
            playerData.inventoryContents = player.inventory.contents.clone()

            val teamMeta = if (teamBluePlayers.contains(player)) {
                arena.blueTeamMeta
            } else {
                arena.redTeamMeta
            }

            player.inventory.contents =
                teamMeta.inventoryContents.map {
                    if (it != null) {
                        ItemStack.deserialize(it)
                    } else {
                        null
                    }
                }.toTypedArray()
            player.inventory.setArmorContents(teamMeta.armorInventoryContents.map {
                if (it != null) {
                    ItemStack.deserialize(it)
                } else {
                    null
                }
            }.toTypedArray())
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
                //  TODO: winTeam(Team.RED)
                //  return
            }

            if (teamRedPlayers.size == 0) {
                //   TODO winTeam(Team.BLUE)
                //  return
            }

            if (gameState == GameState.RUNNING_SERVING) {
                setBallForServingTeam(servingTeam)
            }

            delay(1000L)
        }
    }

    private suspend fun setBallForServingTeam(team: Team) {
        teleportPlayersToSpawnpoint()

        val teamMetaData = getTeamMetaFromTeam(team)
        // Spawnpoint 0 is always serving.
        val spawnpoint = teamMetaData.spawnpoints[0]
        val ballspawnpoint = spawnpoint.clone().addRelativeFront(2.0).addRelativeUp(0.5)

        ball = tennisBallFactory.createTennisBall(ballspawnpoint.toLocation(), this, arena.ballSettings)

        delay(500)
        sendTitleMessageToPlayers(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Ready?")
        delay(1500)
        ball!!.setVelocity(Vector3d(x = 0.0, y = 0.2, z = 0.0))
        ball!!.allowActions = true
        gameState = GameState.RUNNING_PLAYING

        if (team == Team.RED) {
            lastHitPlayer = teamRedPlayers[0]
        } else if (team == Team.BLUE) {
            lastHitPlayer = teamBluePlayers[0]
        }
    }

    /**
     * Gets called when a team has won the game.
     */
    private suspend fun winTeam(team: Team? = null) {
        gameState = GameState.ENDING
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
        if (isDisposed) {
            return
        }

        for (player in cachedData.keys.toTypedArray()) {
            leave(player)
        }

        teamRedPlayers.clear()
        teamBluePlayers.clear()
        cachedData.clear()
        isDisposed = true
        ball?.remove()

        val gameEndEvent = GameEndEvent(this)
        Bukkit.getPluginManager().callEvent(gameEndEvent)
    }

    /**
     * Gets all players.
     */
    override fun getPlayers(): List<Player> {
        val players = ArrayList<Player>()
        players.addAll(teamBluePlayers)
        players.addAll(teamRedPlayers)
        return players
    }

    private fun teleportPlayersToSpawnpoint() {
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

    private fun executeCommand(commandMetas: List<CommandMeta>, players: List<Player>) {
        for (commandMeta in commandMetas) {
            when (commandMeta.type) {
                CommandType.SERVER -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandMeta.command)
                }
                CommandType.SERVER_PER_PLAYER -> {
                    for (player in players) {
                        Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            commandMeta.command.replace("%player_name%", player.name)
                        )
                    }
                }
                CommandType.PER_PLAYER -> {
                    for (player in players) {
                        Bukkit.dispatchCommand(player, commandMeta.command.replace("%player_name%", player.name))
                    }
                }
            }
        }
    }

    /**
     * Sends a message to all players in game.
     */
    override fun sendMessageToPlayers(message: String) {
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
     * Gets the team from a player.
     * Throws an exception if the player isn't in this game.
     */
    override fun getTeamFromPlayer(player: Player): Team {
        if (teamBluePlayers.contains(player)) {
            return Team.BLUE
        }

        if (teamRedPlayers.contains(player)) {
            return Team.RED
        }

        throw RuntimeException("Team of ${player.name} not found!")
    }

    private fun getTeamMetaFromTeam(team: Team): TeamMetadata {
        if (team == Team.RED) {
            return arena.redTeamMeta
        } else if (team == Team.BLUE) {
            return arena.blueTeamMeta
        }

        throw RuntimeException("Team $team not found!")
    }

    private fun sendTitleMessageToPlayers(title: String? = null, subTitle: String? = null) {
        for (player in teamRedPlayers) {
            player.sendTitle(title, subTitle, 10, 70, 20)
        }
        for (player in teamBluePlayers) {
            player.sendTitle(title, subTitle, 10, 70, 20)
        }
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
