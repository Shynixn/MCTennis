package com.github.shynixn.mctennis

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mctennis.impl.TennisGame
import com.github.shynixn.mcutils.ConfigurationService
import com.github.shynixn.mcutils.Version
import com.github.shynixn.mcutils.reloadTranslation
import com.google.inject.Guice
import com.google.inject.Injector
import com.sun.org.apache.xpath.internal.operations.Neg
import org.bukkit.Bukkit
import org.bukkit.ChatColor

class MCTennisPlugin : SuspendingJavaPlugin() {
    companion object {
        private val prefix: String = ChatColor.BLUE.toString() + "[MCTennis] " + ChatColor.WHITE
    }

    private var injector: Injector? = null

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
        this.injector = Guice.createInjector(MCTennisDependencyInjectionBinder(this))
        this.reloadConfig()
        val configurationService = resolve(ConfigurationService::class.java)

        // Register Listeners
        // Bukkit.getPluginManager().registerEvents(resolve(BallListener::class.java), this)
        //   Bukkit.getPluginManager().registerEvents(resolve(PlayerDataListener::class.java), this)

        // Register CommandExecutor
        //  getCommand("lobbyballsreload")!!.setSuspendingExecutor(resolve(ReloadCommandExecutor::class.java))

        /*   val playerDataCommandExecutor = resolve(PlayerBallCommandExecutor::class.java)
           val lobbyBallsCommand = this.getCommand("lobbyballs")!!
           lobbyBallsCommand.aliases = configurationService.findValue("commands.lobbyballs.aliases")
           lobbyBallsCommand.usage = configurationService.findValue("commands.lobbyballs.usage")
           lobbyBallsCommand.description = configurationService.findValue("commands.lobbyballs.description")
           lobbyBallsCommand.permissionMessage = configurationService.findValue("commands.lobbyballs.permission-message")
           lobbyBallsCommand.setSuspendingExecutor(playerDataCommandExecutor)
           lobbyBallsCommand.setSuspendingTabCompleter(playerDataCommandExecutor)*/

        // Register Dependencies.
        /* val dependencyService = resolve(DependencyService::class.java)
         dependencyService.checkForInstalledDependencies()

         if (dependencyService.isInstalled(PluginDependency.PLACEHOLDERAPI)) {
             val placeHolderService = resolve(DependencyPlaceholderApiService::class.java)
             placeHolderService.registerListener()
         }*/



        this.reloadTranslation("en_us", MCTennisLanguage::class.java, "en_us", "de_de")
        Bukkit.getServer()
            .consoleSender.sendMessage(prefix + ChatColor.GREEN + "Enabled MCTennis " + this.description.version + " by Shynixn")
    }

    /**
     * Gets a business logic from the LobbyBalls plugin.
     * All types in the service package can be accessed.
     * Throws a [IllegalArgumentException] if the service could not be found.
     */
    private fun <S> resolve(service: Class<S>): S {
        try {
            return this.injector!!.getBinding(service).provider.get() as S
        } catch (e: Exception) {
            throw IllegalArgumentException("Service could not be resolved.", e)
        }
    }
}
