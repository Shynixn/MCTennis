package com.github.shynixn.mctennis

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.contract.BedrockService
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.contract.PlaceHolderService
import com.github.shynixn.mctennis.contract.TennisBallFactory
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.PluginDependency
import com.github.shynixn.mctennis.impl.commandexecutor.MCTennisCommandExecutor
import com.github.shynixn.mctennis.impl.listener.GameListener
import com.github.shynixn.mctennis.impl.listener.PacketListener
import com.github.shynixn.mctennis.impl.listener.TennisListener
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.Version
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.common.reloadTranslation
import com.github.shynixn.mcutils.common.repository.Repository
import com.github.shynixn.mcutils.packet.api.PacketInType
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.sign.SignService
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.TypeLiteral
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class MCTennisPlugin : JavaPlugin() {
    private val prefix: String = org.bukkit.ChatColor.BLUE.toString() + "[MCTennis] " + org.bukkit.ChatColor.WHITE
    private var injector: Injector? = null

    /**
     * Called when this plugin is enabled.
     */
    override fun onEnable() {
        Bukkit.getServer().consoleSender.sendMessage(prefix + ChatColor.GREEN + "Loading MCTennis ...")
        this.saveDefaultConfig()

        val versions = if (MCTennisDependencyInjectionBinder.areLegacyVersionsIncluded) {
            listOf(
                Version.VERSION_1_17_R1,
                Version.VERSION_1_18_R1,
                Version.VERSION_1_18_R2,
                Version.VERSION_1_19_R1,
                Version.VERSION_1_19_R2,
                Version.VERSION_1_19_R3,
                Version.VERSION_1_20_R1,
                Version.VERSION_1_20_R2,
                Version.VERSION_1_20_R3
            )
        } else {
            listOf(Version.VERSION_1_20_R3)
        }

        if (!Version.serverVersion.isCompatible(*versions.toTypedArray())) {
            logger.log(Level.SEVERE, "================================================")
            logger.log(Level.SEVERE, "MCTennis does not support your server version")
            logger.log(Level.SEVERE, "Install v" + versions[0].id + " - v" + versions[versions.size - 1].id)
            logger.log(Level.SEVERE, "Need support for a particular version? Go to https://www.patreon.com/Shynixn")
            logger.log(Level.SEVERE, "Plugin gets now disabled!")
            logger.log(Level.SEVERE, "================================================")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        // Guice
        this.injector = Guice.createInjector(MCTennisDependencyInjectionBinder(this))
        this.reloadConfig()

        resolve(PacketService::class.java).registerPacketListening(PacketInType.USEENTITY)

        // Register Listeners
        Bukkit.getPluginManager().registerEvents(resolve(GameListener::class.java), this)
        Bukkit.getPluginManager().registerEvents(resolve(TennisListener::class.java), this)
        Bukkit.getPluginManager().registerEvents(resolve(PacketListener::class.java), this)
        Bukkit.getPluginManager().registerEvents(resolve(BedrockService::class.java), this)

        // Register CommandExecutor
        resolve(MCTennisCommandExecutor::class.java)

        // Register Dependencies
        if (Bukkit.getPluginManager().getPlugin(PluginDependency.GEYSER_SPIGOT.pluginName) != null) {
            logger.log(Level.INFO, "Loaded dependency ${PluginDependency.GEYSER_SPIGOT.pluginName}.")
        }

        // Service dependencies
        resolve(MCTennisCommandExecutor::class.java)
        Bukkit.getServicesManager().register(
            TennisBallFactory::class.java,
            resolve(TennisBallFactory::class.java),
            this,
            ServicePriority.Normal
        )
        Bukkit.getServicesManager()
            .register(GameService::class.java, resolve(GameService::class.java), this, ServicePriority.Normal)

        val plugin = this
        runBlocking {
            // Load Language
            val configurationService = resolve(ConfigurationService::class.java)
            val language = configurationService.findValue<String>("language")
            reloadTranslation(language, MCTennisLanguage::class.java, "en_us")
            logger.log(Level.INFO, "Loaded language file $language.properties.")

            // Load Games
            val gameService = resolve(GameService::class.java)
            gameService.reloadAll()

            // Load Signs
            val placeHolderService = resolve(PlaceHolderService::class.java)
            val signService = resolve(SignService::class.java)
            val arenaService =
                injector!!.getBinding(Key.get(object : TypeLiteral<Repository<TennisArena>>() {})).provider.get()
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
                        resolvedText = placeHolderService.replacePlaceHolders(text, null, game)
                    }
                }

                if (resolvedText == null) {
                    resolvedText = placeHolderService.replacePlaceHolders(text)
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
        if (injector == null) {
            return
        }

        val packetService = resolve(PacketService::class.java)
        packetService.close()
        val physicObjectService = resolve(PhysicObjectService::class.java)
        physicObjectService.close()
        val gameService = resolve(GameService::class.java)
        gameService.close()
    }

    /**
     * Gets a business logic from the MCTennis plugin.
     * All types in the service package can be accessed.
     * Throws a [IllegalArgumentException] if the service could not be found.
     */
    private fun <S> resolve(service: Class<S>): S {
        try {
            return this.injector!!.getBinding(service).provider.get() as S
        } catch (e: Exception) {
            throw IllegalArgumentException("Service ${service.name} could not be resolved.", e)
        }
    }
}
