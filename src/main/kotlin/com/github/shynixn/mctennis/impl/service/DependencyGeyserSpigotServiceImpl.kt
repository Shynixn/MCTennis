package com.github.shynixn.mctennis.impl.service

import org.bukkit.entity.Player
import org.geysermc.api.Geyser

class DependencyGeyserSpigotServiceImpl {

    fun isBedrockPlayer(player: Player): Boolean {
        val connection = Geyser.api().connectionByUuid(player.uniqueId)
        return connection != null
    }
}
