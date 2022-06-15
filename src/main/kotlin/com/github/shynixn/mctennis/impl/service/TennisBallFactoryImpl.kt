package com.github.shynixn.mctennis.impl.service

import com.github.shynixn.mctennis.contract.TennisBall
import com.github.shynixn.mctennis.contract.TennisBallFactory
import com.github.shynixn.mctennis.contract.TennisGame
import com.github.shynixn.mctennis.entity.TennisBallSettings
import com.github.shynixn.mctennis.impl.TennisBallImpl
import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.physicobject.api.MathComponentSettings
import com.github.shynixn.mcutils.physicobject.api.PhysicObjectService
import com.github.shynixn.mcutils.physicobject.api.PlayerComponentSettings
import com.github.shynixn.mcutils.physicobject.api.component.*
import com.google.inject.Inject
import org.bukkit.Location
import org.bukkit.plugin.Plugin

class TennisBallFactoryImpl @Inject constructor(
    private val physicObjectService: PhysicObjectService,
    private val plugin: Plugin
) :
    TennisBallFactory {
    /**
     * Create a new tennis ball.
     */
    override fun createTennisBall(location: Location, game: TennisGame, settings: TennisBallSettings): TennisBall {
        val mathComponentSettings = MathComponentSettings()
        mathComponentSettings.airResistanceAbsolute = settings.airResistanceAbsolute
        mathComponentSettings.airResistanceRelative = settings.airResistanceRelative
        mathComponentSettings.groundBouncing = settings.groundBouncing
        mathComponentSettings.gravityAbsolute = settings.gravityAbsolute
        mathComponentSettings.groundResistanceAbsolute = settings.groundResistanceAbsolute
        mathComponentSettings.groundResistanceRelative = settings.groundResistanceRelative
        val mathPhysicComponent = MathComponent(location.toVector3d(), mathComponentSettings)

        val playerComponentSettings = PlayerComponentSettings()
        playerComponentSettings.renderDistanceBlocks = settings.renderDistanceBlocks
        playerComponentSettings.renderVisibilityUpdateMs = settings.renderVisibilityUpdateMs
        val playerComponent = PlayerComponent(mathPhysicComponent, playerComponentSettings)

        val spinComponent = SpinComponent(
            mathPhysicComponent,
            settings.maximumSpinningVelocity,
            settings.spinBaseMultiplier,
            settings.spinMaximum,
            settings.spinMinimum,
            settings.spinDefault,
            settings.spinVertical
        )

        val armorStandEntityId = physicObjectService.createNewEntityId()
        val slimeEntityId = physicObjectService.createNewEntityId()

        val armorstandEntityComponent = if (settings.isArmorstandVisible) {
            ArmorstandEntityComponent(mathPhysicComponent, playerComponent, armorStandEntityId)
        } else {
            null
        }

        val slimeEntity =
            SlimeEntityComponent(mathPhysicComponent, playerComponent, slimeEntityId, settings.clickHitBoxSize, false)

        val ball = TennisBallImpl(
            mathPhysicComponent,
            playerComponent,
            armorstandEntityComponent,
            spinComponent,
            slimeEntity,
            settings,
            plugin,
            game
        )

        physicObjectService.addPhysicObject(ball)
        return ball
    }
}
