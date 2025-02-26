package com.github.shynixn.mctennis

import com.fasterxml.jackson.core.type.TypeReference
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.contract.MCTennisLanguage
import com.github.shynixn.mctennis.contract.TennisBallFactory
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.impl.commandexecutor.MCTennisCommandExecutor
import com.github.shynixn.mctennis.impl.listener.GameListener
import com.github.shynixn.mctennis.impl.listener.PacketListener
import com.github.shynixn.mctennis.impl.listener.TennisListener
import com.github.shynixn.mctennis.impl.service.GameServiceImpl
import com.github.shynixn.mctennis.impl.service.TennisBallFactoryImpl
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.ConfigurationServiceImpl
import com.github.shynixn.mcutils.common.CoroutineExecutor
import com.github.shynixn.mcutils.common.chat.ChatMessageService
import com.github.shynixn.mcutils.common.command.CommandService
import com.github.shynixn.mcutils.common.command.CommandServiceImpl
import com.github.shynixn.mcutils.common.di.DependencyInjectionModule
import com.github.shynixn.mcutils.common.item.ItemService
import com.github.shynixn.mcutils.common.language.globalChatMessageService
import com.github.shynixn.mcutils.common.language.globalPlaceHolderService
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcher
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcherImpl
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.common.physic.PhysicObjectServiceImpl
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.common.repository.CacheRepository
import com.github.shynixn.mcutils.common.repository.CachedRepositoryImpl
import com.github.shynixn.mcutils.common.repository.Repository
import com.github.shynixn.mcutils.common.repository.YamlFileRepositoryImpl
import com.github.shynixn.mcutils.common.sound.SoundService
import com.github.shynixn.mcutils.common.sound.SoundServiceImpl
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.api.RayTracingService
import com.github.shynixn.mcutils.packet.impl.service.ChatMessageServiceImpl
import com.github.shynixn.mcutils.packet.impl.service.ItemServiceImpl
import com.github.shynixn.mcutils.packet.impl.service.PacketServiceImpl
import com.github.shynixn.mcutils.packet.impl.service.RayTracingServiceImpl
import com.github.shynixn.mcutils.sign.SignService
import com.github.shynixn.mcutils.sign.SignServiceImpl
import org.bukkit.plugin.Plugin

class MCTennisDependencyInjectionModule(
    private val plugin: MCTennisPlugin,
    private val language: MCTennisLanguage,
    private val placeHolderService: PlaceHolderService
) {
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

    fun build(): DependencyInjectionModule {
        val module = DependencyInjectionModule()

        // Params
        module.addService<Plugin>(plugin)
        module.addService<MCTennisLanguage>(language)

        // Repositories
        val tennisArenaRepository = YamlFileRepositoryImpl<TennisArena>(
            plugin,
            "arena",
            listOf(Pair("arena_sample.yml", "arena_sample.yml")),
            listOf("arena_sample.yml"),
            object : TypeReference<TennisArena>() {})
        val cacheTennisArenaRepository = CachedRepositoryImpl(tennisArenaRepository)
        module.addService<Repository<TennisArena>>(cacheTennisArenaRepository)
        module.addService<CacheRepository<TennisArena>>(cacheTennisArenaRepository)

        // Library Services
        module.addService<PlaceHolderService> {
            placeHolderService
        }
        module.addService<CommandService>(CommandServiceImpl(object : CoroutineExecutor {
            override fun execute(f: suspend () -> Unit) {
                plugin.launch {
                    f.invoke()
                }
            }
        }))
        module.addService<SignService> {
            SignServiceImpl(plugin, module.getService(), language.noPermissionMessage.text)
        }
        module.addService<PhysicObjectService> {
            PhysicObjectServiceImpl(plugin, module.getService())
        }
        module.addService<ChatMessageService>(ChatMessageServiceImpl(plugin))
        module.addService<PhysicObjectDispatcher>(PhysicObjectDispatcherImpl(plugin))
        module.addService<ConfigurationService>(ConfigurationServiceImpl(plugin))
        module.addService<SoundService>(SoundServiceImpl(plugin))
        module.addService<PacketService>(PacketServiceImpl(plugin))
        module.addService<ItemService>(ItemServiceImpl())
        module.addService<RayTracingService>(RayTracingServiceImpl())

        // Services
        module.addService<MCTennisCommandExecutor> {
            MCTennisCommandExecutor(
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService()
            )
        }
        module.addService<GameListener> {
            GameListener(module.getService(), module.getService())
        }
        module.addService<PacketListener> {
            PacketListener(module.getService(), module.getService(), module.getService())
        }
        module.addService<TennisListener> {
            TennisListener(module.getService(), module.getService(), module.getService())
        }

        module.addService<GameService> {
            GameServiceImpl(
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService()
            )
        }
        module.addService<TennisBallFactory> {
            TennisBallFactoryImpl(
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService()
            )
        }

        plugin.globalChatMessageService = module.getService()
        plugin.globalPlaceHolderService = module.getService()
        return module
    }
}
