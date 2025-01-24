package com.github.shynixn.mctennis.enumeration

import com.github.shynixn.mctennis.MCTennisPlugin
import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.contract.MCTennisLanguage
import com.github.shynixn.mctennis.contract.TennisGame
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import org.bukkit.entity.Player

enum class PlaceHolder(val text: String, val f: (Player?, TennisGame?, Map<String, Any>?) -> String?) {
    // Game
    GAME_NAME("%mctennis_game_name_[game]%", { _, game, _ -> game?.arena?.name }),
    GAME_DISPLAYNAME("%mctennis_game_displayName_[game]%", { _, game, _ -> game?.arena?.displayName }),
    GAME_SUM_MAXPLAYERS("%mctennis_game_maxPlayers_[game]%", { _, game, _ ->
        if (game != null) {
            (game.arena.maxPlayersPerTeam * 2).toString()
        } else {
            null
        }
    }),
    GAME_ENABLED("%mctennis_game_isEnabled_[game]%", { _, game, _ -> game?.arena?.isEnabled?.toString() }),
    GAME_JOINABLE("%mctennis_game_isJoinAble_[game]%", { _, game, _ ->
        if (game != null) {
            ((game.gameState == GameState.LOBBY_IDLE || game.gameState == GameState.LOBBY_COUNTDOWN) && !game.isFull()).toString()
        } else {
            null
        }
    }),
    GAME_STARTED("%mctennis_game_isRunning_[game]%", { _, game, _ ->
        if (game != null) {
            (game.gameState == GameState.RUNNING_SERVING || game.gameState == GameState.RUNNING_PLAYING).toString()
        } else {
            null
        }
    }),
    GAME_RAWSCORETEAMRED("%mctennis_game_rawScoreTeamRed_[game]%", { _, game, _ -> game?.teamRedScore?.toString() }),
    GAME_RAWSCORETEAMBLUE("%mctennis_game_rawScoreTeamBlue_[game]%", { _, game, _ -> game?.teamBlueScore?.toString() }),
    GAME_SCORE("%mctennis_game_score_[game]%", { _, game, _ -> game?.getScoreText() }),
    GAME_CURRENT_SET("%mctennis_game_currentSet_[game]%", { _, game, _ -> game?.currentSet?.toString() }),
    GAME_WON_SETS_TEAM_RED(
        "%mctennis_game_wonSetsTeamRed_[game]%",
        { _, game, _ -> game?.teamRedSetScore?.toString() }),
    GAME_WON_SETS_TEAM_BLUE(
        "%mctennis_game_wonSetsTeamBlue_[game]%",
        { _, game, _ -> game?.teamBlueSetScore?.toString() }),
    GAME_STATE("%mctennis_game_state_[game]%", { _, game, _ ->
        if (game != null) {
            if (!game.arena.isEnabled) {
                "DISABLED"
            } else if (game.gameState == GameState.RUNNING_PLAYING || game.gameState == GameState.RUNNING_SERVING || game.gameState == GameState.ENDING) {
                "RUNNING"
            } else {
                "JOINABLE"
            }
        } else {
            null
        }
    }),
    GAME_STATE_DISPLAYNAME("%mctennis_game_stateDisplayName_[game]%", { _, game, context ->
        val language = context?.get(MCTennisPlugin.languageKey) as MCTennisLanguage?
        if (language != null && game != null) {
            if (!game.arena.isEnabled) {
                language.gameStateDisabled.text
            } else if (game.gameState == GameState.RUNNING_PLAYING || game.gameState == GameState.RUNNING_SERVING || game.gameState == GameState.ENDING) {
                language.gameStateRunning.text
            } else {
                language.gameStateJoinAble.text
            }
        } else {
            null
        }
    }),
    GAME_PLAYER_AMOUNT("%mctennis_game_players_[game]%", { _, game, _ ->
        if (game != null) {
            (game.teamBluePlayers.size + game.teamRedPlayers.size).toString()
        } else {
            null
        }
    }),

    // Game (Ball)
    GAME_BALL_LOCATION_WORLD("%mctennis_ball_locationWorld_[game]%", { _, game, _ ->
        if (game?.ball != null && !game.ball!!.isDead) {
            game.ball!!.getLocation().world!!.name
        } else {
            ""
        }
    }),
    GAME_BALL_LOCATION_X("%mctennis_ball_locationX_[game]%", { _, game, _ ->
        if (game?.ball != null && !game.ball!!.isDead) {
            game.ball!!.getLocation().x.toString()
        } else {
            "0"
        }
    }),
    GAME_BALL_LOCATION_Y("%mctennis_ball_locationY_[game]%", { _, game, _ ->
        if (game?.ball != null && !game.ball!!.isDead) {
            game.ball!!.getLocation().y.toString()
        } else {
            "0"
        }
    }),
    GAME_BALL_LOCATION_Z("%mctennis_ball_locationZ_[game]%", { _, game, _ ->
        if (game?.ball != null && !game.ball!!.isDead) {
            game.ball!!.getLocation().z.toString()
        } else {
            "0"
        }
    }),
    GAME_BALL_LOCATION_YAW("%mctennis_ball_locationYaw_[game]%", { _, game, _ ->
        if (game?.ball != null && !game.ball!!.isDead) {
            game.ball!!.getLocation().yaw.toString()
        } else {
            "0"
        }
    }),
    GAME_BALL_LOCATION_PITCH("%mctennis_ball_locationPitch_[game]%", { _, game, _ ->
        if (game?.ball != null && !game.ball!!.isDead) {
            game.ball!!.getLocation().pitch.toString()
        } else {
            "0"
        }
    }),

    // Player
    PLAYER_ISINGAME("%mctennis_player_isInGame%", { _, game, _ -> (game != null).toString() }),
    PLAYER_NAME("%mctennis_player_name%", { player, _, _ -> player?.name }),

    // Player and Game
    GAME_ISTEAMREDPLAYER(
        "%mctennis_game_isTeamRedPlayer_[game]%",
        { player, game, _ -> game?.teamRedPlayers?.contains(player)?.toString() }),
    GAME_ISTEAMBLUEPLAYER(
        "%mctennis_game_isTeamBluePlayer_[game]%",
        { player, game, _ -> game?.teamBluePlayers?.contains(player)?.toString() });

    companion object {
        /**
         * Registers all placeHolder. Overrides previously registered placeholders.
         */
        fun registerAll(
            placeHolderService: PlaceHolderService,
            gameService: GameService,
            language: MCTennisLanguage
        ) {
            for (placeHolder in PlaceHolder.values()) {
                placeHolderService.register(placeHolder.text) { player, context ->
                    val newContext = context.toMutableMap()
                    newContext[MCTennisPlugin.languageKey] = language
                    val gameNameReference = newContext[MCTennisPlugin.gameKey] as String?
                    val game = if (gameNameReference != null) {
                        gameService.getByName(gameNameReference)
                    } else if (player != null) {
                        gameService.getByPlayer(player)
                    } else {
                        null
                    }

                    placeHolder.f.invoke(player, game, newContext)
                }
            }
        }
    }
}
