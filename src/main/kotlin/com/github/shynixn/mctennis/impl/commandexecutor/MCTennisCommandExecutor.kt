package com.github.shynixn.mctennis.impl.commandexecutor

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import com.github.shynixn.mctennis.MCTennisLanguage
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.JoinResult
import com.github.shynixn.mctennis.enumeration.Permission
import com.github.shynixn.mctennis.enumeration.Team
import com.github.shynixn.mctennis.impl.exception.TennisArenaException
import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.common.repository.CacheRepository
import com.google.inject.Inject
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Level
import kotlin.streams.asSequence

class MCTennisCommandExecutor @Inject constructor(
    private val arenaRepository: CacheRepository<TennisArena>,
    private val gameService: GameService,
    private val plugin: Plugin,
    private val configurationService: ConfigurationService
) : SuspendingCommandExecutor, SuspendingTabCompleter {
    private val fallBackPrefix: String =
        org.bukkit.ChatColor.BLUE.toString() + "[MCTennis] " + org.bukkit.ChatColor.WHITE

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
        sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): Boolean {
        if (args.size >= 3 && sender.hasPermission(Permission.EDIT_GAME.permission) && args[0].equals(
                "create", true
            )
        ) {
            val name = args[1]
            var displayName = args.asList().stream().skip(2).asSequence().joinToString(" ")

            if (displayName.trim().isEmpty()) {
                displayName = name
            }

            createArena(sender, name, displayName)
            return true
        }

        if (args.size == 2 && sender.hasPermission(Permission.EDIT_GAME.permission) && args[0].equals(
                "delete", true
            )
        ) {
            val name = args[1]
            deleteArena(sender, name)
            return true
        }

        if (args.size == 1 && sender.hasPermission(Permission.EDIT_GAME.permission) && args[0].equals(
                "list", true
            )
        ) {
            listArena(sender)
            return true
        }

        if (sender is Player && sender.hasPermission(Permission.EDIT_GAME.permission) && args.size == 3 && args[0].equals(
                "inventory", true
            )
        ) {
            val name = args[1]
            val team = args[2]
            setInventory(sender, name, team)
            return true
        }

        if (sender is Player && sender.hasPermission(Permission.EDIT_GAME.permission) && args.size == 3 && args[0].equals(
                "location", true
            )
        ) {
            val name = args[1]
            val locationType = args[2]
            setLocation(sender, name, locationType)
            return true
        }

        if (sender.hasPermission(Permission.EDIT_GAME.permission) && args.size == 2 && args[0].equals(
                "toggle", true
            )
        ) {
            val name = args[1]
            toggleGame(sender, name)
            return true
        }

        if (sender is Player && sender.hasPermission(Permission.EDIT_GAME.permission) && args.size == 3 && args[0].equals(
                "armor", true
            )
        ) {
            val name = args[1]
            val team = args[2]
            setArmor(sender, name, team)
            return true
        }

        if (sender is Player && args.size >= 2 && args[0].equals(
                "join", true
            )
        ) {
            val name = args[1]
            val team = if (args.size > 2) {
                Team.values().firstOrNull { e -> e.name == args[2] }
            } else {
                null
            }

            joinGame(sender, name, team)
            return true
        }

        if (sender is Player && args.size == 1 && args[0].equals(
                "leave", true
            )
        ) {
            leaveGame(sender)
            return true
        }

        if (args.size >= 1 && sender.hasPermission(Permission.EDIT_GAME.permission) && args[0].equals(
                "reload", true
            )
        ) {
            val name = if (args.size >= 2) {
                args[1]
            } else {
                null
            }

            reloadArena(sender, name)
            return true
        }

        if (args.size == 1 && args[0].equals("help", true)) {

            if (sender.hasPermission(Permission.EDIT_GAME.permission)) {
                sender.sendMessage("---------MCTennis---------")
                sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis create <name> <displayName>")
                sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis delete <name>")
                sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis toggle <name>")
                sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis list")
                sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis location <name> <type>")
                sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis inventory <name> <red/blue>")
                sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis armor <name> <red/blue>")
                sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis join <name>")
                sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis leave")
                sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis reload")
                sender.sendMessage("----------┌1/1┐----------")
            } else {
                sender.sendMessage("---------MCTennis---------")
                sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis join <name>")
                sender.sendMessage(ChatColor.GRAY.toString() + "/mctennis leave")
                sender.sendMessage("----------┌1/1┐----------")
            }

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
        sender: CommandSender, command: Command, alias: String, args: Array<out String>
    ): List<String> {
        if (args.size == 1) {
            if (sender.hasPermission(Permission.EDIT_GAME.permission)) {
                return arrayListOf(
                    "create",
                    "delete",
                    "list",
                    "location",
                    "join",
                    "armor",
                    "leave",
                    "reload",
                    "help",
                    "inventory",
                    "toggle"
                )
            }

            return arrayListOf("join", "leave")
        }

        if (args.size == 2 && sender.hasPermission(Permission.EDIT_GAME.permission) && (args[0].equals(
                "create", true
            ) || args[0].equals("delete", true) || args[0].equals("reload", true))
        ) {
            return arenaRepository.getAll().map { e -> e.name }
        }

        if (sender is Player && args.size >= 2 && (args[0].equals(
                "join", true
            ))
        ) {
            return arenaRepository.getAll().map { e -> e.name }
        }

        return emptyList()
    }

    private fun joinGame(player: Player, name: String, team: Team? = null) {
        for (game in gameService.getAll()) {
            if (game.getPlayers().contains(player)) {
                if (game.arena.name.equals(name, true)) {
                    // Do not leave, if it is the same game.
                    return
                }

                game.leave(player)
            }
        }

        val game = gameService.getByName(name)

        if (game == null) {
            player.sendMessage(MCTennisLanguage.gameDoesNotExistMessage.format(name))
            return
        }

        if (!player.hasPermission("mctennis.join.${game.arena.name}")) {
            player.sendMessage(MCTennisLanguage.noPermissionForGameMessage.format(game.arena.name))
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

        if (joinResult == JoinResult.GAME_FULL || joinResult == JoinResult.GAME_ALREADY_RUNNING) {
            player.sendMessage(MCTennisLanguage.gameIsFullMessage)
            return
        }

        if (joinResult == JoinResult.SUCCESS_BLUE) {
            player.sendMessage(MCTennisLanguage.joinTeamBlueMessage)
        } else if (joinResult == JoinResult.SUCCESS_RED) {
            player.sendMessage(MCTennisLanguage.joinTeamRedMessage)
        }
    }

    private fun leaveGame(player: Player) {
        var leftGame = false

        for (game in gameService.getAll()) {
            if (game.getPlayers().contains(player)) {
                game.leave(player)
                leftGame = true
            }
        }

        if (leftGame) {
            player.sendMessage(MCTennisLanguage.leftGameMessage)
        }
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

    private suspend fun toggleGame(sender: CommandSender, name: String) {
        val arena = arenaRepository.getAll().firstOrNull { e -> e.name.equals(name, true) }

        if (arena == null) {
            sender.sendMessage(MCTennisLanguage.gameDoesNotExistMessage.format(name))
            return
        }

        try {
            arena.isEnabled = !arena.isEnabled
            gameService.reload(arena)
            sender.sendMessage(MCTennisLanguage.enabledArenaMessage.format(arena.isEnabled.toString()))
        } catch (e: TennisArenaException) {
            arena.isEnabled = !arena.isEnabled
            sender.sendMessage(fallBackPrefix + ChatColor.RED.toString() + "Failed to reload arena ${e.arena.name}.")
            sender.sendMessage(fallBackPrefix + e.message)
            return
        }

        arenaRepository.save(arena)
        sender.sendMessage(MCTennisLanguage.reloadedGameMessage.format(name))
        return
    }

    private suspend fun reloadArena(sender: CommandSender, name: String?) {
        try {
            arenaRepository.clearCache()
        } catch (e: TennisArenaException) {
            sender.sendMessage(fallBackPrefix + ChatColor.RED.toString() + "Failed to reload arenas.")
            sender.sendMessage(fallBackPrefix + e.message)
            return
        }

        if (name == null) {
            plugin.reloadConfig()
            val language = configurationService.findValue<String>("language")
            plugin.reloadTranslation(language, MCTennisLanguage::class.java, "en_us")
            plugin.logger.log(Level.INFO, "Loaded language file $language.properties.")

            try {
                arenaRepository.clearCache()
                gameService.reloadAll()
            } catch (e: TennisArenaException) {
                sender.sendMessage(fallBackPrefix + ChatColor.RED.toString() + "Failed to reload arena ${e.arena.name}.")
                sender.sendMessage(fallBackPrefix + e.message)
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
            arenaRepository.clearCache()
            gameService.reload(arena)
        } catch (e: TennisArenaException) {
            sender.sendMessage(fallBackPrefix + ChatColor.RED.toString() + "Failed to reload arena ${e.arena.name}.")
            sender.sendMessage(fallBackPrefix + e.message)
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

        teamMeta.inventoryContents = player.inventory.contents.clone().map { e ->
            val yamlConfiguration = YamlConfiguration()
            yamlConfiguration.set("item", e)
            yamlConfiguration.saveToString()
        }.toTypedArray()
        arenaRepository.save(arena)
        player.sendMessage(MCTennisLanguage.updatedInventoryMessage)
    }

    private suspend fun setLocation(player: Player, name: String, locationType: String) {
        val arena = arenaRepository.getAll().firstOrNull { e -> e.name.equals(name, true) }

        if (arena == null) {
            player.sendMessage(MCTennisLanguage.gameDoesNotExistMessage.format(name))
            return
        }

        if (locationType == "lobbyRed") {
            arena.redTeamMeta.lobbySpawnpoint = player.location.toVector3d()
        } else if (locationType == "lobbyBlue") {
            arena.blueTeamMeta.lobbySpawnpoint = player.location.toVector3d()
        } else if (locationType == "leave") {
            arena.leaveSpawnpoint = player.location.toVector3d()
        } else if (locationType == "spawnRed1") {
            if (arena.redTeamMeta.spawnpoints.size == 0) {
                arena.redTeamMeta.spawnpoints.add(player.location.toVector3d())
            } else {
                arena.redTeamMeta.spawnpoints[0] = player.location.toVector3d()
            }
        } else if (locationType == "spawnBlue1") {
            if (arena.blueTeamMeta.spawnpoints.size == 0) {
                arena.blueTeamMeta.spawnpoints.add(player.location.toVector3d())
            } else {
                arena.blueTeamMeta.spawnpoints[0] = player.location.toVector3d()
            }
        } else if (locationType == "cornerRed1") {
            arena.redTeamMeta.leftLowerCorner = player.location.toVector3d()
        } else if (locationType == "cornerRed2") {
            arena.redTeamMeta.rightUpperCorner = player.location.toVector3d()
        } else if (locationType == "cornerBlue1") {
            arena.blueTeamMeta.leftLowerCorner = player.location.toVector3d()
        } else if (locationType == "cornerBlue2") {
            arena.blueTeamMeta.rightUpperCorner = player.location.toVector3d()
        } else {
            player.sendMessage(MCTennisLanguage.locationTypeDoesNotExistMessage.format(player.location))
            return
        }

        arenaRepository.save(arena)
        player.sendMessage(MCTennisLanguage.spawnPointSetMessage.format(player.location))
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

        teamMeta.armorInventoryContents = player.inventory.armorContents.clone().map { e ->
            val yamlConfiguration = YamlConfiguration()
            yamlConfiguration.set("item", e)
            yamlConfiguration.saveToString()
        }.toTypedArray()
        arenaRepository.save(arena)
        player.sendMessage(MCTennisLanguage.updatedArmorMessage)
    }
}
