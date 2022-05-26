package com.github.shynixn.mctennis

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import org.bukkit.plugin.Plugin

class MCTennisDependencyInjectionBinder(private val plugin: MCTennisPlugin) : AbstractModule() {
    /**
     * Configures the business logic tree.
     */
    override fun configure() {
        val dependencyService = DependencyServiceImpl()

        bind(Plugin::class.java).toInstance(plugin)

        // Repositories
        bind(BallSpawnpointRepository::class.java).to(BallSpawnpointRepositoryImpl::class.java).`in`(Scopes.SINGLETON)
        bind(BallTemplateRepository::class.java).to(BallTemplateRepositoryImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PlayerDataRepository::class.java).to(PlayerDataRepositoryImpl::class.java).`in`(Scopes.SINGLETON)

        // Services
        bind(BallService::class.java).to(BallServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ConfigurationService::class.java).to(ConfigurationServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(DependencyService::class.java).to(DependencyServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(BallApi::class.java).toInstance(ballApi)

        if (dependencyService.isInstalled(PluginDependency.PLACEHOLDERAPI)) {
            bind(DependencyPlaceholderApiService::class.java).to(DependencyPlaceholderApiServiceImpl::class.java)
        }
    }
}
