package com.github.shynixn.mctennis.contract

import com.github.shynixn.mctennis.impl.physic.PhysicMetrics

interface PhysicObjectService : AutoCloseable {
    /**
     * Gets the metrics.
     */
    val metrics: PhysicMetrics

    /**
     * Adds a new physic object to the service.
     * The object is automatically removed i
     */
    fun addPhysicObject(physicObject: PhysicObject)

    /**
     * Creates a new entity id for a physic object.
     */
    fun createNewEntityId(): Int

    /**
     * Tries to locate the physic object by the given entity id.
     */
    fun findPhysicObjectById(id: Int): PhysicObject?
}
