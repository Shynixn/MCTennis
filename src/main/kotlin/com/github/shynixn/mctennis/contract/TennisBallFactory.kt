package com.github.shynixn.mctennis.contract

import com.github.shynixn.mctennis.entity.TennisBallSettings
import org.bukkit.Location
import org.bukkit.entity.Player

interface TennisBallFactory {
    /**
     * Create a new tennis ball.
     */
    fun createTennisBall(location: Location, game: TennisGame, settings: TennisBallSettings): TennisBall
}
