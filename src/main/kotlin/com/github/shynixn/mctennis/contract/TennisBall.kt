package com.github.shynixn.mctennis.contract

import com.github.shynixn.mcutils.common.Vector3d

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
     * Gets the location of the ball.
     */
    fun getLocation(): Vector3d

    /**
     * Removes the ball.
     */
    fun remove()
}
