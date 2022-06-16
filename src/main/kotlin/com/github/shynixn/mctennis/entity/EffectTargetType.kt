package com.github.shynixn.mctennis.entity

enum class EffectTargetType {
    /**
     * Effect effecting everyone.
     */
    EVERYONE,
    /**
     * Effect effecting the causing player.
     */
    RELATED_PLAYER,
    /**
     * Effect effecting nobody.
     */
    NOBODY
}
