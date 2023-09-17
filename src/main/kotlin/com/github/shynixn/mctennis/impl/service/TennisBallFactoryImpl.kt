package com.github.shynixn.mctennis.impl.service

import com.github.shynixn.mctennis.contract.*
import com.github.shynixn.mctennis.entity.TennisBallSettings
import com.github.shynixn.mctennis.enumeration.VisibilityType
import com.github.shynixn.mctennis.impl.TennisBallImpl
import com.github.shynixn.mctennis.impl.physic.*
import com.github.shynixn.mcutils.common.SoundService
import com.github.shynixn.mcutils.common.toVector3d
import com.google.inject.Inject
import org.bukkit.Location
import org.bukkit.plugin.Plugin

class TennisBallFactoryImpl @Inject constructor(
    private val physicObjectService: PhysicObjectService,
    private val plugin: Plugin,
    private val soundService: SoundService,
    private val bedrockService: BedrockService
) :
    TennisBallFactory {

    /**
     * Create a new tennis ball.
     */
    override fun createTennisBall(location: Location, game: TennisGame, settings: TennisBallSettings): TennisBall {
        val mathComponentSettings = MathComponentSettings()
        mathComponentSettings.airResistanceAbsolute = settings.airResistanceAbsolute
        mathComponentSettings.airResistanceRelative = settings.airResistanceRelative
        mathComponentSettings.gravityAbsolute = settings.gravityAbsolute
        mathComponentSettings.groundResistanceAbsolute = settings.groundResistanceAbsolute
        mathComponentSettings.groundResistanceRelative = settings.groundResistanceRelative
        val mathPhysicComponent = MathComponent(location.toVector3d(), mathComponentSettings)

        val bounceComponent =
            com.github.shynixn.mctennis.impl.physic.BounceComponent(mathPhysicComponent, settings.groundBouncing)

        val playerComponent = PlayerComponent(mathPhysicComponent,settings.renderVisibilityUpdateMs, settings.renderDistanceBlocks )

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

        val armorstandEntityComponent = when (settings.armorstandVisibility) {
            VisibilityType.BEDROCK -> {
                com.github.shynixn.mctennis.impl.physic.ArmorstandEntityComponent(
                    mathPhysicComponent,
                    playerComponent,
                    armorStandEntityId,
                    bedrockService.javaPlayers
                )
            }
            VisibilityType.JAVA -> {
                com.github.shynixn.mctennis.impl.physic.ArmorstandEntityComponent(
                    mathPhysicComponent,
                    playerComponent,
                    armorStandEntityId,
                    bedrockService.bedRockPlayers
                )
            }
            VisibilityType.ALL -> {
                com.github.shynixn.mctennis.impl.physic.ArmorstandEntityComponent(
                    mathPhysicComponent,
                    playerComponent,
                    armorStandEntityId,
                    hashSetOf()
                )
            }
            else -> {
                null
            }
        }

        val slimeEntityComponent = when (settings.slimeVisibility) {
            VisibilityType.BEDROCK -> {
                SlimeEntityComponent(
                    mathPhysicComponent,
                    playerComponent,
                    slimeEntityId,
                    settings.clickHitBoxSize,
                    true,
                    bedrockService.javaPlayers
                )
            }
            VisibilityType.JAVA -> {
                SlimeEntityComponent(
                    mathPhysicComponent,
                    playerComponent,
                    slimeEntityId,
                    settings.clickHitBoxSize,
                    true,
                    bedrockService.bedRockPlayers
                )
            }
            VisibilityType.ALL -> {
                SlimeEntityComponent(
                    mathPhysicComponent,
                    playerComponent,
                    slimeEntityId,
                    settings.clickHitBoxSize,
                    true,
                    hashSetOf()
                )
            }
            else -> {
                SlimeEntityComponent(
                    mathPhysicComponent,
                    playerComponent,
                    slimeEntityId,
                    settings.clickHitBoxSize,
                    false,
                    hashSetOf()
                )
            }
        }

        val ball = TennisBallImpl(
            mathPhysicComponent,
            bounceComponent,
            playerComponent,
            armorstandEntityComponent,
            spinComponent,
            slimeEntityComponent,
            settings,
            plugin,
            soundService,
            game,
        )

        physicObjectService.addPhysicObject(ball)
        return ball
    }
}
