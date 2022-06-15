package com.github.shynixn.mctennis.impl.service

import com.github.shynixn.mctennis.contract.CommandService
import com.github.shynixn.mctennis.entity.CommandMeta
import com.github.shynixn.mctennis.enumeration.CommandType
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class CommandServiceImpl : CommandService {
    /**
     * Executes the given command.
     */
    override fun executeCommand(players: List<Player>, meta: CommandMeta) {
        if (meta.type == CommandType.SERVER) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), meta.command)
            return
        }

        if (meta.type == CommandType.PER_PLAYER) {
            for (player in players) {
                val resolvedCommand = meta.command.replace("%player_name%", player.name)
                Bukkit.getServer().dispatchCommand(player, resolvedCommand)
            }
            return
        }

        if (meta.type == CommandType.SERVER_PER_PLAYER) {
            for (player in players) {
                val resolvedCommand = meta.command.replace("%player_name%", player.name)
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), resolvedCommand)
            }
            return
        }
    }

    /**
     * Executes the given commands.
     */
    override fun executeCommands(players: List<Player>, metas: List<CommandMeta>) {
        for (meta in metas) {
            executeCommand(players, meta)
        }
    }
}
