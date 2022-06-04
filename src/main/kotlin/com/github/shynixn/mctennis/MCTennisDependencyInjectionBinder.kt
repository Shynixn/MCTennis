package com.github.shynixn.mctennis

import com.fasterxml.jackson.core.type.TypeReference
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mcutils.arena.api.ArenaRepository
import com.github.shynixn.mcutils.arena.api.CacheArenaRepository
import com.github.shynixn.mcutils.arena.impl.CachedArenaRepositoryImpl
import com.github.shynixn.mcutils.arena.impl.YamlFileArenaRepositoryImpl
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.ConfigurationServiceImpl
import com.google.inject.AbstractModule
import com.google.inject.Scopes
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
            "game",
            "arena_sample.yml",
            object : TypeReference<TennisArena>() {})
        val cacheTennisArenaRepository = CachedArenaRepositoryImpl(tennisArenaRepository)
        bind(ArenaRepository::class.java).toInstance(cacheTennisArenaRepository)
        bind(CacheArenaRepository::class.java).toInstance(cacheTennisArenaRepository)

        // Services
        bind(ConfigurationService::class.java).to(ConfigurationServiceImpl::class.java).`in`(Scopes.SINGLETON)
    }
}
