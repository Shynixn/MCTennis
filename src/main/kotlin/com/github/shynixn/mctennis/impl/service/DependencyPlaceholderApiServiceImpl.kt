package com.github.shynixn.mctennis.impl.service

import com.github.shynixn.mctennis.MCTennisLanguage
import com.github.shynixn.mctennis.contract.DependencyPlaceholderApiService
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.enumeration.GameState
import com.github.shynixn.mctennis.enumeration.PlaceHolder
import com.google.inject.Inject
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class DependencyPlaceholderApiServiceImpl @Inject constructor(
    private val plugin: Plugin,
    private val gameService: GameService
) : PlaceholderExpansion(), DependencyPlaceholderApiService {
    private var registerd: Boolean = false

    /**
     * Registers the placeholder hook if it is not already registered.
     */
    override fun registerListener() {
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
    override fun onPlaceholderRequest(player: Player?, s: String?): String? {
        if (s == null) {
            return null
        }

        try {
            val parts = s.split("_")

            if (parts[0].equals("global", true) && player != null) {
                // All global placeholders.
                if (parts[1].equals(PlaceHolder.PLAYER_ISINGAME.text, true)) {
                    return (gameService.getByPlayer(player) != null).toString()
                }
            }
            val game = if (parts[0].equals("currentGame", true) && player != null) {
                val game = gameService.getByPlayer(player) ?: return null
                game
            } else {
                val name = parts[0]
                val game = gameService.getAll().firstOrNull { e -> e.arena.name.equals(name, true) }
                    ?: return MCTennisLanguage.gameDoesNotExistMessage.format(name)
                game
            }

            // All placeholders based on games.
            if (parts[1].equals(PlaceHolder.GAME_ENABLED.text, true)) {
                return game.arena.isEnabled.toString()
            }
            if (parts[1].equals(PlaceHolder.GAME_STARTED.text, true)) {
                return (game.gameState == GameState.RUNNING_SERVING || game.gameState == GameState.RUNNING_PLAYING).toString()
            }
            if (parts[1].equals(PlaceHolder.GAME_JOINABLE.text, true)) {
                return ((game.gameState == GameState.LOBBY_IDLE || game.gameState == GameState.LOBBY_COUNTDOWN) && !game.isFull()).toString()
            }
            if (parts[1].equals(PlaceHolder.GAME_DISPLAYNAME.text, true)) {
                return game.arena.displayName
            }
            if (parts[1].equals(PlaceHolder.GAME_ISTEAMREDPLAYER.text, true)) {
                return game.teamRedPlayers.contains(player).toString()
            }
            if (parts[1].equals(PlaceHolder.GAME_ISTEAMBLUEPLAYER.text, true)) {
                return game.teamBluePlayers.contains(player).toString()
            }
            if (parts[1].equals(PlaceHolder.GAME_RAWSCORETEAMRED.text, true)) {
                return game.teamRedScore.toString()
            }
            if (parts[1].equals(PlaceHolder.GAME_RAWSCORETEAMBLUE.text, true)) {
                return game.teamBlueScore.toString()
            }
            if (parts[1].equals(PlaceHolder.GAME_SCORE.text, true)) {
                return game.getScoreText()
            }
            if (parts[1].equals(PlaceHolder.GAME_SETSCORETEAMRED.text, true)) {
                return game.teamRedSetScore.toString()
            }
            if (parts[1].equals(PlaceHolder.GAME_SETSCORETEAMBLUE.text, true)) {
                return game.teamBlueSetScore.toString()
            }
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        }

        return null
    }
}
