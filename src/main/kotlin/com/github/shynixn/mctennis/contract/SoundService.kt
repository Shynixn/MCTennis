package com.github.shynixn.mctennis.contract

import com.github.shynixn.mctennis.entity.SoundMeta
import org.bukkit.Location
import org.bukkit.entity.Player

interface SoundService {
    /**
     * Plays a sound.
     */
    fun playSound(location: Location, player: Player, soundMeta: SoundMeta)

    /**
     * Plays a sound.
     */
    fun playSound(location: Location, players: Collection<Player>, soundMeta: SoundMeta)
}
