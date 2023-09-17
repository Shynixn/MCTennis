package com.github.shynixn.mctennis.entity

import com.github.shynixn.mcutils.common.EffectTargetType
import com.github.shynixn.mcutils.common.SoundMeta

class DoubleJumpMeta {
    /**
     * Is the double jump enabled.
     */
    var isDoubleJumpEnabled: Boolean = true

    /**
     * Double jump sound.
     */
    var doubleJumpSound: SoundMeta = SoundMeta().also {
        it.name = "GHAST_FIREBALL"
        it.pitch = 1.0
        it.volume = 5.0
        it.effectType = EffectTargetType.RELATED_PLAYER
    }

    /**
     * Vertical strength.
     */
    var doubleJumpVerticalStrength: Double = 0.2

    /**
     * Horizontal strength.
     */
    var doubleJumpHorizontalStrength: Double = 1.5
}
