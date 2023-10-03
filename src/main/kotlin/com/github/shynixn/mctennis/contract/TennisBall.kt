package com.github.shynixn.mctennis.contract

import com.github.shynixn.mcutils.common.Vector3d
import org.bukkit.Location
import org.bukkit.entity.Player

interface TennisBall {
    /**
     * Allows clicking the ball.
     */
    var allowActions: Boolean

    /**
     * Sets the velocity of the tennis ball.
     */
    fun setVelocity(vector: Vector3d)

    /**
     * Shoots the ball for the given player.
     */
    fun shoot(player: Player, multiplier: Double)

    /**
     * Gets the location of the ball.
     */
    fun getLocation(): Location

    /**
     * Removes the ball.
     */
    fun remove()
}
