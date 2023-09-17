package com.github.shynixn.mctennis.impl.physic

import com.github.shynixn.mctennis.contract.PhysicObjectService
import com.github.shynixn.mcutils.packet.api.InteractionType
import com.github.shynixn.mcutils.packet.api.event.PacketEvent
import com.github.shynixn.mcutils.packet.api.packet.PacketInInteractEntity
import com.github.shynixn.mcutils.packet.api.packet.PacketInSteerVehicle
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

internal class PacketListener(private val physicObjectApi: PhysicObjectService) : Listener {
    /**
     * Is called when a new packet arrives.
     */
    @EventHandler
    fun onPacketEvent(event: PacketEvent) {
        val packet = event.packet

        if (packet is PacketInSteerVehicle) {
            val physicObject = physicObjectApi.findPhysicObjectById(packet.entityId) ?: return
            physicObject.ride(event.player, packet.forward, packet.sideward, packet.isJumping)
        } else if (packet is PacketInInteractEntity) {
            val physicObject = physicObjectApi.findPhysicObjectById(packet.entityId) ?: return
            if (packet.actionType == InteractionType.LEFT_CLICK) {
                physicObject.leftClick(event.player)
            } else if (packet.actionType == InteractionType.RIGHT_CLICK) {
                physicObject.rightClick(event.player)
            }
        }
    }
}
