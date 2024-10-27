package com.github.shynixn.mctennis.impl.service

import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.contract.PlaceHolderService
import com.github.shynixn.mctennis.contract.TennisGame
import com.google.inject.Inject
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class DependencyPlaceholderApiServiceImpl @Inject constructor(
    private val plugin: Plugin,
    private val gameService: GameService
) : PlaceholderExpansion(), PlaceHolderService {
    private var registerd: Boolean = false
    private val placeHolderService = PlaceHolderServiceImpl(gameService)

    init {
        this.registerListener()
    }

    /**
     * Registers the placeholder hook if it is not already registered.
     */
    fun registerListener() {
        if (!registerd) {
            this.register()
            registerd = true
        }
    }

    /**
     * Gets the expansion version which is the same of the plugin version.
     */
    override fun getVersion(): String {
        return plugin.description.version
    }

    /**
     * Gets the expansion author for placeholderapi.
     */
    override fun getAuthor(): String {
        return plugin.description.authors[0]
    }

    /**
     * Gets the identifier which is required by placeholderapi to match the placeholder against this plugin.
     */
    override fun getIdentifier(): String {
        return "mctennis"
    }

    /**
     * OnPlaceHolder Request
     *
     * @param player player
     * @param s      customText
     * @return result
     */
    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (params == null) {
            return null
        }

        val parts = params.split("_")
        val finalPart = parts[parts.size - 1]
        val newParams = parts.dropLast(1).joinToString("_")

        val selectedGame = gameService.getByName(finalPart)
        if (selectedGame != null) {
            return placeHolderService.replacePlaceHolders("%mctennis_${newParams}%", player, selectedGame)
        }

        if (player != null) {
            val otherGame = gameService.getByPlayer(player)

            if (otherGame != null) {
                return placeHolderService.replacePlaceHolders(
                    "%mctennis_${params}%",
                    player,
                    otherGame
                )
            } else {
                return placeHolderService.replacePlaceHolders("%mctennis_${params}%", player, null)
            }
        }

        return null
    }

    /**
     * Replaces the placeholders.
     */
    override fun replacePlaceHolders(text: String, player: Player?, game: TennisGame?): String {
        val replacedInput = placeHolderService.replacePlaceHolders(text, player, game)
        return PlaceholderAPI.setPlaceholders(player, replacedInput)
    }
}
