package com.github.shynixn.mctennis

import com.fasterxml.jackson.core.type.TypeReference
import com.github.shynixn.mctennis.contract.*
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.impl.service.*
import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.common.arena.ArenaRepository
import com.github.shynixn.mcutils.common.arena.CacheArenaRepository
import com.github.shynixn.mcutils.common.arena.CachedArenaRepositoryImpl
import com.github.shynixn.mcutils.common.arena.YamlFileArenaRepositoryImpl
import com.github.shynixn.mcutils.common.command.CommandService
import com.github.shynixn.mcutils.common.command.CommandServiceImpl
import com.github.shynixn.mcutils.common.item.ItemService
import com.github.shynixn.mcutils.common.item.ItemServiceImpl
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcher
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcherImpl
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.common.physic.PhysicObjectServiceImpl
import com.github.shynixn.mcutils.common.sound.SoundService
import com.github.shynixn.mcutils.common.sound.SoundServiceImpl
import com.github.shynixn.mcutils.packet.api.EntityService
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.api.RayTracingService
import com.github.shynixn.mcutils.packet.impl.service.EntityServiceImpl
import com.github.shynixn.mcutils.packet.impl.service.PacketServiceImpl
import com.github.shynixn.mcutils.packet.impl.service.RayTracingServiceImpl
import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.TypeLiteral
import org.bukkit.plugin.Plugin

class MCTennisDependencyInjectionBinder(private val plugin: Plugin) : AbstractModule() {
    companion object {
        val areLegacyVersionsIncluded : Boolean  by lazy {
            try {
                Class.forName("com.github.shynixn.mctennis.lib.com.github.shynixn.mcutils.packet.nms.v1_8_R3.PacketSendServiceImpl")
                true
            } catch (e: ClassNotFoundException) {
                false
            }
        }
    }

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
        val physicObjectDispatcher = PhysicObjectDispatcherImpl(plugin)
        bind(EntityService::class.java).toInstance(EntityServiceImpl())
        bind(RayTracingService::class.java).toInstance(RayTracingServiceImpl())
        bind(PacketService::class.java).toInstance(PacketServiceImpl(plugin))
        bind(PhysicObjectDispatcher::class.java).toInstance(physicObjectDispatcher)
        bind(ConfigurationService::class.java).toInstance(ConfigurationServiceImpl(plugin))
        bind(PhysicObjectService::class.java).toInstance(PhysicObjectServiceImpl(plugin, physicObjectDispatcher))
        bind(ItemService::class.java).toInstance(ItemServiceImpl())
        bind(BedrockService::class.java).to(BedrockServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(GameService::class.java).to(GameServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(SoundService::class.java).toInstance(SoundServiceImpl(plugin))
        bind(CommandService::class.java).to(CommandServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(TennisBallFactory::class.java).to(TennisBallFactoryImpl::class.java).`in`(Scopes.SINGLETON)
    }
}
