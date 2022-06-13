package com.github.shynixn.mctennis.contract

import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.impl.TennisGameImpl
import org.bukkit.entity.Player

interface GameService {
    /**
     * Reloads all games.
     */
    suspend fun reloadAll()

    /**
     * Reloads the specific game.
     */
    suspend fun reload(arena: TennisArena)

    /**
     * Gets all running games.
     */
    fun getAll(): List<TennisGameImpl>

    /**
     * Tries to locate a game this player is playing.
     */
    fun getByPlayer(player: Player): TennisGameImpl?

    /**
     * Tries to locate a game of the given name.
     */
    fun getByName(name: String): TennisGameImpl?

    /**
     * Disposes all running games.
     */
    fun dispose()
}
