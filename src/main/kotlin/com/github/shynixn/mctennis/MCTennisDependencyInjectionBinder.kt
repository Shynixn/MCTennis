package com.github.shynixn.mctennis

import com.fasterxml.jackson.core.type.TypeReference
import com.github.shynixn.mctennis.contract.*
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.PluginDependency
import com.github.shynixn.mctennis.impl.service.*
import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.common.command.CommandService
import com.github.shynixn.mcutils.common.command.CommandServiceImpl
import com.github.shynixn.mcutils.common.item.ItemService
import com.github.shynixn.mcutils.common.item.ItemServiceImpl
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcher
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcherImpl
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.common.physic.PhysicObjectServiceImpl
import com.github.shynixn.mcutils.common.repository.CacheRepository
import com.github.shynixn.mcutils.common.repository.CachedRepositoryImpl
import com.github.shynixn.mcutils.common.repository.Repository
import com.github.shynixn.mcutils.common.repository.YamlFileRepositoryImpl
import com.github.shynixn.mcutils.common.sound.SoundService
import com.github.shynixn.mcutils.common.sound.SoundServiceImpl
import com.github.shynixn.mcutils.packet.api.EntityService
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.api.RayTracingService
import com.github.shynixn.mcutils.packet.impl.service.EntityServiceImpl
import com.github.shynixn.mcutils.packet.impl.service.PacketServiceImpl
import com.github.shynixn.mcutils.packet.impl.service.RayTracingServiceImpl
import com.github.shynixn.mcutils.sign.SignService
import com.github.shynixn.mcutils.sign.SignServiceImpl
import com.google.inject.AbstractModule
import com.google.inject.Provider
import com.google.inject.Scopes
import com.google.inject.TypeLiteral
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.logging.Level

class MCTennisDependencyInjectionBinder(private val plugin: Plugin) : AbstractModule() {
    companion object {
        val areLegacyVersionsIncluded: Boolean by lazy {
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
        val tennisArenaRepository = YamlFileRepositoryImpl<TennisArena>(
            plugin,
            "arena",
            listOf(Pair("arena_sample.yml", "arena_sample.yml")),
            listOf("arena_sample.yml"),
            object : TypeReference<TennisArena>() {})
        val cacheTennisArenaRepository = CachedRepositoryImpl(tennisArenaRepository)
        bind(object : TypeLiteral<Repository<TennisArena>>() {}).toInstance(cacheTennisArenaRepository)
        bind(object : TypeLiteral<CacheRepository<TennisArena>>() {}).toInstance(cacheTennisArenaRepository)
        bind(Repository::class.java).toInstance(cacheTennisArenaRepository)
        bind(CacheRepository::class.java).toInstance(cacheTennisArenaRepository)

        // Services
        bind(SignService::class.java).toInstance(
            SignServiceImpl(
                plugin,
                CommandServiceImpl(),
                MCTennisLanguage.noPermissionMessage
            )
        )
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

        if (Bukkit.getPluginManager().getPlugin(PluginDependency.PLACEHOLDERAPI.pluginName) != null) {
            bind(PlaceHolderService::class.java).to(DependencyPlaceholderApiServiceImpl::class.java)
                .`in`(Scopes.SINGLETON)
            plugin.logger.log(Level.INFO, "Loaded dependency ${PluginDependency.PLACEHOLDERAPI.pluginName}.")
        } else {
            bind(PlaceHolderService::class.java).to(PlaceHolderServiceImpl::class.java).`in`(Scopes.SINGLETON)
        }
    }
}
