package com.github.shynixn.mctennis.impl

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.MCTennisLanguage
import com.github.shynixn.mctennis.contract.CommandService
import com.github.shynixn.mctennis.contract.TennisBall
import com.github.shynixn.mctennis.contract.TennisBallFactory
import com.github.shynixn.mctennis.contract.TennisGame
import com.github.shynixn.mctennis.entity.PlayerData
import com.github.shynixn.mctennis.entity.TeamMetadata
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.GameState
import com.github.shynixn.mctennis.enumeration.JoinResult
import com.github.shynixn.mctennis.enumeration.LeaveResult
import com.github.shynixn.mctennis.enumeration.Team
import com.github.shynixn.mctennis.event.GameEndEvent
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.toLocation
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

class TennisGameImpl(override val arena: TennisArena, val tennisBallFactory: TennisBallFactory) : TennisGame {
    companion object {
        private val random = java.util.Random()
    }

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
     * Dependency.
     */
    lateinit var commandService: CommandService

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
     * Score.
     */
    override var teamRedScore: Int = 0

    /**
     * Score.
     */
    override var teamBlueScore: Int = 0

    /**
     * Amount of won sets.
     */
    override var teamRedSetScore: Int = 0

    /**
     * Amount of won sets.
     */
    override var teamBlueSetScore: Int = 0

    /**
     * Gets the team who is serving.
     */
    override var servingTeam: Team = Team.RED

    init {
        if (random.nextInt(100) < 50) {
            servingTeam = Team.BLUE
        }
    }

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

        commandService.executeCommands(listOf(player), arena.joinCommands)
        return joinResult
    }

    /**
     * Leaves the given player.
     */
    override fun leave(player: Player): LeaveResult {
        if (!cachedData.containsKey(player)) {
            return LeaveResult.NOT_IN_MATCH
        }

        commandService.executeCommands(listOf(player), arena.leaveCommands)

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
        bounceCounter = 0

        if (team == Team.RED) {
            teamRedScore++
        } else {
            teamBlueScore++
        }

        plugin.launch {
            sendMessageToPlayers(MCTennisLanguage.playerScoredMessage.format(player.name, team.name))
            delay(3000)
            teleportPlayersToSpawnpoint()
            ball?.remove()
            ball = null
            gameState = GameState.RUNNING_SERVING

            if (teamRedScore > 3 && teamRedScore - teamBlueScore >= 2) {
                winSet(Team.RED)
            } else if (teamBlueScore > 3 && teamBlueScore - teamRedScore >= 2) {
                winSet(Team.BLUE)
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
            lastHitPlayer = if (teamBluePlayers.size == 0) {
                teamRedPlayers[0] // Auto balance when testing.
            } else {
                teamBluePlayers[0]
            }
        }
    }

    /**
     * Gets called when a team has won the set.
     */
    private suspend fun winSet(team: Team) {
        gameState = GameState.ENDING
        when (team) {
            Team.RED -> {
                teamRedSetScore++
                sendMessageToPlayers("Team red has won this set.")
            }
            else -> {
                teamBlueSetScore++
                sendMessageToPlayers("Team blue has won this set.")
            }
        }

        delay(5000)

        if (team == Team.RED && teamRedSetScore >= this.arena.setsToWin) {
            winGame(team)
            return
        } else if (team == Team.BLUE && teamBlueSetScore >= this.arena.setsToWin) {
            winGame(team)
            return
        }

        teamRedScore = 0
        teamBlueScore = 0
        sendMessageToPlayers("Switching service.")
        servingTeam = if (servingTeam == Team.RED) {
            Team.BLUE
        } else {
            Team.RED
        }
        gameState = GameState.RUNNING_SERVING
    }

    /**
     * Gets called when a team has won the game.
     */
    private suspend fun winGame(team: Team? = null) {
        when (team) {
            null -> {
                sendMessageToPlayers("Game has ended in a draw.")
                commandService.executeCommands(teamRedPlayers, arena.redTeamMeta.drawCommands)
                commandService.executeCommands(teamBluePlayers, arena.blueTeamMeta.drawCommands)
            }
            Team.RED -> {
                sendMessageToPlayers("Team red has won the match.")
                commandService.executeCommands(teamRedPlayers, arena.redTeamMeta.winCommands)
                commandService.executeCommands(teamBluePlayers, arena.blueTeamMeta.looseCommands)
            }
            else -> {
                sendMessageToPlayers("Team blue has won the match.")
                commandService.executeCommands(teamBluePlayers, arena.blueTeamMeta.winCommands)
                commandService.executeCommands(teamRedPlayers, arena.redTeamMeta.looseCommands)
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
}
