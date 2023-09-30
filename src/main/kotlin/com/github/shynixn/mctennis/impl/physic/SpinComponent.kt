package com.github.shynixn.mctennis.impl.physic

import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.physic.PhysicComponent
import com.github.shynixn.mcutils.common.vector
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos

class SpinComponent(
    private val physicComponent: MathComponent,
    private val maximumSpinVelocity: Double,
    private val baseMultiplier: Double,
    private val maximum: Double,
    private val minimum: Double,
    private val default: Double,
    private val spinVertical : Double
) :
    PhysicComponent {
    init {
        physicComponent.onPostPositionChange.add { _, motion, _ ->
            setPhysic(motion)
        }
    }

    private var angularVelocity = 0.0

    /**
     * Sets the spin direction.
     */
    fun setSpin(previousDirection: Vector3d, currentDirection: Vector3d) {
        val spinV = calculateSpinVelocity(currentDirection, previousDirection)

        val spinDrag = 1.0 - abs(spinV) / (3.0 * maximumSpinVelocity)
        val angle = calculatePitchToLaunch(previousDirection, currentDirection)

        val horizontalMod = baseMultiplier * spinDrag * cos(angle)
        val verticalMod = currentDirection.y * spinVertical

        physicComponent.motion.x = physicComponent.motion.x * horizontalMod
        physicComponent.motion.y = verticalMod
        physicComponent.motion.z = physicComponent.motion.z * horizontalMod

        // Multiply the angular velocity by 2 to make it more visible.
        this.angularVelocity = spinV * 2

        physicComponent.fixYawMotion()
    }

    /**
     * Applies additional motion to the current motion.
     */
    private fun setPhysic(motion: Vector3d) {
        // Handles angular velocity spinning in air.
        if (abs(angularVelocity) < 0.01) {
            return
        }

        val addVector = vector {
            this.x = -motion.z
            this.z = motion.x
        }.multiply(angularVelocity)

        this.physicComponent.motion = vector {
            x = motion.x + addVector.x
            y = motion.y
            z = motion.z + addVector.z
        }

        angularVelocity /= 2
        physicComponent.fixYawMotion()
    }

    /**
     * Calculates the angular velocity in order to spin the physicObject.
     *
     * @return The angular velocity
     */
    private fun calculateSpinVelocity(postVector: Vector3d, initVector: Vector3d): Double {
        val angle = Math.toDegrees(getHorizontalDeviation(initVector, postVector))
        val absAngle = abs(angle).toFloat()
        val maxV = maximumSpinVelocity
        var velocity: Double

        velocity = when (absAngle < 90) {
            true -> maxV * absAngle / 90
            false -> maxV * (180 - absAngle) / 90
        }

        if (angle < 0.0) {
            velocity *= -1f
        }

        return velocity
    }

    /**
     * Calculates the pitch when launching the physicObject.
     * Result depends on the change of pitch. For example,
     * positive value implies that entity raised the pitch of its head.
     *
     * @param preLoc The eye location of entity before a certain event occurs
     * @param postLoc The eye location of entity after a certain event occurs
     * @return Angle measured in Radian
     */
    private fun calculatePitchToLaunch(preLoc: Vector3d, postLoc: Vector3d): Double {
        if (default > maximum || default < minimum) {
            throw IllegalArgumentException("Default value must be in range of minimum and maximum!")
        }

        val delta = (preLoc.pitch - postLoc.pitch)
        val plusBasis = 90 + preLoc.pitch

        val result = when {
            (delta >= 0) -> default + (maximum - default) * delta / plusBasis
            else -> default + (default - minimum) * delta / (180 - plusBasis)
        }

        return Math.toRadians(result)
    }

    /**
     * Calculates the angle deviation between two vectors in X-Z dimension.
     * The angle never exceeds PI. If the calculated value is negative,
     * then subseq vector is actually not subsequent to precede vector.
     * @param subseq The vector subsequent to precede vector in clock-wised order.
     * @param precede The vector preceding subseq vector in clock-wised order.
     * @return A radian angle in the range of -PI to PI
     */
    private fun getHorizontalDeviation(subseq: Vector3d, precede: Vector3d): Double {
        val s = subseq.normalize()
        val p = precede.normalize()
        val dot = s.x * p.x + s.z * p.z
        val det = s.x * p.z - s.z * p.x

        return atan2(det, dot)
    }
}
