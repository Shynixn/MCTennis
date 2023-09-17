package com.github.shynixn.mctennis

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
import com.github.shynixn.mccoroutine.bukkit.setSuspendingTabCompleter
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.contract.PhysicObjectService
import com.github.shynixn.mctennis.enumeration.PluginDependency
import com.github.shynixn.mctennis.impl.commandexecutor.MCTennisCommandExecutor
import com.github.shynixn.mctennis.impl.listener.DoubleJumpListener
import com.github.shynixn.mctennis.impl.listener.GameListener
import com.github.shynixn.mctennis.impl.listener.TennisListener
import com.github.shynixn.mctennis.impl.physic.PhysicObjectDispatcher
import com.github.shynixn.mctennis.impl.service.DependencyPlaceholderApiServiceImpl
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.Version
import com.github.shynixn.mcutils.common.reloadTranslation
import com.google.inject.Guice
import com.google.inject.Injector
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.logging.Level

class MCTennisPlugin : SuspendingJavaPlugin() {
    companion object {
        val prefix: String = ChatColor.BLUE.toString() + "[MCTennis] " + ChatColor.WHITE
    }

    private var injector: Injector? = null
    private var physicObjectDispatcher = PhysicObjectDispatcher(this)

    /**
     * Called when this plugin is enabled.
     */
    override suspend fun onEnableAsync() {
        Bukkit.getServer().consoleSender.sendMessage(prefix + ChatColor.GREEN + "Loading MCTennis ...")
        this.saveDefaultConfig()

        if (!Version.serverVersion.isCompatible(
                Version.VERSION_1_18_R2,
            )
        ) {
            Bukkit.getServer().consoleSender.sendMessage(ChatColor.RED.toString() + "================================================")
            Bukkit.getServer().consoleSender.sendMessage(ChatColor.RED.toString() + "MCTennis does not support your server version")
            Bukkit.getServer().consoleSender.sendMessage(ChatColor.RED.toString() + "Install v" + Version.VERSION_1_18_R2.id + " - v" + Version.VERSION_1_18_R2.id)
            Bukkit.getServer().consoleSender.sendMessage(ChatColor.RED.toString() + "Plugin gets now disabled!")
            Bukkit.getServer().consoleSender.sendMessage(ChatColor.RED.toString() + "================================================")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        // Guice
        this.injector = Guice.createInjector(MCTennisDependencyInjectionBinder(this, physicObjectDispatcher))
        this.reloadConfig()

        // Register Listeners
        Bukkit.getPluginManager().registerEvents(resolve(GameListener::class.java), this)
        Bukkit.getPluginManager().registerEvents(resolve(TennisListener::class.java), this)
        Bukkit.getPluginManager().registerEvents(resolve(DoubleJumpListener::class.java), this)

        // Register CommandExecutors
        val configurationService = resolve(ConfigurationService::class.java)
        val mcTennisCommandExecutor = resolve(MCTennisCommandExecutor::class.java)
        val mcTennisCommand = this.getCommand("mctennis")!!
        mcTennisCommand.aliases = configurationService.findValue("commands.mctennis.aliases")
        mcTennisCommand.usage = configurationService.findValue("commands.mctennis.usage")
        mcTennisCommand.description = configurationService.findValue("commands.mctennis.description")
        mcTennisCommand.permissionMessage = configurationService.findValue("commands.mctennis.permission-message")
        mcTennisCommand.setSuspendingExecutor(mcTennisCommandExecutor)
        mcTennisCommand.setSuspendingTabCompleter(mcTennisCommandExecutor)

        // Register Dependencies
        if (Bukkit.getPluginManager().getPlugin(PluginDependency.PLACEHOLDERAPI.pluginName) != null) {
            val placeHolderApi = DependencyPlaceholderApiServiceImpl(this, resolve(GameService::class.java))
            placeHolderApi.registerListener()
            logger.log(Level.INFO, "Loaded dependency ${PluginDependency.PLACEHOLDERAPI.pluginName}.")
        }

        val language = configurationService.findValue<String>("language")
        this.reloadTranslation(language, MCTennisLanguage::class.java, "en_us")
        logger.log(Level.INFO, "Loaded language file $language.properties.")

        val gameService = resolve(GameService::class.java)
        gameService.reloadAll()

        Bukkit.getServer()
            .consoleSender.sendMessage(prefix + ChatColor.GREEN + "Enabled MCTennis " + this.description.version + " by Shynixn")
    }

    /**
     * Called when this plugin is disabled
     */
    override fun onDisable() {
        val ballService = resolve(PhysicObjectService::class.java)
        ballService.close()
        val gameService = resolve(GameService::class.java)
        gameService.dispose()
        physicObjectDispatcher.close()
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
