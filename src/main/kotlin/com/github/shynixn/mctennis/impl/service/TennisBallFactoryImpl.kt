package com.github.shynixn.mctennis.impl.service

import com.github.shynixn.mctennis.contract.*
import com.github.shynixn.mctennis.entity.TennisBallSettings
import com.github.shynixn.mctennis.impl.TennisBallImpl
import com.github.shynixn.mctennis.entity.MathSettings
import com.github.shynixn.mctennis.impl.physic.*
import com.github.shynixn.mcutils.common.item.ItemService
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.common.sound.SoundService
import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.api.RayTracingService
import org.bukkit.Location
import org.bukkit.plugin.Plugin

class TennisBallFactoryImpl(
    private val physicObjectService: PhysicObjectService,
    private val plugin: Plugin,
    private val soundService: SoundService,
    private val packetService: PacketService,
    private val rayTracingService: RayTracingService,
    private val itemService: ItemService
) :
    TennisBallFactory {

    /**
     * Create a new tennis ball.
     */
    override fun createTennisBall(location: Location, settings: TennisBallSettings, game: TennisGame?): TennisBall {
        val mathSettings = MathSettings()
        mathSettings.airResistanceAbsolute = settings.airResistanceAbsolute
        mathSettings.airResistanceRelative = settings.airResistanceRelative
        mathSettings.gravityAbsolute = settings.gravityAbsolute
        mathSettings.groundResistanceAbsolute = settings.groundResistanceAbsolute
        mathSettings.groundResistanceRelative = settings.groundResistanceRelative
        mathSettings.rayTraceYOffset = settings.rayTraceYOffset
        mathSettings.collideWithWater = settings.collideWithWater
        mathSettings.collideWithPassableBlocks = settings.collideWithPassableBlocks
        val mathPhysicComponent = MathComponent(location.toVector3d(), mathSettings, rayTracingService)

        val bounceComponent =
            BounceComponent(mathPhysicComponent, settings.groundBouncing)

        val playerComponent =
            PlayerComponent(mathPhysicComponent, settings.renderVisibilityUpdateMs, settings.renderDistanceBlocks)

        val armorStandEntityId = packetService.getNextEntityId()
        val slimeEntityId = packetService.getNextEntityId()

        val armorstandEntityComponent =  ArmorstandEntityComponent(
            mathPhysicComponent,
            packetService,
            playerComponent,
            itemService,
            settings,
            armorStandEntityId,
            settings.renderYOffset
        )

        val slimeEntityComponent =  SlimeEntityComponent(
            mathPhysicComponent,
            playerComponent,
            packetService,
            slimeEntityId,
            settings.clickHitBoxSize
        )

        val ball = TennisBallImpl(
            mathPhysicComponent,
            bounceComponent,
            playerComponent,
            armorstandEntityComponent,
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
