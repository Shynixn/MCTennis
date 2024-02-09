package com.github.shynixn.mctennis.contract

import org.bukkit.entity.Player

interface PlaceHolderService {
    /**
     * Replaces the placeholders.
     */
    fun replacePlaceHolders(text: String, player: Player? = null, game: TennisGame? = null): String
}
