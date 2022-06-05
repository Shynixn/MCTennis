package com.github.shynixn.mctennis

import com.fasterxml.jackson.core.type.TypeReference
import com.github.shynixn.mctennis.contract.DependencyPlaceholderApiService
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.impl.service.DependencyPlaceholderApiServiceImpl
import com.github.shynixn.mctennis.impl.service.GameServiceImpl
import com.github.shynixn.mcutils.arena.api.ArenaRepository
import com.github.shynixn.mcutils.arena.api.CacheArenaRepository
import com.github.shynixn.mcutils.arena.impl.CachedArenaRepositoryImpl
import com.github.shynixn.mcutils.arena.impl.YamlFileArenaRepositoryImpl
import com.github.shynixn.mcutils.ball.api.BallService
import com.github.shynixn.mcutils.ball.impl.BallServiceImpl
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.ConfigurationServiceImpl
import com.github.shynixn.mcutils.common.ItemService
import com.github.shynixn.mcutils.common.ItemServiceImpl
import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.TypeLiteral
import org.bukkit.plugin.Plugin


class MCTennisDependencyInjectionBinder(private val plugin: MCTennisPlugin) : AbstractModule() {
    /**
     * Configures the business logic tree.
     */
    override fun configure() {
        bind(Plugin::class.java).toInstance(plugin)

        // Repositories
        val tennisArenaRepository = YamlFileArenaRepositoryImpl<TennisArena>(
            plugin,
            "arena",
            "arena_sample.yml",
            object : TypeReference<TennisArena>() {})
        val cacheTennisArenaRepository = CachedArenaRepositoryImpl(tennisArenaRepository)
        bind(object : TypeLiteral<ArenaRepository<TennisArena>>() {}).toInstance(cacheTennisArenaRepository)
        bind(object : TypeLiteral<CacheArenaRepository<TennisArena>>() {}).toInstance(cacheTennisArenaRepository)
        bind(ArenaRepository::class.java).toInstance(cacheTennisArenaRepository)
        bind(CacheArenaRepository::class.java).toInstance(cacheTennisArenaRepository)

        // Services
        bind(ConfigurationService::class.java).toInstance(ConfigurationServiceImpl(plugin))
        bind(BallService::class.java).toInstance(BallServiceImpl(plugin))
        bind(ItemService::class.java).toInstance(ItemServiceImpl())
        bind(GameService::class.java).to(GameServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(DependencyPlaceholderApiService::class.java).to(DependencyPlaceholderApiServiceImpl::class.java).`in`(Scopes.SINGLETON)
    }
}
