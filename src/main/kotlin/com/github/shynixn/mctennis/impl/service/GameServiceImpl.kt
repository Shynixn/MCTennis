package com.github.shynixn.mctennis.impl.service

import com.github.shynixn.mctennis.MCTennisDependencyInjectionModule
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.contract.Language
import com.github.shynixn.mctennis.contract.PlaceHolderService
import com.github.shynixn.mctennis.contract.TennisBallFactory
import com.github.shynixn.mctennis.entity.TeamMetadata
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.impl.exception.TennisGameException
import com.github.shynixn.mctennis.impl.TennisGameImpl
import com.github.shynixn.mcutils.common.chat.ChatMessageService
import com.github.shynixn.mcutils.common.command.CommandService
import com.github.shynixn.mcutils.common.repository.Repository
import com.github.shynixn.mcutils.sign.SignService
import com.google.inject.Inject
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.logging.Level
import kotlin.math.max
import kotlin.math.min

class GameServiceImpl @Inject constructor(
    private val arenaRepository: Repository<TennisArena>,
    private val tennisBallFactory: TennisBallFactory,
    private val plugin: Plugin,
    private val commandService: CommandService,
    private val signService: SignService,
    private val chatMessageService: ChatMessageService,
    private val placeHolderService: PlaceHolderService,
    private val language: Language
) : GameService {
    private val games = ArrayList<TennisGameImpl>()

    /**
     * Reloads all games.
     */
    override suspend fun reloadAll() {
        close()

        val arenas = arenaRepository.getAll()

        for (arena in arenas) {
            reload(arena)
        }
    }

    /**
     * Reloads the specific game.
     */
    override suspend fun reload(arena: TennisArena) {
        // A game with the same arena name is currently running. Stop it and reboot it.
        val existingGame = getByName(arena.name)
        if (existingGame != null) {
            existingGame.dispose(false)
            games.remove(existingGame)
            plugin.logger.log(Level.INFO, "Stopped game '" + arena.name + "'.")
        }

        // Enable signs, if they are already added, the call does nothing.
        for (sign in arena.signs) {
            sign.tag = arena.name
            signService.addSign(sign)
        }

        if (arena.isEnabled) {
            validateGame(arena)

            if (!MCTennisDependencyInjectionModule.areLegacyVersionsIncluded && games.size >= 1) {
                plugin.logger.info("Failed to boot game ${arena.name}. This version of MCTennis only supports one game. See release notes for details.")
                return
            }

            val tennisGameImpl = TennisGameImpl(arena, tennisBallFactory, chatMessageService, plugin, commandService, placeHolderService, language)
            games.add(tennisGameImpl)
            plugin.logger.log(Level.INFO, "Game '" + arena.name + "' is ready.")
        } else {
            plugin.logger.log(Level.INFO, "Cannot boot game '" + arena.name + "' because it is not enabled.")
        }
    }

    /**
     * Gets all running games.
     */
    override fun getAll(): List<TennisGameImpl> {
        return games
    }

    /**
     * Tries to locate a game this player is playing.
     */
    override fun getByPlayer(player: Player): TennisGameImpl? {
        for (game in games) {
            if (game.cachedData.containsKey(player)) {
                return game
            }
        }

        return null
    }

    /**
     * Tries to locate a game of the given name.
     */
    override fun getByName(name: String): TennisGameImpl? {
        for (game in games) {
            if (game.arena.name.equals(name, true)) {
                return game
            }
        }

        return null
    }

    /**
     * Disposes all running games.
     */
    override fun close() {
        for (game in games) {
            game.dispose(false)
            plugin.logger.log(Level.INFO, "Stopped game '" + game.arena.name + "'.")
        }

        games.clear()
    }

    private fun validateGame(arena: TennisArena) {
        if (arena.leaveSpawnpoint.isEmpty()) {
            throw TennisGameException(arena, "Set the leave spawnpoint values in arena ${arena.name}!")
        }
        if (arena.redTeamMeta.lobbySpawnpoint.isEmpty()) {
            throw TennisGameException(arena, "Set the lobby spawnpoint of team red in arena ${arena.name}!")
        }
        if (arena.redTeamMeta.spawnpoints.firstOrNull() == null || arena.redTeamMeta.spawnpoints.first().isEmpty()) {
            throw TennisGameException(arena, "Set the first spawnpoint of team red in arena ${arena.name}!")
        }
        if (arena.redTeamMeta.leftLowerCorner.isEmpty()) {
            throw TennisGameException(arena, "Set the corner 1 of team red in arena ${arena.name}!")
        }
        if (arena.redTeamMeta.rightUpperCorner.isEmpty()) {
            throw TennisGameException(arena, "Set the corner 2 of team red in arena ${arena.name}!")
        }
        if (arena.blueTeamMeta.lobbySpawnpoint.isEmpty()) {
            throw TennisGameException(arena, "Set the lobby spawnpoint of team blue in arena ${arena.name}!")
        }
        if (arena.blueTeamMeta.spawnpoints.firstOrNull() == null || arena.blueTeamMeta.spawnpoints.first().isEmpty()) {
            throw TennisGameException(arena, "Set the first spawnpoint of team blue in arena ${arena.name}!")
        }
        if (arena.blueTeamMeta.leftLowerCorner.isEmpty()) {
            throw TennisGameException(arena, "Set the corner 1 of team blue in arena ${arena.name}!")
        }
        if (arena.blueTeamMeta.rightUpperCorner.isEmpty()) {
            throw TennisGameException(arena, "Set the corner 2 of team blue in arena ${arena.name}!")
        }

        fixCorners(arena.redTeamMeta)
        fixCorners(arena.blueTeamMeta)
    }

    /**
     * Corrects the corner values.
     */
    private fun fixCorners(teamMetadata: TeamMetadata) {
        val minX = min(teamMetadata.leftLowerCorner.x, teamMetadata.rightUpperCorner.x)
        val minY = min(teamMetadata.leftLowerCorner.y, teamMetadata.rightUpperCorner.y)
        val minZ = min(teamMetadata.leftLowerCorner.z, teamMetadata.rightUpperCorner.z)
        val maxX = max(teamMetadata.leftLowerCorner.x, teamMetadata.rightUpperCorner.x)
        val maxY = max(teamMetadata.leftLowerCorner.y, teamMetadata.rightUpperCorner.y)
        val maxZ = max(teamMetadata.leftLowerCorner.z, teamMetadata.rightUpperCorner.z)

        teamMetadata.leftLowerCorner.x = minX
        teamMetadata.leftLowerCorner.y = minY
        teamMetadata.leftLowerCorner.z = minZ
        teamMetadata.rightUpperCorner.x = maxX
        teamMetadata.rightUpperCorner.y = maxY
        teamMetadata.rightUpperCorner.z = maxZ
    }
}
