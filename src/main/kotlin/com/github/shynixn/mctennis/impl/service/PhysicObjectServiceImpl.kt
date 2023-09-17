package com.github.shynixn.mctennis.impl.service

import com.github.shynixn.mccoroutine.bukkit.CoroutineTimings
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mctennis.contract.PhysicObject
import com.github.shynixn.mctennis.contract.PhysicObjectService
import com.github.shynixn.mctennis.impl.physic.*
import com.github.shynixn.mctennis.impl.physic.PacketListener
import com.github.shynixn.mctennis.impl.physic.PhysicObjectDispatcher
import com.github.shynixn.mcutils.packet.api.PacketInType
import com.github.shynixn.mcutils.packet.impl.PacketServiceImpl
import com.github.shynixn.mcutils.packet.impl.service.EntityServiceImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.Date
import java.util.logging.Level

class PhysicObjectServiceImpl(private val plugin: Plugin, private val physicObjectDispatcher: PhysicObjectDispatcher) : PhysicObjectService {
    private var entityService = EntityServiceImpl()
    private var packetApiImpl = PacketServiceImpl(plugin)
    private var isDisposed = false

    // Bukkit thread.
    private val physicObjectEntityIdTracked = HashMap<Int, PhysicObject>()

    // Async thread.
    private val asyncObjects = ArrayList<PhysicObject>()
    private var lastTick = 0L

    /**
     * Gets the metrics.
     */
    override val metrics: PhysicMetrics = PhysicMetrics()

    init {
        packetApiImpl.registerPacketType(PacketInType.USEENTITY)
        packetApiImpl.registerPacketType(PacketInType.STEERENTITY)
        Bukkit.getPluginManager().registerEvents(PacketListener(this), plugin)
        plugin.launch(physicObjectDispatcher) {
            while (!isDisposed) {
                try {
                    val physicObjectsToBeProcessed = asyncObjects.toTypedArray()

                    // A single context switch per tick is much more performant than a context switch per physicObject.
                    withContext(plugin.minecraftDispatcher + object : CoroutineTimings() {}) {
                        if (metrics.isEnabled) {
                            val currenTime = Date().time
                            metrics.lastTickMilliseconds = currenTime - lastTick
                            lastTick = currenTime
                        }

                        var tickPerformanceOptimizerTimeStamp = Date().time

                        for (physicObject in physicObjectsToBeProcessed) {
                            physicObject.tickMinecraft()

                            if (physicObject.isDead) {
                                removeObject(physicObject)
                            }

                            val currentTime = Date().time
                            val timeSpent = currentTime - tickPerformanceOptimizerTimeStamp

                            if (timeSpent > 20) {
                                if (metrics.isEnabled) {
                                    metrics.optimizationDelayRequired++
                                }
                                delay(1) // On tick.
                                tickPerformanceOptimizerTimeStamp = currentTime
                            }
                        }
                    }

                    for (physicObject in physicObjectsToBeProcessed) {
                        physicObject.tickAsync()
                    }

                } catch (e: Exception) {
                    plugin.logger.log(Level.SEVERE, "Failed to tick physicObject.", e)
                }
            }
        }
    }

    /**
     * Adds a new physic object to the service.
     * The object is automatically removed i
     */
    override fun addPhysicObject(physicObject: PhysicObject) {
        if (isDisposed) {
            throw RuntimeException("PhysicObject API is already disposed! Cannot spawn new physicObject!")
        }

        for (entityId in physicObject.entityIds) {
            physicObjectEntityIdTracked[entityId] = physicObject
        }

        plugin.launch(physicObjectDispatcher) {
            asyncObjects.add(physicObject)
        }

    }

    /**
     * Creates a new entity id for a physic object.
     */
    override fun createNewEntityId(): Int {
        return entityService.createNewEntityId()
    }

    /**
     * Tries to locate the physicObject with the given entity id.
     */
    override fun findPhysicObjectById(id: Int): PhysicObject? {
        if (physicObjectEntityIdTracked.containsKey(id)) {
            return physicObjectEntityIdTracked[id]
        }
        return null
    }

    /**
     * Removes the given physicObject.
     */
    private fun removeObject(physicObject: PhysicObject) {
        for (entityID in physicObject.entityIds) {
            physicObjectEntityIdTracked.remove(entityID)
        }

        plugin.launch(physicObjectDispatcher) {
            if (asyncObjects.contains(physicObject)) {
                asyncObjects.remove(physicObject)
            }
        }
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * `try`-with-resources statement.
     * However, implementers of this interface are strongly encouraged
     * to make their `close` methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    override fun close() {
        if (isDisposed) {
            return
        }

        physicObjectDispatcher.close()
        for (physicObject in physicObjectEntityIdTracked.values.toTypedArray()) {
            physicObject.remove()
        }
        packetApiImpl.close()
        isDisposed = true
    }
}
