package com.github.shynixn.mctennis.impl.physic

import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.physic.PhysicComponent
import com.github.shynixn.mcutils.common.toLocation
import com.github.shynixn.mcutils.common.toVector
import com.github.shynixn.mcutils.packet.api.*
import com.github.shynixn.mcutils.packet.api.packet.*
import org.bukkit.Location
import org.bukkit.entity.Player

class SlimeEntityComponent(
    physicsComponent: MathComponent,
    private val playerComponent: PlayerComponent,
    private val packetService: PacketService,
    val entityId: Int,
    private val slimeSize: Int,
    private val isVisible: Boolean,
    var filteredPlayers: HashSet<Player>
    ) : PhysicComponent {

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
            it.entityType = EntityType.SLIME
            it.target = location
        })

        if (isVisible) {
            packetService.sendPacketOutEntityMetadata(player, PacketOutEntityMetadata().also {
                it.entityId = this.entityId
                it.slimeSize = this.slimeSize
            })
        } else {
            packetService.sendPacketOutEntityMetadata(player, PacketOutEntityMetadata().also {
                it.entityId = this.entityId
                it.slimeSize = this.slimeSize
                it.isInvisible = true
            })
        }
    }

    private fun onPlayerRemove(player: Player) {
        if (filteredPlayers.contains(player)) {
            return
        }

        packetService.sendPacketOutEntityDestroy(player, PacketOutEntityDestroy().also {
            it.entityIds = listOf(this.entityId)
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
                it.target = position.toLocation()
            })
        }
    }

    /**
     * Closes the component.
     */
    override fun close() {
       filteredPlayers.clear()
    }
}
