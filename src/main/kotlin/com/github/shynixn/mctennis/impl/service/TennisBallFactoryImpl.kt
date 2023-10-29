package com.github.shynixn.mctennis.impl.service

import com.github.shynixn.mctennis.contract.*
import com.github.shynixn.mctennis.entity.TennisBallSettings
import com.github.shynixn.mctennis.enumeration.VisibilityType
import com.github.shynixn.mctennis.impl.TennisBallImpl
import com.github.shynixn.mctennis.entity.MathSettings
import com.github.shynixn.mctennis.impl.physic.*
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.common.sound.SoundService
import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.packet.api.EntityService
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.api.RayTracingService
import com.google.inject.Inject
import org.bukkit.Location
import org.bukkit.plugin.Plugin

class TennisBallFactoryImpl @Inject constructor(
    private val physicObjectService: PhysicObjectService,
    private val entityService: EntityService,
    private val plugin: Plugin,
    private val soundService: SoundService,
    private val bedrockService: BedrockService,
    private val packetService: PacketService,
    private val rayTracingService: RayTracingService,
) :
    TennisBallFactory {

    /**
     * Create a new tennis ball.
     */
    override fun createTennisBall(location: Location, game: TennisGame, settings: TennisBallSettings): TennisBall {
        val mathSettings = MathSettings()
        mathSettings.airResistanceAbsolute = settings.airResistanceAbsolute
        mathSettings.airResistanceRelative = settings.airResistanceRelative
        mathSettings.gravityAbsolute = settings.gravityAbsolute
        mathSettings.groundResistanceAbsolute = settings.groundResistanceAbsolute
        mathSettings.groundResistanceRelative = settings.groundResistanceRelative
        mathSettings.rayTraceYOffset = settings.rayTraceYOffset
        val mathPhysicComponent = MathComponent(location.toVector3d(), mathSettings, rayTracingService)

        val bounceComponent =
            BounceComponent(mathPhysicComponent, settings.groundBouncing)

        val playerComponent =
            PlayerComponent(mathPhysicComponent, settings.renderVisibilityUpdateMs, settings.renderDistanceBlocks)

        val spinComponent = SpinComponent(
            mathPhysicComponent,
            settings.maximumSpinningVelocity,
            settings.spinBaseMultiplier,
            settings.spinMaximum,
            settings.spinMinimum,
            settings.spinDefault,
            settings.spinVertical
        )

        val armorStandEntityId = entityService.createNewEntityId()
        val slimeEntityId = entityService.createNewEntityId()

        val armorstandEntityComponent = when (settings.armorstandVisibility) {
            VisibilityType.BEDROCK -> {
                ArmorstandEntityComponent(
                    mathPhysicComponent,
                    packetService,
                    playerComponent,
                    armorStandEntityId,
                    bedrockService.javaPlayers,
                    settings.renderYOffset
                )
            }
            VisibilityType.JAVA -> {
                ArmorstandEntityComponent(
                    mathPhysicComponent,
                    packetService,
                    playerComponent,
                    armorStandEntityId,
                    bedrockService.bedRockPlayers,
                    settings.renderYOffset
                )
            }
            VisibilityType.ALL -> {
                ArmorstandEntityComponent(
                    mathPhysicComponent,
                    packetService,
                    playerComponent,
                    armorStandEntityId,
                    hashSetOf(),
                    settings.renderYOffset
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
                    packetService,
                    slimeEntityId,
                    settings.clickHitBoxSize,
                    bedrockService.javaPlayers
                )
            }
            VisibilityType.JAVA -> {
                SlimeEntityComponent(
                    mathPhysicComponent,
                    playerComponent,
                    packetService,
                    slimeEntityId,
                    settings.clickHitBoxSize,
                    bedrockService.bedRockPlayers
                )
            }
            VisibilityType.ALL -> {
                SlimeEntityComponent(
                    mathPhysicComponent,
                    playerComponent,
                    packetService,
                    slimeEntityId,
                    settings.clickHitBoxSize,
                    hashSetOf()
                )
            }
            else -> {
                SlimeEntityComponent(
                    mathPhysicComponent,
                    playerComponent,
                    packetService,
                    slimeEntityId,
                    settings.clickHitBoxSize,
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
