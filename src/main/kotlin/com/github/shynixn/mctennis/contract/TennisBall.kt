package com.github.shynixn.mctennis.contract

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

interface TennisBall {
    /**
     * Allows clicking the ball.
     */
    var allowActions: Boolean

    /**
     * Checks if the ball is dead.
     */
    val isDead: Boolean

    /**
     * Sets the velocity of the tennis ball.
     */
    fun setVelocity(vector: Vector)

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
