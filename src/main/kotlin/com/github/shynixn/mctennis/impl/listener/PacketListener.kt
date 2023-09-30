package com.github.shynixn.mctennis.impl.listener

import com.github.shynixn.mctennis.contract.TennisBall
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.packet.api.InteractionType
import com.github.shynixn.mcutils.packet.api.event.PacketEvent
import com.github.shynixn.mcutils.packet.api.packet.PacketInInteractEntity
import com.google.inject.Inject
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PacketListener @Inject constructor(private val physicObjectApi: PhysicObjectService) : Listener {
    /**
     * Is called when a new packet arrives.
     */
    @EventHandler
    fun onPacketEvent(event: PacketEvent) {
        val packet = event.packet

        if (packet !is PacketInInteractEntity) {
            return
        }

        if (packet.actionType != InteractionType.LEFT_CLICK) {
            return
        }

        val physicObject = physicObjectApi.findPhysicObjectById(packet.entityId) ?: return

        if (physicObject is TennisBall) {
            physicObject.shoot(event.player)
        }
    }
}
