package com.github.shynixn.mctennis.enumeration

enum class PlaceHolder(val fullPlaceHolder: String) {
    // Game
    GAME_ENABLED("%mctennis_game_isEnabled%"),
    GAME_JOINABLE("%mctennis_game_isJoinAble%"),
    GAME_STARTED("%mctennis_game_isRunning%"),
    GAME_DISPLAYNAME("%mctennis_game_displayName%"),
    GAME_RAWSCORETEAMRED("%mctennis_game_rawScoreTeamRed%"),
    GAME_RAWSCORETEAMBLUE("%mctennis_game_rawScoreTeamBlue%"),
    GAME_SCORE("%mctennis_game_score%"),
    GAME_CURRENT_SET("%mctennis_game_currentSet%"),
    GAME_WON_SETS_TEAM_RED("%mctennis_game_wonSetsTeamRed%"),
    GAME_WON_SETS_TEAM_BLUE("%mctennis_game_wonSetsTeamBlue%"),
    GAME_STATE("%mctennis_game_state%"),
    GAME_STATE_DISPLAYNAME("%mctennis_game_stateDisplayName%"),
    GAME_PLAYER_AMOUNT("%mctennis_game_players%"),
    GAME_MAX_PLAYER_AMOUNT("%mctennis_game_maxPlayers%"),
    // Game (Ball)
    GAME_BALL_LOCATION_WORLD("%mctennis_ball_locationWorld%"),
    GAME_BALL_LOCATION_X("%mctennis_ball_locationX%"),
    GAME_BALL_LOCATION_Y("%mctennis_ball_locationY%"),
    GAME_BALL_LOCATION_Z("%mctennis_ball_locationZ%"),
    GAME_BALL_LOCATION_YAW("%mctennis_ball_locationYaw%"),
    GAME_BALL_LOCATION_PITCH("%mctennis_ball_locationPitch%"),
    // Player
    PLAYER_ISINGAME("%mctennis_player_isInGame%"),
    PLAYER_NAME("%mctennis_player_name%"),
    // Player and Game
    GAME_ISTEAMREDPLAYER("%mctennis_game_isTeamRedPlayer%"),
    GAME_ISTEAMBLUEPLAYER("%mctennis_game_isTeamBluePlayer%"),
}
