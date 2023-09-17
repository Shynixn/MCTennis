package com.github.shynixn.mctennis

import com.fasterxml.jackson.core.type.TypeReference
import com.github.shynixn.mctennis.contract.*
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.impl.physic.PhysicObjectDispatcher
import com.github.shynixn.mctennis.impl.service.*
import com.github.shynixn.mcutils.arena.api.ArenaRepository
import com.github.shynixn.mcutils.arena.api.CacheArenaRepository
import com.github.shynixn.mcutils.arena.impl.CachedArenaRepositoryImpl
import com.github.shynixn.mcutils.arena.impl.YamlFileArenaRepositoryImpl
import com.github.shynixn.mcutils.common.*
import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.TypeLiteral
import org.bukkit.plugin.Plugin

class MCTennisDependencyInjectionBinder(private val plugin: MCTennisPlugin, private val physicObjectDispatcher: PhysicObjectDispatcher) : AbstractModule() {
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
        bind(PhysicObjectDispatcher::class.java).toInstance(physicObjectDispatcher)
        bind(ConfigurationService::class.java).toInstance(ConfigurationServiceImpl(plugin))
        bind(PhysicObjectService::class.java).toInstance(PhysicObjectServiceImpl(plugin, physicObjectDispatcher))
        bind(ItemService::class.java).toInstance(ItemServiceImpl())
        bind(GameService::class.java).to(GameServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(SoundService::class.java).to(SoundServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CommandService::class.java).to(CommandServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(TennisBallFactory::class.java).to(TennisBallFactoryImpl::class.java).`in`(Scopes.SINGLETON)
    }
}
