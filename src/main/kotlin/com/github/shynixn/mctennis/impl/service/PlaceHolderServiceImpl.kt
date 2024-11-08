package com.github.shynixn.mctennis.impl.service

import com.github.shynixn.mctennis.MCTennisLanguageImpl
import com.github.shynixn.mctennis.MCTennisPlugin
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.contract.PlaceHolderService
import com.github.shynixn.mctennis.contract.TennisGame
import com.github.shynixn.mctennis.enumeration.GameState
import com.github.shynixn.mctennis.enumeration.PlaceHolder
import com.github.shynixn.mcutils.common.language.LanguageItem
import com.github.shynixn.mcutils.common.translateChatColors
import com.google.inject.Inject
import org.bukkit.entity.Player

class PlaceHolderServiceImpl @Inject constructor(private val gameService: GameService) : PlaceHolderService {
    private val langPlaceHolderFunctions = HashMap<String, (() -> String)>()
    private val gamePlayerHolderFunctions = HashMap<PlaceHolder, ((TennisGame) -> String)>()
    private val gameAndPlayerHolderFunctions = HashMap<PlaceHolder, ((TennisGame, Player) -> String)>()
    private val playerPlaceHolderFunctions = HashMap<PlaceHolder, ((Player) -> String)>()
    private val placeHolders = HashMap<String, PlaceHolder>()

    init {
        for (placeHolder in PlaceHolder.values()) {
            placeHolders[placeHolder.fullPlaceHolder] = placeHolder
        }

        for (field in MCTennisLanguageImpl::class.java.declaredFields) {
            field.isAccessible = true
            langPlaceHolderFunctions["%mctennis_lang_${field.name}%"] = { (field.get(null) as LanguageItem).text }
        }

        // Player
        playerPlaceHolderFunctions[PlaceHolder.PLAYER_NAME] =
            { p -> p.name }
        playerPlaceHolderFunctions[PlaceHolder.PLAYER_ISINGAME] =
            { p -> (gameService.getByPlayer(p) != null).toString() }

        // Game
        gamePlayerHolderFunctions[PlaceHolder.GAME_ENABLED] = { g -> g.arena.isEnabled.toString() }
        gamePlayerHolderFunctions[PlaceHolder.GAME_STARTED] =
            { g -> (g.gameState == GameState.RUNNING_SERVING || g.gameState == GameState.RUNNING_PLAYING).toString() }
        gamePlayerHolderFunctions[PlaceHolder.GAME_JOINABLE] =
            { g -> ((g.gameState == GameState.LOBBY_IDLE || g.gameState == GameState.LOBBY_COUNTDOWN) && !g.isFull()).toString() }
        gamePlayerHolderFunctions[PlaceHolder.GAME_DISPLAYNAME] = { g -> g.arena.displayName }
        gamePlayerHolderFunctions[PlaceHolder.GAME_RAWSCORETEAMRED] = { g -> g.teamRedScore.toString() }
        gamePlayerHolderFunctions[PlaceHolder.GAME_RAWSCORETEAMBLUE] = { g -> g.teamBlueScore.toString() }
        gamePlayerHolderFunctions[PlaceHolder.GAME_SCORE] = { g -> g.getScoreText() }
        gamePlayerHolderFunctions[PlaceHolder.GAME_STATE] = { g ->
            if (!g.arena.isEnabled) {
                "DISABLED"
            } else if (g.gameState == GameState.RUNNING_PLAYING || g.gameState == GameState.RUNNING_SERVING || g.gameState == GameState.ENDING) {
                "RUNNING"
            } else {
                "JOINABLE"
            }

        }
        gamePlayerHolderFunctions[PlaceHolder.GAME_STATE_DISPLAYNAME] = { g ->
            if (!g.arena.isEnabled) {
                MCTennisPlugin.language!!.gameStateDisabled.text
            } else if (g.gameState == GameState.RUNNING_PLAYING || g.gameState == GameState.RUNNING_SERVING || g.gameState == GameState.ENDING) {
                MCTennisPlugin.language!!.gameStateRunning.text
            } else {
                MCTennisPlugin.language!!.gameStateJoinAble.text
            }
        }
        gamePlayerHolderFunctions[PlaceHolder.GAME_PLAYER_AMOUNT] = { g ->
            (g.teamBluePlayers.size + g.teamRedPlayers.size).toString()
        }
        gamePlayerHolderFunctions[PlaceHolder.GAME_MAX_PLAYER_AMOUNT] = { g ->
            (g.arena.maxPlayersPerTeam * 2).toString()
        }
        gamePlayerHolderFunctions[PlaceHolder.GAME_BALL_LOCATION_WORLD] = { g ->
            if (g.ball != null && !g.ball!!.isDead) {
                g.ball!!.getLocation().world!!.name
            } else {
                ""
            }
        }
        gamePlayerHolderFunctions[PlaceHolder.GAME_BALL_LOCATION_X] = { g ->
            if (g.ball != null && !g.ball!!.isDead) {
                g.ball!!.getLocation().x.toString()
            } else {
                "0"
            }
        }
        gamePlayerHolderFunctions[PlaceHolder.GAME_BALL_LOCATION_Y] = { g ->
            if (g.ball != null && !g.ball!!.isDead) {
                g.ball!!.getLocation().y.toString()
            } else {
                "-1000"
            }
        }
        gamePlayerHolderFunctions[PlaceHolder.GAME_BALL_LOCATION_Z] = { g ->
            if (g.ball != null && !g.ball!!.isDead) {
                g.ball!!.getLocation().z.toString()
            } else {
                "0"
            }
        }
        gamePlayerHolderFunctions[PlaceHolder.GAME_BALL_LOCATION_YAW] = { g ->
            if (g.ball != null && !g.ball!!.isDead) {
                g.ball!!.getLocation().yaw.toString()
            } else {
                "0"
            }
        }
        gamePlayerHolderFunctions[PlaceHolder.GAME_BALL_LOCATION_PITCH] = { g ->
            if (g.ball != null && !g.ball!!.isDead) {
                g.ball!!.getLocation().pitch.toString()
            } else {
                "0"
            }
        }
        gamePlayerHolderFunctions[PlaceHolder.GAME_CURRENT_SET] = { g ->
            g.currentSet.toString()
        }
        gamePlayerHolderFunctions[PlaceHolder.GAME_WON_SETS_TEAM_BLUE] = { g ->
            g.teamBlueSetScore.toString()
        }
        gamePlayerHolderFunctions[PlaceHolder.GAME_WON_SETS_TEAM_RED] = { g ->
            g.teamRedSetScore.toString()
        }

        // Game and Player
        gameAndPlayerHolderFunctions[PlaceHolder.GAME_ISTEAMBLUEPLAYER] =
            { g, p -> g.teamBluePlayers.contains(p).toString() }
        gameAndPlayerHolderFunctions[PlaceHolder.GAME_ISTEAMREDPLAYER] =
            { g, p -> g.teamRedPlayers.contains(p).toString() }
    }

    /**
     * Replaces the placeholders.
     */
    override fun replacePlaceHolders(text: String, player: Player?, game: TennisGame?): String {
        var output = text
        for (i in 0 until 4) {
            if (!output.contains("%")) {
                break
            }

            val locatedPlaceHolders = HashMap<String, String>()
            val characterCache = StringBuilder()

            for (character in output) {
                characterCache.append(character)

                if (character == '%') {
                    val evaluatedPlaceHolder = characterCache.toString()
                    if (placeHolders.containsKey(evaluatedPlaceHolder)) {
                        val placeHolder = placeHolders[evaluatedPlaceHolder]!!
                        if (!locatedPlaceHolders.containsKey(placeHolder.fullPlaceHolder)) {
                            if (game != null && player != null && gameAndPlayerHolderFunctions.containsKey(placeHolder)) {
                                locatedPlaceHolders[placeHolder.fullPlaceHolder] =
                                    gameAndPlayerHolderFunctions[placeHolder]!!.invoke(game, player)
                            } else if (game != null && gamePlayerHolderFunctions.containsKey(placeHolder)) {
                                locatedPlaceHolders[placeHolder.fullPlaceHolder] =
                                    gamePlayerHolderFunctions[placeHolder]!!.invoke(game)
                            } else if (player != null && playerPlaceHolderFunctions.containsKey(placeHolder)) {
                                locatedPlaceHolders[placeHolder.fullPlaceHolder] =
                                    playerPlaceHolderFunctions[placeHolder]!!.invoke(player)
                            }
                        }
                    } else if (langPlaceHolderFunctions.containsKey(evaluatedPlaceHolder)) {
                        locatedPlaceHolders[evaluatedPlaceHolder] =
                            langPlaceHolderFunctions[evaluatedPlaceHolder]!!.invoke()
                    }

                    characterCache.clear()
                    characterCache.append(character)
                }
            }

            for (locatedPlaceHolder in locatedPlaceHolders.keys) {
                output = output.replace(locatedPlaceHolder, locatedPlaceHolders[locatedPlaceHolder]!!)
            }
        }

        return output.translateChatColors()
    }
}
