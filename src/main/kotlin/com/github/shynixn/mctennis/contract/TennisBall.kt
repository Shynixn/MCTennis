package com.github.shynixn.mctennis.contract

import com.github.shynixn.mcutils.common.Vector3d

interface TennisBall {
    /**
     * Sets the velocity of the tennis ball.
     */
    fun setVelocity(vector: Vector3d)

    /**
     * Removes the ball.
     */
    fun remove()
}
