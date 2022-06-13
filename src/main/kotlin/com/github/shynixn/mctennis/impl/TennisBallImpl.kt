package com.github.shynixn.mctennis.impl

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.contract.TennisBall
import com.github.shynixn.mctennis.entity.TennisBallSettings
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.physicobject.api.PhysicObject
import com.github.shynixn.mcutils.physicobject.api.component.*
import kotlinx.coroutines.delay
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

class TennisBallImpl(
    private val physicsComponent: MathComponent,
    private val playerComponent: PlayerComponent,
    private val entityComponent: ArmorstandEntityComponent,
    private val spinComponent: SpinComponent,
    private val slimeEntityComponent: SlimeEntityComponent,
    private val settings: TennisBallSettings,
    private val plugin: Plugin,
) : PhysicObject, TennisBall {
    private var lastClick = 0L

    /**
     * Gets all entity ids.
     */
    override val entityIds: List<Int> = arrayListOf(entityComponent.entityId, slimeEntityComponent.entityId)

    /**
     * Sets the ball dead or not.
     */
    override var isDead: Boolean = false
        private set

    /**
     * Gets the entity id.
     */
    val entityId: Int
        get() {
            return entityComponent.entityId
        }

    /**
     * Allows clicking the ball.
     */
    override var allowLeftClick: Boolean = false

    /**
     * Sets the velocity in the world.
     */
    override fun setVelocity(vector: Vector3d) {
        physicsComponent.setVelocity(vector)
    }

    /**
     * LeftClick on the physic object.
     */
    override fun leftClick(player: Player) {
        if (!allowLeftClick) {
            return
        }

        val current = Date().time
        val timeDif = current - lastClick

        if (timeDif < settings.clickCooldown) {
            return
        }

        lastClick = current

        plugin.launch {
            val prevDirection = player.eyeLocation.direction.toVector3d()
            val kickVector = player.eyeLocation.direction.toVector3d().normalize().multiply(0.5)
            kickVector.y += 0.25
            setVelocity(kickVector)
            delay(250)
            spinComponent.setSpin(prevDirection, player.eyeLocation.direction.toVector3d())
        }
    }

    /**
     * Ticks on minecraft thread.
     */
    override fun tickMinecraft() {
        physicsComponent.tickMinecraft()
        playerComponent.tickMinecraft()
        entityComponent.tickMinecraft()
    }

    /**
     * Tick on async thread.
     */
    override fun tickAsync() {
        physicsComponent.tickAsync()
        playerComponent.tickAsync()
        entityComponent.tickAsync()
    }

    /**
     * Removes the physic object.
     */
    override fun remove() {
        physicsComponent.close()
        playerComponent.close()
        entityComponent.close()
        isDead = true
    }
}
