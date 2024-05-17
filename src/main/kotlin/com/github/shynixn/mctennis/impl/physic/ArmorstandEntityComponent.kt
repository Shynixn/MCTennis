package com.github.shynixn.mctennis.impl.physic

import com.github.shynixn.mctennis.entity.TennisBallSettings
import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.common.item.ItemService
import com.github.shynixn.mcutils.common.physic.PhysicComponent
import com.github.shynixn.mcutils.packet.api.*
import com.github.shynixn.mcutils.packet.api.meta.enumeration.ArmorSlotType
import com.github.shynixn.mcutils.packet.api.meta.enumeration.EntityType
import com.github.shynixn.mcutils.packet.api.packet.*
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.EulerAngle

class ArmorstandEntityComponent(
    physicsComponent: MathComponent,
    private val packetService: PacketService,
    private val playerComponent: PlayerComponent,
    private val itemService: ItemService,
    private val ballSettings: TennisBallSettings,
    val entityId: Int,
    private var filteredPlayers: HashSet<Player>,
    private val renderOffsetY : Double
) : PhysicComponent {
    private var rotation = 0.0

    init {
        playerComponent.onSpawnMinecraft.add { player, location -> onPlayerSpawn(player, location) }
        playerComponent.onRemoveMinecraft.add { player, _ -> onPlayerRemove(player) }
        physicsComponent.onPostPositionChange.add { position, motion, _ -> onPositionChange(position, motion) }
    }

    private fun onPlayerSpawn(player: Player, location: Location) {
        if (filteredPlayers.contains(player)) {
            return
        }

        packetService.sendPacketOutEntitySpawn(player, PacketOutEntitySpawn().also {
            it.entityId = this.entityId
            it.entityType = EntityType.ARMOR_STAND
            it.target = location.toVector3d().addRelativeUp(renderOffsetY).toLocation()
        })

        val itemStack = itemService.toItemStack(ballSettings.item)

        packetService.sendPacketOutEntityEquipment(player, PacketOutEntityEquipment().also {
            it.entityId = this.entityId
            it.items = listOf(Pair(ArmorSlotType.HELMET, itemStack))
        })

        packetService.sendPacketOutEntityMetadata(player, PacketOutEntityMetadata().also {
            it.entityId = this.entityId
            it.isInvisible = true
            it.isArmorstandSmall = true
        })
    }

    private fun onPlayerRemove(player: Player) {
        if (filteredPlayers.contains(player)) {
            return
        }

        val outer = this
        packetService.sendPacketOutEntityDestroy(player, PacketOutEntityDestroy().also {
            it.entityIds = listOf(outer.entityId)
        })
    }

    private fun onPositionChange(position: Vector3d, motion: Vector3d) {
        val players = playerComponent.visiblePlayers

        for (player in players) {
            if (filteredPlayers.contains(player)) {
                continue
            }

            packetService.sendPacketOutEntityVelocity(player, PacketOutEntityVelocity().also {
                it.entityId = this.entityId
                it.target = motion.toVector()
            })

            packetService.sendPacketOutEntityTeleport(player, PacketOutEntityTeleport().also {
                it.entityId = this.entityId
                it.target = position.copy().addRelativeUp(renderOffsetY).toLocation()
            })
        }

        doAnimation(motion)
    }

    private fun doAnimation(motion: Vector3d) {
        // 360 0 0 is a full forward rotation.
        val prevRotation = rotation

        if (Math.abs(motion.x) > 1.0 || Math.abs(motion.z) > 1.0) {
            rotation = rotation - 30
        } else if (Math.abs(motion.x) > 0.1 || Math.abs(motion.z) > 0.1) {
            rotation = rotation - 10
        } else if (Math.abs(motion.x) > 0.01 || Math.abs(motion.z) > 0.01) {
            rotation = rotation - 5
        } else if (Math.abs(motion.x) > 0.0001 || Math.abs(motion.z) > 0.0001) {
            rotation = rotation - 1
        }

        if (prevRotation == rotation) {
            return
        }

        val players = playerComponent.visiblePlayers

        for (player in players) {
            if (filteredPlayers.contains(player)) {
                continue
            }

            packetService.sendPacketOutEntityMetadata(player, PacketOutEntityMetadata().also {
                it.entityId = this.entityId
                it.armorStandHeadRotation = EulerAngle(rotation, 0.0, 0.0)
            })
        }
    }
}
