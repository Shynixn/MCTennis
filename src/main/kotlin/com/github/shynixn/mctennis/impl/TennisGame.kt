package com.github.shynixn.mctennis.impl

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.MCTennisPlugin
import com.github.shynixn.mctennis.entity.PlayerData
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.JoinResult
import com.github.shynixn.mctennis.enumeration.Team
import com.github.shynixn.mcutils.toLocation
import kotlinx.coroutines.delay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

class TennisGame(private val arena: TennisArena) {
    private val teamRedPlayers = ArrayList<Player>()
    private val teamBluePlayers = ArrayList<Player>()
    private val cachedData = HashMap<Player, PlayerData>()

    /**
     * Dependency.
     */
    lateinit var plugin: Plugin

    /**
     * Gets if this game is no longer useable.
     */
    var isDisposed = false

    /**
     * Lets the given player
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
            player.teleport(arena.redTeamLobbySpawnpoint.toLocation())
            JoinResult.SUCCESS_RED
        } else {
            teamBluePlayers.add(player)
            player.teleport(arena.blueTeamLobbySpawnpoint.toLocation())
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
     * Starts the game.
     */
    private suspend fun startGame() {
        // Wait in lobby.
        for (i in 0 until arena.timeToStart) {
            val remaining = arena.timeToStart - i
            sendMessageToPlayers(MCTennisPlugin.prefix + "Game is starting in $remaining seconds.")
            delay(1000L)

            if (!arena.isEnabled) {
                sendMessageToPlayers(MCTennisPlugin.prefix + "Game start was cancelled!")
                dispose()
                return
            }

            if (teamBluePlayers.size <= arena.minPlayersPerTeam || teamRedPlayers.size <= arena.minPlayersPerTeam) {
                sendMessageToPlayers(MCTennisPlugin.prefix + "Not enough players! Game start was cancelled.")
                dispose()
                return
            }
        }

        // Move to arena.
        for (i in 0 until teamRedPlayers.size) {
            val player = teamRedPlayers[i]
            val spawnpoint = arena.redPlayerSpawnpoints[i]
            player.teleport(spawnpoint.toLocation())
        }
        for (i in 0 until teamBluePlayers.size) {
            val player = teamBluePlayers[i]
            val spawnpoint = arena.bluePlayerSpawnpoints[i]
            player.teleport(spawnpoint.toLocation())
        }

        // Store cache data.
        delay(250)
        for (player in cachedData.keys) {
            val cacheData = cachedData[player]!!
            cacheData.armorContents = player.inventory.armorContents.clone() as Array<Any?>
            cacheData.inventoryContents = player.inventory.contents.clone() as Array<Any?>
            player.inventory.contents =
                arena.redTeamInventoryContents.clone().map { d -> d as ItemStack? }.toTypedArray()
            player.inventory.setArmorContents(arena.redTeamInventoryContents.clone().map { d -> d as ItemStack? }
                .toTypedArray())
        }


    }


    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * `try`-with-resources statement.
     * However, implementers of this interface are strongly encouraged
     * to make their `close` methods idempotent.

     */
    suspend fun dispose() {
        // Restore armor contents
        for (player in cachedData.keys) {
            val playerData = cachedData[player]!!

            if (playerData.inventoryContents != null) {
                player.inventory.contents =
                    playerData.inventoryContents!!.clone().map { d -> d as ItemStack? }.toTypedArray()
                player.inventory.setArmorContents(playerData.armorContents!!.clone().map { d -> d as ItemStack? }
                    .toTypedArray())
                player.updateInventory()
            }
        }

        // Then teleport
        delay(1000L)
        val spawnpoint = arena.leaveSpawnpoint.toLocation()
        for (player in cachedData.keys) {
            player.teleport(spawnpoint)
        }

        teamRedPlayers.clear()
        teamBluePlayers.clear()
        cachedData.clear()
        isDisposed = true
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
}
