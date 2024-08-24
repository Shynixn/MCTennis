package com.github.shynixn.mctennis.impl.commandexecutor

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.MCTennisDependencyInjectionModule
import com.github.shynixn.mctennis.MCTennisLanguage
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.entity.TeamMetadata
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.JoinResult
import com.github.shynixn.mctennis.enumeration.LocationType
import com.github.shynixn.mctennis.enumeration.Permission
import com.github.shynixn.mctennis.enumeration.Team
import com.github.shynixn.mctennis.impl.exception.TennisGameException
import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.common.chat.ChatMessageService
import com.github.shynixn.mcutils.common.command.CommandBuilder
import com.github.shynixn.mcutils.common.command.CommandMeta
import com.github.shynixn.mcutils.common.command.CommandType
import com.github.shynixn.mcutils.common.command.Validator
import com.github.shynixn.mcutils.common.repository.CacheRepository
import com.github.shynixn.mcutils.sign.SignService
import com.google.inject.Inject
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Level

class MCTennisCommandExecutor @Inject constructor(
    private val arenaRepository: CacheRepository<TennisArena>,
    private val gameService: GameService,
    private val plugin: Plugin,
    private val configurationService: ConfigurationService,
    private val signService: SignService,
    chatMessageService: ChatMessageService
) {
    private val fallBackPrefix: String =
        org.bukkit.ChatColor.BLUE.toString() + "[MCTennis] " + org.bukkit.ChatColor.WHITE
    private val arenaTabs: suspend (s: CommandSender) -> List<String> = {
        arenaRepository.getAll().map { e -> e.name }
    }
    private val coroutineExecutor = object : CoroutineExecutor {
        override fun execute(f: suspend () -> Unit) {
            plugin.launch {
                f.invoke()
            }
        }
    }

    private val remainingStringValidator = object : Validator<String> {
        override suspend fun transform(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return openArgs.joinToString(" ")
        }
    }
    private val maxLengthValidator = object : Validator<String> {
        override suspend fun validate(
            sender: CommandSender,
            prevArgs: List<Any>,
            argument: String,
            openArgs: List<String>
        ): Boolean {
            return argument.length < 20
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return MCTennisLanguage.maxLength20Characters
        }
    }
    private val gameMustNotExistValidator = object : Validator<String> {
        override suspend fun validate(
            sender: CommandSender,
            prevArgs: List<Any>,
            argument: String,
            openArgs: List<String>
        ): Boolean {
            val existingArenas = arenaRepository.getAll()
            return existingArenas.firstOrNull { e -> e.name.equals(argument, true) } == null
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return MCTennisLanguage.gameAlreadyExistsMessage.format(openArgs[0])
        }
    }
    private val gameMustExistValidator = object : Validator<TennisArena> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): TennisArena? {
            val existingArenas = arenaRepository.getAll()
            return existingArenas.firstOrNull { e -> e.name.equals(openArgs[0], true) }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return MCTennisLanguage.gameDoesNotExistMessage.format(openArgs[0])
        }
    }
    private val teamValidator = object : Validator<Team> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): Team? {
            return try {
                Team.valueOf(openArgs[0].uppercase(Locale.ENGLISH))
            } catch (e: Exception) {
                return null
            }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return MCTennisLanguage.teamDoesNotExistMessage.format(openArgs[0])
        }
    }

    private val teamMetaValidator = object : Validator<TeamMetadata> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): TeamMetadata? {
            val team: Team = try {
                Team.valueOf(openArgs[0].uppercase(Locale.ENGLISH))
            } catch (e: Exception) {
                return null
            }
            val arena = prevArgs[prevArgs.size - 1] as TennisArena
            val teamMeta = if (team == Team.RED) {
                arena.redTeamMeta
            } else {
                arena.blueTeamMeta
            }
            return teamMeta
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return MCTennisLanguage.teamDoesNotExistMessage.format(openArgs[0])
        }
    }

    private val locationTypeValidator = object : Validator<LocationType> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): LocationType? {
            return LocationType.values().firstOrNull { e -> e.id.equals(openArgs[0], true) }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return MCTennisLanguage.locationTypeDoesNotExistMessage
        }
    }
    private val signTypeValidator = object : Validator<String> {
        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return MCTennisLanguage.signTypeDoesNotExist
        }

        override suspend fun validate(
            sender: CommandSender,
            prevArgs: List<Any>,
            argument: String,
            openArgs: List<String>
        ): Boolean {
            return argument.equals("join", true) || argument.equals("leave", true)
        }
    }

    init {
        val mcCart = CommandBuilder(plugin, coroutineExecutor, "mctennis", chatMessageService) {
            usage(MCTennisLanguage.commandUsage.translateChatColors())
            description(MCTennisLanguage.commandDescription)
            aliases(plugin.config.getStringList("commands.mctennis.aliases"))
            permission(Permission.COMMAND)
            permissionMessage(MCTennisLanguage.noPermissionMessage.translateChatColors())
            subCommand("create") {
                permission(Permission.EDIT_GAME)
                builder().argument("name").validator(maxLengthValidator).validator(maxLengthValidator)
                    .validator(gameMustNotExistValidator).tabs { listOf("<name>") }.argument("displayName")
                    .validator(remainingStringValidator).tabs { listOf("<displayName>") }
                    .execute { sender, name, displayName -> createArena(sender, name, displayName) }
            }
            subCommand("delete") {
                permission(Permission.EDIT_GAME)
                builder().argument("name").validator(gameMustExistValidator).tabs(arenaTabs)
                    .execute { sender, arena -> deleteArena(sender, arena) }
            }
            subCommand("list") {
                permission(Permission.EDIT_GAME)
                builder().execute { sender -> listArena(sender) }
            }
            subCommand("toggle") {
                permission(Permission.EDIT_GAME)
                builder().argument("name").validator(gameMustExistValidator).tabs(arenaTabs)
                    .execute { sender, arena -> toggleGame(sender, arena) }
            }
            subCommand("join") {
                noPermission()
                builder().argument("name").validator(gameMustExistValidator).tabs(arenaTabs)
                    .executePlayer({ MCTennisLanguage.commandSenderHasToBePlayer }) { sender, arena ->
                        joinGame(
                            sender, arena.name
                        )
                    }.argument("team").validator(teamValidator).tabs { listOf("red", "blue") }
                    .executePlayer({ MCTennisLanguage.commandSenderHasToBePlayer }) { sender, arena, team ->
                        joinGame(sender, arena.name, team)
                    }
            }
            subCommand("leave") {
                noPermission()
                builder().executePlayer({ MCTennisLanguage.commandSenderHasToBePlayer }) { sender -> leaveGame(sender) }
            }
            helpCommand()
            subCommand("location") {
                permission(Permission.EDIT_GAME)
                builder().argument("name").validator(gameMustExistValidator).tabs(arenaTabs)
                    .argument("type").validator(locationTypeValidator).tabs { LocationType.values().map { e -> e.id } }
                    .executePlayer({ MCTennisLanguage.commandSenderHasToBePlayer }) { player, arena, locationType ->
                        setLocation(player, arena, locationType)
                    }
            }
            subCommand("inventory") {
                permission(Permission.EDIT_GAME)
                builder().argument("name").validator(gameMustExistValidator).tabs(arenaTabs)
                    .argument("team").validator(teamMetaValidator).tabs { listOf("red", "blue") }
                    .executePlayer({ MCTennisLanguage.commandSenderHasToBePlayer }) { player, arena, meta ->
                        setInventory(player, arena, meta)
                    }
            }
            subCommand("armor") {
                permission(Permission.EDIT_GAME)
                builder().argument("name").validator(gameMustExistValidator).tabs(arenaTabs)
                    .argument("team").validator(teamMetaValidator).tabs { listOf("red", "blue") }
                    .executePlayer({ MCTennisLanguage.commandSenderHasToBePlayer }) { player, arena, meta ->
                        setArmor(player, arena, meta)
                    }
            }
            subCommand("sign") {
                permission(Permission.EDIT_GAME)
                builder().argument("name").validator(gameMustExistValidator).tabs(arenaTabs)
                    .argument("type").validator(signTypeValidator).tabs { listOf("join", "leave") }
                    .executePlayer({ MCTennisLanguage.commandSenderHasToBePlayer }) { player, arena, signType ->
                        setSign(player, arena, signType)
                    }
            }
            subCommand("reload") {
                permission(Permission.EDIT_GAME)
                builder()
                    .execute { sender ->
                        reloadArena(sender, null)
                    }
                    .argument("name").validator(gameMustExistValidator).tabs(arenaTabs)
                    .execute { sender, arena ->
                        reloadArena(sender, arena)
                    }
            }

        }
        mcCart.build()
    }

    private suspend fun createArena(sender: CommandSender, name: String, displayName: String) {
        if (arenaRepository.getAll().size > 0 && !MCTennisDependencyInjectionModule.areLegacyVersionsIncluded) {
            sender.sendMessage(MCTennisLanguage.freeVersionMessage)
            return
        }

        val arena = TennisArena()
        arena.name = name
        arena.displayName = displayName
        arenaRepository.save(arena)
        sender.sendMessage(MCTennisLanguage.gameCreatedMessage.format(name))
    }

    private suspend fun deleteArena(sender: CommandSender, arena: TennisArena) {
        val runningGame = gameService.getAll().firstOrNull { e -> e.arena.name.equals(arena.name, true) }
        runningGame?.dispose(false)
        arenaRepository.delete(arena)
        sender.sendMessage(MCTennisLanguage.deletedGameMessage.format(arena.name))
    }

    private suspend fun toggleGame(sender: CommandSender, arena: TennisArena) {
        try {
            arena.isEnabled = !arena.isEnabled
            gameService.reload(arena)
            sender.sendMessage(MCTennisLanguage.enabledArenaMessage.format(arena.isEnabled.toString()))
        } catch (e: TennisGameException) {
            arena.isEnabled = !arena.isEnabled
            sender.sendMessage(fallBackPrefix + ChatColor.RED.toString() + "Failed to reload arena ${e.arena.name}.")
            sender.sendMessage(fallBackPrefix + e.message)
            return
        }
        arenaRepository.save(arena)
        sender.sendMessage(MCTennisLanguage.reloadedGameMessage.format(arena.name))
    }

    private suspend fun setInventory(player: Player, arena: TennisArena, teamMetadata: TeamMetadata) {
        teamMetadata.inventoryContents = player.inventory.contents.clone().map { e ->
            val yamlConfiguration = YamlConfiguration()
            yamlConfiguration.set("item", e)
            yamlConfiguration.saveToString()
        }.toTypedArray()
        arenaRepository.save(arena)
        player.sendMessage(MCTennisLanguage.updatedInventoryMessage)
    }

    private suspend fun setArmor(player: Player, arena: TennisArena, teamMeta: TeamMetadata) {
        teamMeta.armorInventoryContents = player.inventory.armorContents.clone().map { e ->
            val yamlConfiguration = YamlConfiguration()
            yamlConfiguration.set("item", e)
            yamlConfiguration.saveToString()
        }.toTypedArray()
        arenaRepository.save(arena)
        player.sendMessage(MCTennisLanguage.updatedArmorMessage)
    }

    private fun CommandBuilder.permission(permission: Permission) {
        this.permission(permission.permission)
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
            if (MCTennisDependencyInjectionModule.areLegacyVersionsIncluded) {
                player.sendMessage(MCTennisLanguage.gameDoesNotExistMessage.format(name))
            } else {
                player.sendMessage(MCTennisLanguage.freeVersionMessage)
            }
            return
        }

        if (!player.hasPermission("mctennis.join.${game.arena.name}") && !player.hasPermission("mctennis.join.*")) {
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

    private suspend fun setLocation(player: Player, arena: TennisArena, locationType: LocationType) {
        if (locationType == LocationType.LOBBY_RED) {
            arena.redTeamMeta.lobbySpawnpoint = player.location.toVector3d()
        } else if (locationType == LocationType.LOBBY_BLUE) {
            arena.blueTeamMeta.lobbySpawnpoint = player.location.toVector3d()
        } else if (locationType == LocationType.LEAVE) {
            arena.leaveSpawnpoint = player.location.toVector3d()
        } else if (locationType == LocationType.SPAWN_RED_1) {
            if (arena.redTeamMeta.spawnpoints.size == 0) {
                arena.redTeamMeta.spawnpoints.add(player.location.toVector3d())
            } else {
                arena.redTeamMeta.spawnpoints[0] = player.location.toVector3d()
            }
        } else if (locationType == LocationType.SPAWN_BLUE_1) {
            if (arena.blueTeamMeta.spawnpoints.size == 0) {
                arena.blueTeamMeta.spawnpoints.add(player.location.toVector3d())
            } else {
                arena.blueTeamMeta.spawnpoints[0] = player.location.toVector3d()
            }
        } else if (locationType == LocationType.CORNER_RED_1) {
            arena.redTeamMeta.leftLowerCorner = player.location.toVector3d()
        } else if (locationType == LocationType.CORNER_RED_2) {
            arena.redTeamMeta.rightUpperCorner = player.location.toVector3d()
        } else if (locationType == LocationType.CORNER_BLUE_1) {
            arena.blueTeamMeta.leftLowerCorner = player.location.toVector3d()
        } else if (locationType == LocationType.CORNER_BLUE_2) {
            arena.blueTeamMeta.rightUpperCorner = player.location.toVector3d()
        } else {
            player.sendMessage(MCTennisLanguage.locationTypeDoesNotExistMessage.format(player.location))
            return
        }

        arenaRepository.save(arena)
        player.sendMessage(MCTennisLanguage.spawnPointSetMessage.format(player.location))
    }

    private suspend fun setSign(sender: Player, arena: TennisArena, signType: String) {
        if (signType.equals("join", true)) {
            sender.sendMessage(MCTennisLanguage.rightClickOnSignMessage)
            signService.addSignByRightClick(sender) { sign ->
                sign.let {
                    it.line1 = "%mctennis_lang_joinSignLine1%"
                    it.line2 = "%mctennis_lang_joinSignLine2%"
                    it.line3 = "%mctennis_lang_joinSignLine3%"
                    it.line4 = "%mctennis_lang_joinSignLine4%"
                    it.cooldown = 20
                    it.update = 40
                    it.commands = mutableListOf(CommandMeta().also {
                        it.command = "/mctennis join ${arena.name}"
                        it.type = CommandType.PER_PLAYER
                    })
                }

                if (arena.signs.firstOrNull { e -> e.isSameSign(sign) } == null) {
                    arena.signs.add(sign)
                }

                plugin.launch {
                    arenaRepository.save(arena)
                    gameService.reload(arena)
                    sender.sendMessage(MCTennisLanguage.addedSignMessage)
                }
            }
        } else if (signType.equals("leave", true)) {
            sender.sendMessage(MCTennisLanguage.rightClickOnSignMessage)
            signService.addSignByRightClick(sender) { sign ->
                sign.let {
                    it.line1 = "%mctennis_lang_leaveSignLine1%"
                    it.line2 = "%mctennis_lang_leaveSignLine2%"
                    it.line3 = "%mctennis_lang_leaveSignLine3%"
                    it.line4 = "%mctennis_lang_leaveSignLine4%"
                    it.cooldown = 20
                    it.update = 40
                    it.commands = mutableListOf(CommandMeta().also {
                        it.command = "/mctennis leave"
                        it.type = CommandType.PER_PLAYER
                    })
                }

                if (arena.signs.firstOrNull { e -> e.isSameSign(sign) } == null) {
                    arena.signs.add(sign)
                }

                plugin.launch {
                    arenaRepository.save(arena)
                    gameService.reload(arena)
                    sender.sendMessage(MCTennisLanguage.addedSignMessage)
                }
            }
        } else {
            sender.sendMessage(MCTennisLanguage.signTypeDoesNotExist)
        }
    }


    private suspend fun reloadArena(sender: CommandSender, arena: TennisArena?) {
        try {
            arenaRepository.clearCache()
        } catch (e: TennisGameException) {
            sender.sendMessage(fallBackPrefix + ChatColor.RED.toString() + "Failed to reload arenas.")
            sender.sendMessage(fallBackPrefix + e.message)
            return
        }

        if (arena == null) {
            plugin.reloadConfig()
            val language = configurationService.findValue<String>("language")
            plugin.reloadTranslation(language, MCTennisLanguage::class.java, "en_us")
            plugin.logger.log(Level.INFO, "Loaded language file $language.properties.")

            try {
                arenaRepository.clearCache()
                gameService.reloadAll()
            } catch (e: TennisGameException) {
                sender.sendMessage(fallBackPrefix + ChatColor.RED.toString() + "Failed to reload arena ${e.arena.name}.")
                sender.sendMessage(fallBackPrefix + e.message)
                return
            }

            sender.sendMessage(MCTennisLanguage.reloadedAllGamesMessage)
            return
        }

        try {
            arenaRepository.clearCache()
            gameService.reload(arena)
        } catch (e: TennisGameException) {
            sender.sendMessage(fallBackPrefix + ChatColor.RED.toString() + "Failed to reload arena ${e.arena.name}.")
            sender.sendMessage(fallBackPrefix + e.message)
            return
        }
        sender.sendMessage(MCTennisLanguage.reloadedGameMessage.format(arena.name))
        return
    }
}
