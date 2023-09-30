package com.github.shynixn.mctennis.contract

import com.github.shynixn.mctennis.entity.TennisArena
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
    fun getAll(): List<TennisGame>

    /**
     * Tries to locate a game this player is playing.
     */
    fun getByPlayer(player: Player): TennisGame?

    /**
     * Tries to locate a game of the given name.
     */
    fun getByName(name: String): TennisGame?

    /**
     * Disposes all running games.
     */
    fun close()
}
