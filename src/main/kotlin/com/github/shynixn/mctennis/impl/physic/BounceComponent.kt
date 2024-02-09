package com.github.shynixn.mctennis.impl.physic

import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.physic.PhysicComponent
import com.github.shynixn.mcutils.common.vector
import com.github.shynixn.mcutils.packet.api.meta.enumeration.BlockDirection

class BounceComponent(
    private val mathComponent: MathComponent,
    /**
     * The bouncing modifiers when a physicObject hits the ground.
     */
    private val groundBouncing: Double = 0.7
) : PhysicComponent {
    private var currentBounce: Vector3d = Vector3d()
    private var bounceMotion: Double? = null
    private var bounceMotionCounter = 0

    /**
     * Function being called when the ground is hit.
     */
    var onGroundAsync: MutableList<(Vector3d, Vector3d) -> Unit> = arrayListOf()

    init {
        mathComponent.onPrePositionChange.add { _, motion, rayTraceResult ->
            if (rayTraceResult.hitBlock) {
                if (rayTraceResult.blockDirection == BlockDirection.UP) {
                    // Ground bounce.
                    this.currentBounce = calculateWallBounce(motion, rayTraceResult.blockDirection)
                } else {
                    // Ordinary wall bounce.
                    mathComponent.motion = calculateWallBounce(motion, rayTraceResult.blockDirection)

                    // Correct the yaw of the object after bouncing.
                    mathComponent.fixYawMotion()
                }
            }
        }
        mathComponent.onPostPositionChange.add { position, motion, rayTraceResult ->
            if (rayTraceResult.hitBlock) {
                if (rayTraceResult.blockDirection == BlockDirection.UP) {
                    // Bouncing is above a threshold, actually bounce.
                    if (currentBounce.y > 0.1) {
                        // The ground needs to be corrected every time because the velocity is not always exactly perfect.
                        position.y = rayTraceResult.block!!.location.y + 1
                        // Cache the bounce and apply it later.
                        this.bounceMotion = currentBounce.y
                        this.bounceMotionCounter = 0
                    }

                    onGroundAsync.forEach { e -> e.invoke(position, motion) }
                }
            }
        }
    }

    /**
     * Tick on async thread.
     */
    override fun tickPhysic() {
        // The bounce motion needs to be shown a few ticks later.
        if (bounceMotion != null) {
            if (bounceMotionCounter < 0) {
                mathComponent.motion.y = bounceMotion!!
                bounceMotion = null
            }

            bounceMotionCounter--
            return
        }
    }

    /**
     * Calculates the outgoing vector from the incoming vector and the wall block direction.
     */
    private fun calculateWallBounce(
        incomingVector: Vector3d, blockDirection: BlockDirection
    ): Vector3d {
        val normalVector = when (blockDirection) {
            BlockDirection.WEST -> {
                vector {
                    x = -1.0
                }
            }
            BlockDirection.EAST -> {
                vector {
                    x = 1.0
                }
            }
            BlockDirection.NORTH -> {
                vector {
                    z = -1.0
                }
            }
            BlockDirection.SOUTH -> {
                vector {
                    z = 1.0
                }
            }
            else -> if (blockDirection == BlockDirection.DOWN) {
                vector {
                    y = -1.0
                }
            } else {
                vector {
                    y = 1.0
                }
            }.normalize()
        }

        val radianAngle = 2 * incomingVector.dot(normalVector)
        val outgoingVector = incomingVector.clone().subtract(normalVector.multiply(radianAngle))

        if (blockDirection == BlockDirection.UP) {
            outgoingVector.y = outgoingVector.y * groundBouncing
        }

        return outgoingVector
    }
}
