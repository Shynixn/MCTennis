package com.github.shynixn.mctennis.impl

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.contract.TennisBall
import com.github.shynixn.mctennis.contract.TennisGame
import com.github.shynixn.mctennis.entity.TennisBallSettings
import com.github.shynixn.mctennis.event.TennisBallBounceGroundEvent
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.physicobject.api.PhysicObject
import com.github.shynixn.mcutils.physicobject.api.component.*
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

class TennisBallImpl(
    private val physicsComponent: MathComponent,
    private val playerComponent: PlayerComponent,
    private val entityComponent: ArmorstandEntityComponent?, // Armorstand is optional.
    private val spinComponent: SpinComponent,
    private val slimeEntityComponent: SlimeEntityComponent,
    private val settings: TennisBallSettings,
    private val plugin: Plugin,
    var game: TennisGame? = null
) : PhysicObject, TennisBall {
    private var lastClick = 0L
    private var currentLocation = Vector3d()

    init {
        physicsComponent.onGroundAsync.add { position, motion -> onTouchGround() }
    }

    /**
     * Gets all entity ids.
     */
    override val entityIds: List<Int> by lazy {
        if (entityComponent == null) {
            arrayListOf(slimeEntityComponent.entityId)
        } else {
            arrayListOf(entityComponent.entityId, slimeEntityComponent.entityId)
        }
    }

    /**
     * Sets the ball dead or not.
     */
    override var isDead: Boolean = false
        private set

    /**
     * Allows clicking the ball.
     */
    override var allowActions: Boolean = false

    /**
     * Sets the velocity in the world.
     */
    override fun setVelocity(vector: Vector3d) {
        physicsComponent.setVelocity(vector)
    }

    /**
     * Gets the location of the ball.
     */
    override fun getLocation(): Vector3d {
        return currentLocation
    }

    /**
     * LeftClick on the physic object.
     */
    override fun leftClick(player: Player) {
        if (!allowActions) {
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
            val kickVector = player.eyeLocation.direction.toVector3d().normalize().multiply(0.7)
            kickVector.y += 0.25
            setVelocity(kickVector)
            delay(250)
            spinComponent.setSpin(prevDirection, player.eyeLocation.direction.toVector3d())
        }
    }

    /**
     * Gets called on ground bounce.
     */
    private fun onTouchGround() {
        if (!allowActions) {
            return
        }

        val ball = this
        plugin.launch {
            val event = TennisBallBounceGroundEvent(ball, game!!)
            Bukkit.getPluginManager().callEvent(event)
        }
    }

    /**
     * Ticks on minecraft thread.
     */
    override fun tickMinecraft() {
        this.currentLocation = physicsComponent.position.clone()
        physicsComponent.tickMinecraft()
        playerComponent.tickMinecraft()
        entityComponent?.tickMinecraft()
        slimeEntityComponent.tickMinecraft()
    }

    /**
     * Tick on async thread.
     */
    override fun tickAsync() {
        physicsComponent.tickAsync()
        playerComponent.tickAsync()
        entityComponent?.tickAsync()
        slimeEntityComponent.tickAsync()
    }

    /**
     * Removes the physic object.
     */
    override fun remove() {
        physicsComponent.close()
        playerComponent.close()
        entityComponent?.close()
        slimeEntityComponent.close()
        isDead = true
        game = null
    }
}
