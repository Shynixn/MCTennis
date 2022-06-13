package com.github.shynixn.mctennis.contract

import com.github.shynixn.mctennis.entity.TennisBallSettings
import org.bukkit.Location

interface TennisBallFactory {
    /**
     * Create a new tennis ball.
     */
    fun createTennisBall(location: Location, settings: TennisBallSettings): TennisBall
}
