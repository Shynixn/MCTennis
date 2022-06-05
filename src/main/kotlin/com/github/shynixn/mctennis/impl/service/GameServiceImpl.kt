package com.github.shynixn.mctennis.impl.service

import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.impl.TennisGame
import com.github.shynixn.mcutils.arena.api.ArenaRepository
import com.google.inject.Inject
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class GameServiceImpl @Inject constructor(
    private val arenaRepository: ArenaRepository<TennisArena>,
    private val plugin: Plugin
) : GameService {
    private val games = ArrayList<TennisGame>()

    /**
     * Reloads all games.
     */
    override suspend fun reloadAll() {
        dispose()

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
        val existingGame = games.firstOrNull { e -> e.arena.name == arena.name }
        if (existingGame != null) {
            existingGame.dispose()
            games.remove(existingGame)
        }

        if (arena.isEnabled) {
            val tennisGame = TennisGame(arena)
            tennisGame.plugin = plugin
            games.add(tennisGame)
        }
    }

    /**
     * Gets all running games.
     */
    override fun getAll(): List<TennisGame> {
        return games
    }

    /**
     * Tries to locate a game this player is playing.
     */
    override fun getByPlayer(player: Player): TennisGame? {
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
    override fun getByName(name: String): TennisGame? {
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
    override fun dispose() {
        for (game in games) {
            game.dispose()
        }

        games.clear()
    }
}
