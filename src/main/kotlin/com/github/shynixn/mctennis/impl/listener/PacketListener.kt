package com.github.shynixn.mctennis.impl.listener

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.contract.TennisBall
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.packet.api.event.PacketAsyncEvent
import com.github.shynixn.mcutils.packet.api.meta.enumeration.InteractionType
import com.github.shynixn.mcutils.packet.api.packet.PacketInInteractEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class PacketListener (
    private val physicObjectApi: PhysicObjectService,
    private val gameService: GameService,
    private val plugin: Plugin
) : Listener {
    /**
     * Is called when a new packet arrives.
     */
    @EventHandler
    fun onPacketEvent(event: PacketAsyncEvent) {
        val packet = event.packet

        if (packet !is PacketInInteractEntity) {
            return
        }

        if (packet.actionType != InteractionType.ATTACK) {
            return
        }

        plugin.launch {
            val physicObject = physicObjectApi.findPhysicObjectById(packet.entityId) ?: return@launch

            if (physicObject !is TennisBall) {
                return@launch
            }

            val player = event.player
            val game = gameService.getByPlayer(player) ?: return@launch
            val playerData = game.getPlayerData(player) ?: return@launch

            val powerSettings = if(game.servingPlayer == player){
                game.arena.servePowerLevelSettings
            }else{
                game.arena.defaultPowerLevelSettings
            }

            val power = powerSettings.powerLevelMultiplierPerLevel * playerData.currentPowerLevel
            physicObject.shoot(event.player, power)
        }
    }
}
