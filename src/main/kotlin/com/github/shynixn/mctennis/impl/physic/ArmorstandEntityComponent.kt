package com.github.shynixn.mctennis.impl.physic

import com.github.shynixn.mctennis.contract.PhysicComponent
import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.packet.api.*
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.EulerAngle

class ArmorstandEntityComponent(
    physicsComponent: com.github.shynixn.mctennis.impl.physic.MathComponent,
    private val playerComponent: com.github.shynixn.mctennis.impl.physic.PlayerComponent,
    val entityId: Int,
    private var filteredPlayers: HashSet<Player>
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

        val outer = this
        player.sendPacket(packetOutEntitySpawn {
            this.entityId = outer.entityId
            this.entityType = EntityType.ARMOR_STAND
            this.target = location
        })
        val itemStack = item {
            this.typeName = "PLAYER_HEAD"
            this.nbt =
                "{SkullOwner:{Id:[I;1,1,1,1],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjZkYThhNzk3N2VjOTIxNGM1YjcwMWY5YWU3ZTE1NWI4ZWIyMWQxZDM3MTU5OGUxYjk4NzVjNGM4NWM2NWFlNiJ9fX0=\"}]}}}"
        }.toItemStack()
        player.sendPacket(packetOutEntityEquipment {
            this.entityId = outer.entityId
            this.itemStack = itemStack
            this.slot = ArmorSlotType.HELMET
        })
        player.sendPacket(packetOutEntityMetadata {
            this.entityId = outer.entityId
            this.isInvisible = true
            this.isArmorstandSmall = true
        })
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
                this.target = position.clone().addRelativeDown(0.3).toLocation()
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
        val outer = this

        for (player in players) {
            if (filteredPlayers.contains(player)) {
                continue
            }

            player.sendPacket(packetOutEntityMetadata {
                this.entityId = outer.entityId
                this.armorStandHeadRotation = EulerAngle(rotation, 0.0, 0.0)
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
