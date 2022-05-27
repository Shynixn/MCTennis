package com.github.shynixn.mctennis.impl.commandexecutor

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import com.github.shynixn.mctennis.MCTennisPlugin
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.JoinResult
import com.github.shynixn.mctennis.enumeration.Team
import com.github.shynixn.mcutils.arena.api.ArenaRepository
import com.google.inject.Inject
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.streams.asSequence

class MCTennisCommandExecutor @Inject constructor(
    private val arenaRepository: ArenaRepository<TennisArena>,
    private val gameService: GameService
) : SuspendingCommandExecutor, SuspendingTabCompleter {
    /**
     * Executes the given command, returning its success.
     * If false is returned, then the "usage" plugin.yml entry for this command (if defined) will be sent to the player.
     * @param sender - Source of the command.
     * @param command - Command which was executed.
     * @param label - Alias of the command which was used.
     * @param args - Passed command arguments.
     * @return True if a valid command, otherwise false.
     */
    override suspend fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.size >= 3 && args[0].equals("create", true)) {
            val name = args[1]
            var displayName = args.asList().stream().skip(2).asSequence().joinToString(" ")

            if (displayName.trim().isEmpty()) {
                displayName = name
            }

            createArena(sender, name, displayName)
            return true
        }

        if (args.size == 2 && args[0].equals("delete", true)) {
            val name = args[0]
            deleteArena(sender, name)
            return true
        }

        if (args.size == 2 && args[0].equals("list", true)) {
            listArena(sender)
            return true
        }

        if (sender is Player && args.size >= 2 && args[0].equals("join", true)) {
            val name = args[1]
            val team = if (args.size > 2) {
                Team.values().firstOrNull { e -> e.name == args[2] }
            } else {
                null
            }

            joinGame(sender, name, team)
            return true
        }

        if (sender is Player && args[0].equals("leave", true)) {
            leaveGame(sender)
            return true
        }

        if (args.size >= 1 && args[0].equals("reload", true)) {
            val name = if (args.size >= 2) {
                args[1]
            } else {
                null
            }

            reloadArena(sender, name)
            return true
        }

        return false
    }

    /**
     * Requests a list of possible completions for a command argument.
     * If the call is suspended during the execution, the returned list will not be shown.
     * @param sender - Source of the command.
     * @param command - Command which was executed.
     * @param alias - Alias of the command which was used.
     * @param args - Passed command arguments.
     * @return A list of possible completions for the final argument, or an empty list.
     */
    override suspend fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (args.size == 1) {
            return arrayListOf("create", "delete", "list", "enable", "disable")
        }

        if (args.size == 2 && (args[0].equals("create", true)
                    || args[0].equals("delete", true)
                    || args[0].equals("enable", true)
                    || args[0].equals("disable", true))
        ) {
            return arenaRepository.getAll().map { e -> e.name }
        }

        if (sender is Player && args.size >= 2 && (args[0].equals("join", true))) {
            return arenaRepository.getAll().map { e -> e.name }
        }

        return emptyList()
    }

    private fun joinGame(player: Player, name: String, team: Team? = null) {
        for (game in gameService.getAll()) {
            if (game.getPlayers().contains(player)) {
                game.leave(player)
            }
        }

        val game = gameService.getAll().firstOrNull { e -> e.arena.name.equals(name, true) }

        if (game == null) {
            player.sendMessage(MCTennisPlugin.prefix + ChatColor.RED + "Game '$name' does not exist.")
            return
        }

        val joinResult = game.join(player, team)

        if (team != null && joinResult == JoinResult.TEAM_FULL) {
            if (team == Team.BLUE) {
                return joinGame(player, name, Team.RED)
            } else {
                return joinGame(player, name, Team.BLUE)
            }
        }

        if (joinResult == JoinResult.GAME_FULL) {
            player.sendMessage(MCTennisPlugin.prefix + ChatColor.RED + "Game is already full.")
            return
        }

        if (joinResult == JoinResult.SUCCESS_BLUE) {
            player.sendMessage(MCTennisPlugin.prefix + ChatColor.BLUE + "Successfully joined team ${game.arena.blueTeamMeta.name}.")
        } else if (joinResult == JoinResult.SUCCESS_RED) {
            player.sendMessage(MCTennisPlugin.prefix + ChatColor.BLUE + "Successfully joined team ${game.arena.redTeamMeta.name}.")
        }
    }

    private fun leaveGame(player: Player) {
        for (game in gameService.getAll()) {
            if (game.getPlayers().contains(player)) {
                game.leave(player)
            }
        }

        player.sendMessage(MCTennisPlugin.prefix + ChatColor.BLUE + "Left the game.")
    }

    private suspend fun listArena(sender: CommandSender) {
        val existingArenas = arenaRepository.getAll()

        for (arena in existingArenas) {
            sender.sendMessage(MCTennisPlugin.prefix + "${arena.name}: " + arena.displayName)
        }
    }

    private suspend fun deleteArena(sender: CommandSender, name: String) {
        val existingArenas = arenaRepository.getAll()
        val existingArena = existingArenas.firstOrNull { e -> e.name.equals(name, true) }

        if (existingArena == null) {
            sender.sendMessage(MCTennisPlugin.prefix + ChatColor.RED + "Arena '$name' does not exist and cannot be deleted.")
            return
        }

        arenaRepository.delete(existingArena)
        sender.sendMessage(MCTennisPlugin.prefix + ChatColor.GREEN + "Deleted arena '$name'.")
    }

    private suspend fun createArena(sender: CommandSender, name: String, displayName: String) {
        val arena = TennisArena()
        arena.name = name
        arena.displayName = displayName
        val existingArenas = arenaRepository.getAll()

        if (existingArenas.firstOrNull { e -> e.name.equals(name, true) } != null) {
            sender.sendMessage(MCTennisPlugin.prefix + ChatColor.RED + "Arena '$name' already exists and cannot be created.")
            return
        }

        arenaRepository.save(arena)
        sender.sendMessage(MCTennisPlugin.prefix + ChatColor.GREEN + "Created arena '$name'.")
    }

    private suspend fun reloadArena(sender: CommandSender, name: String?) {
        if (name == null) {
            gameService.reload()
            sender.sendMessage(MCTennisPlugin.prefix + ChatColor.GREEN + "Reloaded all games.")
            return
        }

        val arena = arenaRepository.getAll().firstOrNull { e -> e.name.equals(name, true) }

        if (arena == null) {
            sender.sendMessage(MCTennisPlugin.prefix + ChatColor.RED + "Game '$name' does not exist.")
            return
        }

        gameService.reload(arena)
        sender.sendMessage(MCTennisPlugin.prefix + ChatColor.GREEN + "Reloaded game '$name'.")
        return
    }
}
