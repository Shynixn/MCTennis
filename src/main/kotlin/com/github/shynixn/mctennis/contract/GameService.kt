package com.github.shynixn.mctennis.contract

import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.impl.TennisGame

interface GameService {
    /**
     * Reloads all games.
     */
    suspend fun reload()

    /**
     * Reloads the specific game.
     */
    suspend fun reload(arena: TennisArena)

    /**
     * Gets all running games.
     */
    fun getAll(): List<TennisGame>

    /**
     * Disposes all running games.
     */
    fun dispose()
}
