package com.github.shynixn.mctennis.impl.physic

import com.github.shynixn.mctennis.contract.PhysicComponent
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.toLocation
import com.github.shynixn.mcutils.common.toVector
import com.github.shynixn.mcutils.packet.api.*
import org.bukkit.Location
import org.bukkit.entity.Player

class SlimeEntityComponent(
    physicsComponent: MathComponent,
    private val playerComponent: PlayerComponent,
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

        val outer = this
        player.sendPacket(packetOutEntitySpawn {
            this.entityId = outer.entityId
            this.entityType = EntityType.SLIME
            this.target = location
        })

        if (isVisible) { // Bug with the packet in 1.18.2.
            player.sendPacket(packetOutEntityMetadata {
                this.entityId = outer.entityId
                this.slimeSize = outer.slimeSize
            })
        } else {
            player.sendPacket(packetOutEntityMetadata {
                this.entityId = outer.entityId
                this.slimeSize = outer.slimeSize
                this.isInvisible = true
            })
        }
    }

    private fun onPlayerRemove(player: Player) {
        if (filteredPlayers.contains(player)) {
            return
        }

        val outer = this
        player.sendPacket(packetOutEntityDestroy {
            this.entityId = outer.entityId
        })
    }

    private fun onPositionChange(position: Vector3d, motion: Vector3d) {
        val players = playerComponent.visiblePlayers
        val outer = this

        for (player in players) {
            if (filteredPlayers.contains(player)) {
                continue
            }

            player.sendPacket(packetOutEntityVelocity {
                this.entityId = outer.entityId
                this.target = motion.toVector()
            })
            player.sendPacket(packetOutEntityTeleport {
                this.entityId = outer.entityId
                this.target = position.toLocation()
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
