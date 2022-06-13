package com.github.shynixn.mctennis.impl.commandexecutor

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import com.github.shynixn.mctennis.MCTennisLanguage
import com.github.shynixn.mctennis.MCTennisPlugin
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.JoinResult
import com.github.shynixn.mctennis.enumeration.Permission
import com.github.shynixn.mctennis.enumeration.Team
import com.github.shynixn.mctennis.impl.exception.TennisArenaException
import com.github.shynixn.mcutils.arena.api.CacheArenaRepository
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.chat.ChatColor
import com.github.shynixn.mcutils.common.reloadTranslation
import com.github.shynixn.mcutils.common.translateChatColors
import com.google.inject.Inject
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Level
import kotlin.streams.asSequence

class MCTennisCommandExecutor @Inject constructor(
    private val arenaRepository: CacheArenaRepository<TennisArena>,
    private val gameService: GameService,
    private val plugin: Plugin,
    private val configurationService: ConfigurationService
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
            val name = args[1]
            deleteArena(sender, name)
            return true
        }

        if (args.size == 1 && args[0].equals("list", true)) {
            listArena(sender)
            return true
        }

        if (sender is Player && args.size == 3 && args[0].equals("inventory", true)) {
            val name = args[1]
            val team = args[2]
            setInventory(sender, name, team)
            return true
        }

        if (sender is Player && args.size == 3 && args[0].equals("armor", true)) {
            val name = args[1]
            val team = args[2]
            setArmor(sender, name, team)
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

        if (sender is Player && args.size == 1 && args[0].equals("leave", true)) {
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

        if (args.size == 1 && args[0].equals("help", true)) {
            sender.sendMessage("---------MCTennis---------")
            sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis create <name> <displayName>")
            sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis delete <name>")
            sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis list")
            sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis inventory <name> <red/blue>")
            sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis armor <name> <red/blue>")
            sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis join <name>")
            sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis leave")
            sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis reload")
            sender.sendMessage("----------┌1/1┐----------")
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
            if (sender.hasPermission(Permission.EDIT_GAME.permissionString)) {
                return arrayListOf("create", "delete", "list", "join", "leave", "reload", "help", "inventory")
            }

            return arrayListOf("join", "leave")
        }

        if (args.size == 2 && (args[0].equals("create", true)
                    || args[0].equals("delete", true) || args[0].equals("reload", true))
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

        val game = gameService.getByName(name)

        if (game == null) {
            player.sendMessage(MCTennisLanguage.gameDoesNotExistMessage.format(name))
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
            player.sendMessage(MCTennisLanguage.gameIsFullMessage)
            return
        }

        if (joinResult == JoinResult.SUCCESS_BLUE) {
            player.sendMessage(MCTennisLanguage.joinedTeamSuccessMessage.format(game.arena.blueTeamMeta.name))
        } else if (joinResult == JoinResult.SUCCESS_RED) {
            player.sendMessage(MCTennisLanguage.joinedTeamSuccessMessage.format(game.arena.redTeamMeta.name))
        }
    }

    private fun leaveGame(player: Player) {
        for (game in gameService.getAll()) {
            if (game.getPlayers().contains(player)) {
                game.leave(player)
            }
        }

        player.sendMessage(MCTennisLanguage.leftGameMessage)
    }

    private suspend fun listArena(sender: CommandSender) {
        val existingArenas = arenaRepository.getAll()

        sender.sendMessage("---------MCTennis---------")
        for (arena in existingArenas) {
            if (arena.isEnabled) {
                sender.sendMessage(ChatColor.GRAY.toString() + arena.name + " [${arena.displayName.translateChatColors()}" + ChatColor.GRAY + "] " + ChatColor.GREEN + "[enabled]")
            } else {
                sender.sendMessage(ChatColor.GRAY.toString() + arena.name + " [${arena.displayName.translateChatColors()}" + ChatColor.GRAY + "] " + ChatColor.RED + "[disabled]")

            }

            sender.sendMessage()
        }
        sender.sendMessage("----------┌1/1┐----------")
    }

    private suspend fun deleteArena(sender: CommandSender, name: String) {
        val existingArenas = arenaRepository.getAll()
        val existingArena = existingArenas.firstOrNull { e -> e.name.equals(name, true) }

        if (existingArena == null) {
            sender.sendMessage(MCTennisLanguage.gameDoesNotExistMessage.format(name))
            return
        }

        arenaRepository.delete(existingArena)
        sender.sendMessage(MCTennisLanguage.deletedGameMessage.format(name))
    }

    private suspend fun createArena(sender: CommandSender, name: String, displayName: String) {
        val existingArenas = arenaRepository.getAll()

        if (existingArenas.firstOrNull { e -> e.name.equals(name, true) } != null) {
            sender.sendMessage(MCTennisLanguage.gameAlreadyExistsMessage.format(name))
            return
        }

        val arena = TennisArena()
        arena.name = name
        arena.displayName = displayName
        arenaRepository.save(arena)
        sender.sendMessage(MCTennisLanguage.gameCreatedMessage.format(arena.name))
    }

    private suspend fun reloadArena(sender: CommandSender, name: String?) {
        if (name == null) {
            plugin.reloadConfig()
            val language = configurationService.findValue<String>("language")
            plugin.reloadTranslation(language, MCTennisLanguage::class.java, "en_us")
            plugin.logger.log(Level.INFO, "Loaded language file $language.properties.")
            arenaRepository.clearCache()

            try {
                gameService.reloadAll()
            } catch (e: TennisArenaException) {
                sender.sendMessage(MCTennisPlugin.prefix + ChatColor.RED.toString() + "Failed to reload arena ${e.arena.name}.")
                sender.sendMessage(MCTennisPlugin.prefix + e.message)
                return
            }

            sender.sendMessage(MCTennisLanguage.reloadedAllGamesMessage)
            return
        }

        val arena = arenaRepository.getAll().firstOrNull { e -> e.name.equals(name, true) }

        if (arena == null) {
            sender.sendMessage(MCTennisLanguage.gameDoesNotExistMessage.format(name))
            return
        }

        try {
            gameService.reload(arena)
        } catch (e: TennisArenaException) {
            sender.sendMessage(MCTennisPlugin.prefix + ChatColor.RED.toString() + "Failed to reload arena ${e.arena.name}.")
            sender.sendMessage(MCTennisPlugin.prefix + e.message)
            return
        }
        sender.sendMessage(MCTennisLanguage.reloadedGameMessage.format(name))
        return
    }

    private suspend fun setInventory(player: Player, name: String, teamName: String) {
        val arena = arenaRepository.getAll().firstOrNull { e -> e.name.equals(name, true) }

        if (arena == null) {
            player.sendMessage(MCTennisLanguage.gameDoesNotExistMessage.format(name))
            return
        }

        val team: Team = try {
            Team.valueOf(teamName.uppercase(Locale.ENGLISH))
        } catch (e: Exception) {
            player.sendMessage(MCTennisLanguage.teamDoesNotExistMessage.format(teamName))
            return
        }
        val teamMeta = if (team == Team.RED) {
            arena.redTeamMeta
        } else {
            arena.blueTeamMeta
        }

        teamMeta.inventoryContents = player.inventory.contents.clone().map { e -> e?.serialize() }.toTypedArray()
        arenaRepository.save(arena)
        player.sendMessage(MCTennisLanguage.updatedInventoryMessage)
    }

    private suspend fun setArmor(player: Player, name: String, teamName: String) {
        val arena = arenaRepository.getAll().firstOrNull { e -> e.name.equals(name, true) }

        if (arena == null) {
            player.sendMessage(MCTennisLanguage.gameDoesNotExistMessage.format(name))
            return
        }

        val team: Team = try {
            Team.valueOf(teamName.uppercase(Locale.ENGLISH))
        } catch (e: Exception) {
            player.sendMessage(MCTennisLanguage.teamDoesNotExistMessage.format(teamName))
            return
        }
        val teamMeta = if (team == Team.RED) {
            arena.redTeamMeta
        } else {
            arena.blueTeamMeta
        }

        teamMeta.armorInventoryContents =
            player.inventory.armorContents.clone().map { e -> e?.serialize() }.toTypedArray()
        arenaRepository.save(arena)
        player.sendMessage(MCTennisLanguage.updatedArmorMessage)
    }
}
