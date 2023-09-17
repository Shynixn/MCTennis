package com.github.shynixn.mctennis.impl.physic

import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.toLocation
import com.github.shynixn.mcutils.common.toVector
import com.github.shynixn.mcutils.common.toVector3d
import org.bukkit.FluidCollisionMode
import org.bukkit.util.NumberConversions
import java.util.*

class RayTracingServiceImpl {
    /**
     * Ray traces in the world for the given motion.
     */
    fun rayTraceMotion(position: Vector3d, motion: Vector3d): RayTraceResult {
        if (!NumberConversions.isFinite(position.yaw.toFloat())) {
            position.yaw = Math.round((position.yaw % 360.0F) * 100.0) / 100.0
        }

        if (!NumberConversions.isFinite(position.pitch.toFloat())) {
            position.pitch = Math.round((position.pitch % 360.0F) * 100.0) / 100.0
        }

        position.x = fixFiniteDomain(position.x)
        position.y = fixFiniteDomain(position.y)
        position.z = fixFiniteDomain(position.z)
        motion.x = fixFiniteDomain(motion.x)
        motion.y = fixFiniteDomain(motion.y)
        motion.z = fixFiniteDomain(motion.z)

        val endPosition =
            Vector3d(position.world!!, position.x + motion.x, position.y + motion.y, position.z + motion.z)
        val sourceLocation = position.toLocation()

        val directionVector = motion.toVector().normalize()
        var distance = motion.length()
        val world = sourceLocation.world!!

        sourceLocation.x = fixFiniteDomain(sourceLocation.x)
        sourceLocation.y = fixFiniteDomain(sourceLocation.y)
        sourceLocation.z = fixFiniteDomain(sourceLocation.z)
        sourceLocation.yaw = fixFiniteDomain(sourceLocation.yaw.toDouble()).toFloat()
        sourceLocation.pitch = fixFiniteDomain(sourceLocation.pitch.toDouble()).toFloat()
        directionVector.x = fixFiniteDomain(directionVector.x)
        directionVector.y = fixFiniteDomain(directionVector.y)
        directionVector.z = fixFiniteDomain(directionVector.z)
        distance = fixFiniteDomain(distance)

        val directionSum = directionVector.x + directionVector.y + directionVector.z

        if (directionSum <= 0.0) {
            // Can happen in rare cases.
            directionVector.x = 0.000001
        }

        val movingObjectPosition =
            world.rayTraceBlocks(sourceLocation, directionVector, distance, FluidCollisionMode.NEVER,true)

        if (movingObjectPosition == null) {
            endPosition.yaw = position.yaw
            endPosition.pitch = position.pitch
            return RayTraceResult(false, endPosition, com.github.shynixn.mctennis.impl.physic.BlockDirection.DOWN)
        }

        val targetPosition = movingObjectPosition.hitPosition.toLocation(world).toVector3d()
        val direction = com.github.shynixn.mctennis.impl.physic.BlockDirection.valueOf(
            movingObjectPosition.hitBlockFace!!.toString().uppercase(Locale.ENGLISH)
        )

        targetPosition.yaw = position.yaw
        targetPosition.pitch = position.pitch

        return RayTraceResult(true, targetPosition, direction, movingObjectPosition.hitBlock)
    }

    private fun fixFiniteDomain(value: Double): Double {
        if (!NumberConversions.isFinite(value)) {
            return Math.round(value * 100.0) / 100.0
        }

        return value
    }
}
