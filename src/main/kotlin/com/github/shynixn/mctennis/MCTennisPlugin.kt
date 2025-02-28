package com.github.shynixn.mctennis

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.contract.TennisBallFactory
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.PlaceHolder
import com.github.shynixn.mctennis.impl.commandexecutor.MCTennisCommandExecutor
import com.github.shynixn.mctennis.impl.exception.TennisGameException
import com.github.shynixn.mctennis.impl.listener.GameListener
import com.github.shynixn.mctennis.impl.listener.PacketListener
import com.github.shynixn.mctennis.impl.listener.TennisListener
import com.github.shynixn.mcutils.common.Version
import com.github.shynixn.mcutils.common.di.DependencyInjectionModule
import com.github.shynixn.mcutils.common.language.reloadTranslation
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.common.repository.Repository
import com.github.shynixn.mcutils.packet.api.PacketInType
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.sign.SignService
import com.github.shynixn.shyscoreboard.ShyScoreboardDependencyInjectionModule
import com.github.shynixn.shyscoreboard.contract.ScoreboardService
import com.github.shynixn.shyscoreboard.contract.ShyScoreboardLanguage
import com.github.shynixn.shyscoreboard.entity.ShyScoreboardSettings
import com.github.shynixn.shyscoreboard.impl.commandexecutor.ShyScoreboardCommandExecutor
import com.github.shynixn.shyscoreboard.impl.listener.ShyScoreboardListener
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class MCTennisPlugin : JavaPlugin() {
    companion object {
        var gameKey = "[game]"
        var languageKey = "language"
    }

    private val prefix: String = org.bukkit.ChatColor.BLUE.toString() + "[MCTennis] " + org.bukkit.ChatColor.WHITE
    private var module: DependencyInjectionModule? = null
    private var scoreboardModule: DependencyInjectionModule? = null

    /**
     * Called when this plugin is enabled.
     */
    override fun onEnable() {
        Bukkit.getServer().consoleSender.sendMessage(prefix + ChatColor.GREEN + "Loading MCTennis ...")
        this.saveDefaultConfig()
        val versions = if (MCTennisDependencyInjectionModule.areLegacyVersionsIncluded) {
            listOf(
                Version.VERSION_1_8_R3,
                Version.VERSION_1_9_R2,
                Version.VERSION_1_10_R1,
                Version.VERSION_1_11_R1,
                Version.VERSION_1_12_R1,
                Version.VERSION_1_13_R1,
                Version.VERSION_1_13_R2,
                Version.VERSION_1_14_R1,
                Version.VERSION_1_15_R1,
                Version.VERSION_1_16_R1,
                Version.VERSION_1_16_R2,
                Version.VERSION_1_16_R3,
                Version.VERSION_1_17_R1,
                Version.VERSION_1_18_R1,
                Version.VERSION_1_18_R2,
                Version.VERSION_1_19_R1,
                Version.VERSION_1_19_R2,
                Version.VERSION_1_19_R3,
                Version.VERSION_1_20_R1,
                Version.VERSION_1_20_R2,
                Version.VERSION_1_20_R3,
                Version.VERSION_1_20_R4,
                Version.VERSION_1_21_R1,
                Version.VERSION_1_21_R2,
                Version.VERSION_1_21_R3
            )
        } else {
            listOf(Version.VERSION_1_21_R3)
        }

        if (!Version.serverVersion.isCompatible(*versions.toTypedArray())) {
            logger.log(Level.SEVERE, "================================================")
            logger.log(Level.SEVERE, "MCTennis does not support your server version")
            logger.log(Level.SEVERE, "Install v" + versions[0].from + " - v" + versions[versions.size - 1].to)
            logger.log(Level.SEVERE, "Need support for a particular version? Go to https://www.patreon.com/Shynixn")
            logger.log(Level.SEVERE, "Plugin gets now disabled!")
            logger.log(Level.SEVERE, "================================================")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        logger.log(Level.INFO, "Loaded NMS version ${Version.serverVersion}.")

        // Load MCTennisLanguage
        val language = MCTennisLanguageImpl()
        reloadTranslation(language)
        logger.log(Level.INFO, "Loaded language file.")

        // Module
        this.scoreboardModule = loadShyScoreboardModule(language)
        this.module = MCTennisDependencyInjectionModule(this, language, this.scoreboardModule!!.getService()).build()

        // Register PlaceHolder
        PlaceHolder.registerAll(module!!.getService(), module!!.getService(), language)

        // Register Packet
        module!!.getService<PacketService>().registerPacketListening(PacketInType.USEENTITY)

        // Register Listeners
        Bukkit.getPluginManager().registerEvents(module!!.getService<GameListener>(), this)
        Bukkit.getPluginManager().registerEvents(module!!.getService<PacketListener>(), this)
        Bukkit.getPluginManager().registerEvents(module!!.getService<TennisListener>(), this)

        // Register CommandExecutor
        module!!.getService<MCTennisCommandExecutor>()

        // Service dependencies
        Bukkit.getServicesManager().register(
            TennisBallFactory::class.java,
            module!!.getService<TennisBallFactory>(),
            this,
            ServicePriority.Normal
        )
        Bukkit.getServicesManager()
            .register(GameService::class.java, module!!.getService<GameService>(), this, ServicePriority.Normal)

        val plugin = this
        plugin.launch {
            // Load Games
            val gameService = module!!.getService<GameService>()
            try {
                gameService.reloadAll()
            } catch (e: TennisGameException) {
                plugin.logger.log(Level.WARNING, "Cannot start game of tennisArena ${e.arena.name}.", e)
            }

            // Load Signs
            val placeHolderService = module!!.getService<PlaceHolderService>()
            val signService = module!!.getService<SignService>()
            val arenaService = module!!.getService<Repository<TennisArena>>()
            signService.onSignDestroy = { signMeta ->
                plugin.launch {
                    val arenas = arenaService.getAll()
                    for (arena in arenas) {
                        for (signToRemove in arena.signs.filter { e -> e.isSameSign(signMeta) }) {
                            arena.signs.remove(signToRemove)
                            arenaService.save(arena)
                        }
                    }
                }
            }
            signService.onPlaceHolderResolve = { signMeta, text ->
                var resolvedText: String? = null

                if (signMeta.tag != null) {
                    val game = gameService.getByName(signMeta.tag!!)
                    if (game != null) {
                        resolvedText =
                            placeHolderService.resolvePlaceHolder(text, null, mapOf(gameKey to game.arena.name))
                    }
                }

                if (resolvedText == null) {
                    resolvedText = placeHolderService.resolvePlaceHolder(text, null)
                }

                resolvedText
            }
            Bukkit.getServer().consoleSender.sendMessage(prefix + ChatColor.GREEN + "Enabled MCTennis " + plugin.description.version + " by Shynixn")
        }
    }

    /**
     * Called when this plugin is disabled
     */
    override fun onDisable() {
        scoreboardModule?.close()
        module?.close()
    }

    private fun loadShyScoreboardModule(language: ShyScoreboardLanguage): DependencyInjectionModule {
        val settings = ShyScoreboardSettings({ s ->
            s.joinDelaySeconds = config.getInt("scoreboard.joinDelaySeconds")
            s.checkForPermissionChangeSeconds = config.getInt("scoreboard.checkForPermissionChangeSeconds")
            s.baseCommand = "mctennisscoreboard"
            s.commandAliases = config.getStringList("commands.mctennisscoreboard.aliases")
            s.commandPermission = "mctennis.shyscoreboard.command"
            s.reloadPermission = "mctennis.shyscoreboard.reload"
            s.dynScoreboardPermission = "mctennis.shyscoreboard.scoreboard."
            s.addPermission = "mctennis.shyscoreboard.add"
            s.removePermission = "mctennis.shyscoreboard.remove"
            s.updatePermission = "mctennis.shyscoreboard.update"
            s.defaultScoreboards = listOf(
                "scoreboard/mctennis_scoreboard.yml" to "mctennis_scoreboard.yml"
            )
        })
        settings.reload()
        val module = ShyScoreboardDependencyInjectionModule(this, settings, language).build()

        // Register PlaceHolders
        com.github.shynixn.shyscoreboard.enumeration.PlaceHolder.registerAll(
            this,
            module.getService<PlaceHolderService>(),
        )

        // Register Listeners
        Bukkit.getPluginManager().registerEvents(module.getService<ShyScoreboardListener>(), this)

        // Register CommandExecutor
        module.getService<ShyScoreboardCommandExecutor>()
        val scoreboardService = module.getService<ScoreboardService>()
        launch {
            scoreboardService.reload()
        }

        return module
    }
}
