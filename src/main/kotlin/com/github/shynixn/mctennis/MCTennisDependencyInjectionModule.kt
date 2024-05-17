package com.github.shynixn.mctennis

import com.fasterxml.jackson.core.type.TypeReference
import com.github.shynixn.mctennis.contract.BedrockService
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.contract.PlaceHolderService
import com.github.shynixn.mctennis.contract.TennisBallFactory
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.PluginDependency
import com.github.shynixn.mctennis.impl.service.*
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.ConfigurationServiceImpl
import com.github.shynixn.mcutils.common.chat.ChatMessageService
import com.github.shynixn.mcutils.common.command.CommandService
import com.github.shynixn.mcutils.common.command.CommandServiceImpl
import com.github.shynixn.mcutils.common.item.ItemService
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
import com.github.shynixn.mcutils.guice.DependencyInjectionModule
import com.github.shynixn.mcutils.packet.api.EntityService
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.api.RayTracingService
import com.github.shynixn.mcutils.packet.impl.service.*
import com.github.shynixn.mcutils.sign.SignService
import com.github.shynixn.mcutils.sign.SignServiceImpl
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.concurrent.Executor
import java.util.logging.Level

class MCTennisDependencyInjectionModule(private val plugin: Plugin) : DependencyInjectionModule() {
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
        // Common
        addService<Plugin>(plugin)

        // Repositories
        val tennisArenaRepository = YamlFileRepositoryImpl<TennisArena>(plugin,
            "arena",
            listOf(Pair("arena_sample.yml", "arena_sample.yml")),
            listOf("arena_sample.yml"),
            object : TypeReference<TennisArena>() {})
        val cacheTennisArenaRepository = CachedRepositoryImpl(tennisArenaRepository)
        addService<Repository<TennisArena>>(cacheTennisArenaRepository)
        addService<CacheRepository<TennisArena>>(cacheTennisArenaRepository)

        // Services
        addService<SignService> {
            SignServiceImpl(plugin, getService(), MCTennisLanguage.noPermissionMessage)
        }
        addService<PhysicObjectService> {
            PhysicObjectServiceImpl(plugin, getService())
        }
        addService<CommandService>(CommandServiceImpl(plugin))
        addService<PhysicObjectDispatcher>(PhysicObjectDispatcherImpl(plugin))
        addService<ConfigurationService>(ConfigurationServiceImpl(plugin))
        addService<SoundService>(SoundServiceImpl(plugin))
        addService<PacketService>(PacketServiceImpl(plugin
        ) { command -> plugin.server.scheduler.runTask(plugin, command) })
        addService<ItemService>(ItemServiceImpl())
        addService<EntityService, EntityServiceImpl>()
        addService<ChatMessageService, ChatMessageServiceImpl>()
        addService<RayTracingService, RayTracingServiceImpl>()
        addService<BedrockService, BedrockServiceImpl>()
        addService<GameService, GameServiceImpl>()
        addService<TennisBallFactory, TennisBallFactoryImpl>()

        if (Bukkit.getPluginManager().getPlugin(PluginDependency.PLACEHOLDERAPI.pluginName) != null) {
            addService<PlaceHolderService, DependencyPlaceholderApiServiceImpl>()
            plugin.logger.log(Level.INFO, "Loaded dependency ${PluginDependency.PLACEHOLDERAPI.pluginName}.")
        } else {
            addService<PlaceHolderService, PlaceHolderServiceImpl>()
        }
    }
}
