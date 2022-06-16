package com.github.shynixn.mctennis.impl.service

import com.github.shynixn.mctennis.contract.SoundService
import com.github.shynixn.mctennis.entity.EffectTargetType
import com.github.shynixn.mctennis.entity.SoundMeta
import com.google.inject.Inject
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Level

class SoundServiceImpl @Inject constructor(private val plugin: Plugin) : SoundService {
    private val compatibility = hashMapOf<String, String>("GHAST_FIREBALL" to "ENTITY_GHAST_SHOOT")

    /**
     * Plays a sound.
     */
    override fun playSound(location: Location, player: Player, soundMeta: SoundMeta) {
        playSound(location, listOf(player), soundMeta)
    }

    /**
     * Plays a sound.
     */
    override fun playSound(location: Location, players: Collection<Player>, soundMeta: SoundMeta) {
        if (soundMeta.effectType == EffectTargetType.NOBODY) {
            return
        }

        val playedPlayers = if (soundMeta.effectType == EffectTargetType.EVERYONE) {
            location.world!!.players
        } else {
            players
        }

        try {
            val name = soundMeta.name.uppercase(Locale.ENGLISH)
            val soundName = if (compatibility.containsKey(name)) {
                compatibility[name]!!
            } else {
                name
            }

            val sound = Sound.valueOf(soundName)

            for (player in playedPlayers) {
                player.playSound(location, sound, soundMeta.volume.toFloat(), soundMeta.pitch.toFloat())
            }
        } catch (e: Exception) {
            plugin.logger.log(
                Level.WARNING,
                "Failed to send sound. Is the sound '" + soundMeta.name + "' supported by this server version?",
                e
            )
        }
    }
}
