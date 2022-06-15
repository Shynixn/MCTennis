package com.github.shynixn.mctennis.contract

import com.github.shynixn.mctennis.entity.CommandMeta
import org.bukkit.entity.Player

interface CommandService {
    /**
     * Executes the given command.
     */
    fun executeCommand(players: List<Player>, meta: CommandMeta)

    /**
     * Executes the given commands.
     */
    fun executeCommands(players: List<Player>, metas: List<CommandMeta>)
}
