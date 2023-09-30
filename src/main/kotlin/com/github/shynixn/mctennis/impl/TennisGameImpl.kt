package com.github.shynixn.mctennis.impl

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.MCTennisLanguage
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
import com.github.shynixn.mcutils.common.command.CommandService
import com.github.shynixn.mcutils.common.toLocation
import kotlinx.coroutines.delay
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
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
            // TODO: servingTeam = Team.BLUE
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
        if (isFull()) {
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

        // Disable flying.
        player.isFlying = false
        player.allowFlight = false

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
            sendTitleMessageToPlayers(
                MCTennisLanguage.scoreRedTitle.format(getScoreText(), player.name),
                MCTennisLanguage.scoreRedSubTitle.format(getScoreText(), player.name)
            )
        } else {
            teamBlueScore++
            sendTitleMessageToPlayers(
                MCTennisLanguage.scoreBlueTitle.format(getScoreText(), player.name),
                MCTennisLanguage.scoreBlueSubTitle.format(getScoreText(), player.name)
            )
        }

        plugin.launch {
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
                        val configuration = YamlConfiguration()
                        configuration.loadFromString(it)
                        configuration.getItemStack("item")
                    } else {
                        null
                    }
                }.toTypedArray()
            player.inventory.setArmorContents(teamMeta.armorInventoryContents.map {
                if (it != null) {
                    val configuration = YamlConfiguration()
                    configuration.loadFromString(it)
                    configuration.getItemStack("item")
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
                sendMessageToPlayers(MCTennisLanguage.secondsRemaining.format(30))
            }

            if (remaining <= 10) {
                sendMessageToPlayers(MCTennisLanguage.secondsRemaining.format(remaining))
            }

            if (!arena.isEnabled) {
                sendMessageToPlayers(MCTennisLanguage.gameCancelledMessage)
                dispose()
                return
            }

            if (teamBluePlayers.size < arena.minPlayersPerTeam) {
                winGame(Team.RED)
                return
            }

            if (teamRedPlayers.size == arena.minPlayersPerTeam) {
                winGame(Team.BLUE)
                return
            }

            if (teamRedPlayers.size == 0 && teamBluePlayers.size == 0) {
                dispose()
                return
            }

            if (gameState == GameState.RUNNING_SERVING) {
                setBallForServingTeam(servingTeam)
            }

            increasePunchPower()
            delay(500L)
            increasePunchPower()
            delay(500L)
        }

        if (teamRedSetScore > teamBlueSetScore) {
            winGame(Team.RED)
        } else if (teamRedSetScore < teamBlueSetScore) {
            winGame(Team.BLUE)
        } else {
            if (teamRedScore > teamBlueScore) {
                winGame(Team.RED)
            } else if (teamRedScore < teamBlueScore) {
                winGame(Team.BLUE)
            } else {
                winGame(null)
            }
        }
    }

    private fun increasePunchPower() {
        for (player in getPlayers()) {
            val playerData = cachedData[player]

            if (playerData == null) {
                continue
            }

            if (player.isSneaking) {
                if (playerData.wasSneaking) {
                    playerData.currentPower += 1
                } else {
                    playerData.currentPower = 1
                    playerData.wasSneaking = true
                }
            } else {
                playerData.wasSneaking = false
            }

            val builder = StringBuilder()

            for (i in 0 until playerData.currentPower) {
                builder.append("█")
            }

            val textComponent = TextComponent(builder.toString())
            textComponent.color = ChatColor.GREEN
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent)
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
        sendTitleMessageToPlayers(MCTennisLanguage.readyTitle, MCTennisLanguage.readySubTitle)
        delay(1500)
        ball!!.setVelocity(Vector3d(x = 0.0, y = 0.2, z = 0.0))
        ball!!.allowActions = true
        gameState = GameState.RUNNING_PLAYING

        if (team == Team.RED && teamRedPlayers.size > 0) {
            lastHitPlayer = teamRedPlayers[0]
        } else if (team == Team.BLUE) {
            if (teamBluePlayers.size > 0) {
                lastHitPlayer = teamBluePlayers[0]
            } else if (teamRedPlayers.size > 0) {
                // Auto balance for testing.
                lastHitPlayer = teamRedPlayers[0]
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
                sendTitleMessageToPlayers(MCTennisLanguage.winSetRedTitle, MCTennisLanguage.winSetRedSubTitle)
            }
            else -> {
                teamBlueSetScore++
                sendTitleMessageToPlayers(MCTennisLanguage.winSetBlueTitle, MCTennisLanguage.winSetBlueSubTitle)
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
                sendTitleMessageToPlayers(MCTennisLanguage.winDrawTitle, MCTennisLanguage.winDrawSubTitle)
                commandService.executeCommands(teamRedPlayers, arena.redTeamMeta.drawCommands)
                commandService.executeCommands(teamBluePlayers, arena.blueTeamMeta.drawCommands)
            }
            Team.RED -> {
                sendTitleMessageToPlayers(MCTennisLanguage.winRedTitle, MCTennisLanguage.winRedSubTitle)
                commandService.executeCommands(teamRedPlayers, arena.redTeamMeta.winCommands)
                commandService.executeCommands(teamBluePlayers, arena.blueTeamMeta.looseCommands)
            }
            else -> {
                sendTitleMessageToPlayers(MCTennisLanguage.winBlueTitle, MCTennisLanguage.winBlueSubTitle)
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
    override fun dispose(sendEvent: Boolean) {
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

        if (sendEvent) {
            val gameEndEvent = GameEndEvent(this)
            Bukkit.getPluginManager().callEvent(gameEndEvent)
        }
    }

    /**
     * Gets if the game is full.
     */
    override fun isFull(): Boolean {
        return teamBluePlayers.size >= arena.maxPlayersPerTeam && teamRedPlayers.size >= arena.maxPlayersPerTeam
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

    /**
     * Gets the score text.
     */
    override fun getScoreText(): String {
        if (teamRedScore == 3 && teamBlueScore == 3) {
            return "Deuce"
        }

        if (teamRedScore >= 3 && teamBlueScore >= 3) {
            return if (servingTeam == Team.RED && teamRedScore > teamBlueScore) {
                "Ad-In"
            } else if (servingTeam == Team.BLUE && teamBlueScore > teamRedScore) {
                "Ad-In"
            } else {
                "Ad-Out"
            }
        }

        if (teamRedScore > 3 || teamBlueScore > 3) {
            return "Game"
        }

        val redScore = getScore(teamRedScore)
        val blueScore = getScore(teamBlueScore)
        return "$redScore - $blueScore"
    }

    private fun getScore(points: Int): String {
        return when (points) {
            0 -> {
                "0"
            }
            1 -> {
                "15"
            }
            2 -> {
                "30"
            }
            3 -> {
                "40"
            }
            else -> throw RuntimeException("Score $points cannot be converted!")
        }
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

    private fun sendTitleMessageToPlayers(title: String, subTitle: String? = null) {
        for (player in teamRedPlayers) {
            player.sendTitle(title, subTitle, 10, 70, 20)
        }
        for (player in teamBluePlayers) {
            player.sendTitle(title, subTitle, 10, 70, 20)
        }
    }
}
