package com.github.shynixn.mctennis.impl

import com.github.shynixn.mccoroutine.bukkit.CoroutineTimings
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.github.shynixn.mctennis.MCTennisLanguage
import com.github.shynixn.mctennis.contract.PlaceHolderService
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
import com.github.shynixn.mctennis.event.GameStartEvent
import com.github.shynixn.mcutils.common.ChatColor
import com.github.shynixn.mcutils.common.chat.ChatMessageService
import com.github.shynixn.mcutils.common.command.CommandMeta
import com.github.shynixn.mcutils.common.command.CommandService
import com.github.shynixn.mcutils.common.toLocation
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import java.util.logging.Level

class TennisGameImpl(
    override val arena: TennisArena,
    private val tennisBallFactory: TennisBallFactory,
    private val chatMessageService: ChatMessageService,
    private val plugin: Plugin,
    private val commandService: CommandService,
    private val placeHolderService: PlaceHolderService
) : TennisGame {
    private var isDisposed = false

    /**
     * Tennis ball.
     */
    override var ball: TennisBall? = null

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
        plugin.launch(plugin.minecraftDispatcher + object : CoroutineTimings() {}) {
            while (!isDisposed) {
                if (gameState == GameState.RUNNING_PLAYING || gameState == GameState.RUNNING_SERVING) {
                    increasePunchPower()
                }

                delay(3.ticks)
            }
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
            executeCommandsWithPlaceHolder(listOf(player), arena.redTeamMeta.joinCommands)
            JoinResult.SUCCESS_RED
        } else {
            teamBluePlayers.add(player)
            player.teleport(arena.blueTeamMeta.lobbySpawnpoint.toLocation())
            executeCommandsWithPlaceHolder(listOf(player), arena.blueTeamMeta.joinCommands)
            JoinResult.SUCCESS_BLUE
        }

        // Store inventory once in game world. Lobby and game world can be different -> may cause problems with per world plugins.
        val playerData = PlayerData()
        cachedData[player] = playerData

        if (teamBluePlayers.size >= arena.minPlayersPerTeam && teamRedPlayers.size >= arena.minPlayersPerTeam && teamRedPlayers.size + teamBluePlayers.size > 0) {
            if (gameState == GameState.LOBBY_IDLE) {
                gameState = GameState.LOBBY_COUNTDOWN
                plugin.launch {
                    startGame()
                }
            }
        }

        return joinResult
    }

    /**
     * Leaves the given player.
     */
    override fun leave(player: Player): LeaveResult {
        if (!cachedData.containsKey(player)) {
            return LeaveResult.NOT_IN_MATCH
        }

        // Disable flying.
        player.isFlying = false
        player.allowFlight = false

        // Restore armor contents
        val playerData = cachedData[player]!!
        if (playerData.inventoryContents != null) {
            player.inventory.contents = playerData.inventoryContents!!.clone()
            player.inventory.setArmorContents(playerData.armorContents!!.clone())
            player.updateInventory()
        }

        // Then teleport
        val spawnpoint = arena.leaveSpawnpoint.toLocation()
        player.teleport(spawnpoint)

        cachedData.remove(player)
        if (teamRedPlayers.contains(player)) {
            teamRedPlayers.remove(player)
            executeCommandsWithPlaceHolder(listOf(player), arena.redTeamMeta.leaveCommands)
        }
        if (teamBluePlayers.contains(player)) {
            teamBluePlayers.remove(player)
            executeCommandsWithPlaceHolder(listOf(player), arena.blueTeamMeta.leaveCommands)
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

            player.inventory.contents = teamMeta.inventoryContents.map {
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

        val gameStartEvent = GameStartEvent(this)
        Bukkit.getPluginManager().callEvent(gameStartEvent)

        runGame()
    }

    /**
     * Runs the game.
     */
    private suspend fun runGame() {
        plugin.logger.log(Level.INFO, "Started game '" + arena.name + "'.")

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

            if (teamRedPlayers.size < arena.minPlayersPerTeam) {
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

            val allPlayers = getPlayers()
            executeCommandsWithPlaceHolder(allPlayers, arena.tickCommands)
            executeCommandsWithPlaceHolder(teamBluePlayers, arena.blueTeamMeta.tickCommands)
            executeCommandsWithPlaceHolder(teamRedPlayers, arena.redTeamMeta.tickCommands)

            if (ball != null && !ball!!.isDead) {
                executeCommandsWithPlaceHolder(allPlayers, arena.ballSettings.tickCommands)
            }

            delay(20.ticks)
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
                    playerData.currentPower += 0.5
                } else {
                    playerData.currentPower = 1.0
                    playerData.wasSneaking = true
                }
            } else {
                playerData.wasSneaking = false
            }

            if (playerData.currentPower > 5.0) {
                playerData.currentPower = 5.0
            }

            val builder = StringBuilder()

            for (i in 0 until playerData.currentPower.toInt()) {
                builder.append("█")
            }

            chatMessageService.sendActionBarMessage(player, ChatColor.GREEN.toString() + builder.toString())
        }
    }

    private suspend fun setBallForServingTeam(team: Team) {
        teleportPlayersToSpawnpoint()

        val teamMetaData = getTeamMetaFromTeam(team)
        // Spawnpoint 0 is always serving.
        val spawnpoint = teamMetaData.spawnpoints[0]
        val ballspawnpoint = spawnpoint.copy().addRelativeFront(2.0).addRelativeUp(0.5)

        ball = tennisBallFactory.createTennisBall(ballspawnpoint.toLocation(), arena.ballSettings, this)

        delay(500)
        sendTitleMessageToPlayers(MCTennisLanguage.readyTitle, MCTennisLanguage.readySubTitle)
        delay(1500)
        ball!!.setVelocity(Vector(0.0, 0.2, 0.0))
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
                executeCommandsWithPlaceHolder(teamRedPlayers, arena.redTeamMeta.drawCommands)
                executeCommandsWithPlaceHolder(teamBluePlayers, arena.blueTeamMeta.drawCommands)
            }
            Team.RED -> {
                sendTitleMessageToPlayers(MCTennisLanguage.winRedTitle, MCTennisLanguage.winRedSubTitle)
                executeCommandsWithPlaceHolder(teamRedPlayers, arena.redTeamMeta.winCommands)
                executeCommandsWithPlaceHolder(teamBluePlayers, arena.blueTeamMeta.looseCommands)
            }
            else -> {
                sendTitleMessageToPlayers(MCTennisLanguage.winBlueTitle, MCTennisLanguage.winBlueSubTitle)
                executeCommandsWithPlaceHolder(teamBluePlayers, arena.blueTeamMeta.winCommands)
                executeCommandsWithPlaceHolder(teamRedPlayers, arena.redTeamMeta.looseCommands)
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
     * Gets the collected player data.
     */
    override fun getPlayerData(player: Player): PlayerData? {
        if (cachedData.containsKey(player)) {
            return cachedData[player]!!
        }

        return null
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
    override fun getTeamFromPlayer(player: Player): Team? {
        if (teamBluePlayers.contains(player)) {
            return Team.BLUE
        }

        if (teamRedPlayers.contains(player)) {
            return Team.RED
        }

        return null
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

    private fun sendTitleMessageToPlayers(title: String, subTitle: String) {
        for (player in teamRedPlayers) {
            chatMessageService.sendTitleMessage(player, title, subTitle, 10, 70, 20)
        }
        for (player in teamBluePlayers) {
            chatMessageService.sendTitleMessage(player, title, subTitle, 10, 70, 20)
        }
    }

    private fun executeCommandsWithPlaceHolder(players: List<Player>, commands: List<CommandMeta>) {
        commandService.executeCommands(players, commands) { c, p ->
            placeHolderService.replacePlaceHolders(
                c,
                p,
                this
            )
        }
    }
}
