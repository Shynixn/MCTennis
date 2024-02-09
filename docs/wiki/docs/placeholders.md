# PlaceHolders

The following placeholders are available in MCTennis and can also be used via PlaceHolderApi.

!!! note "PlaceHolder Api"
    As MCTennis supports multiple games per server, you need to specify the name of the game in external plugins. You can do this by appending the name of the game ``_game1`` ``_mygame``.
    This results into placeholders such as e.g. ``%mctennis_game_isEnabled_game1%`` or ``%mctennis_game_displayName_mygame%``. This is only relevant in external plugins. For placeholders in MCTennis, you can directly use the placeholders below.

| Game PlaceHolders                | Description                                    |   
|----------------------------------|------------------------------------------------|
| %mctennis_game_isEnabled%        | true if the game enabled, false if not         |   
| %mctennis_game_isJoinAble%       | true if the game can be joined, false if not   |   
| %mctennis_game_isRunning%        | true if the game is running, false if not      |
| %mctennis_game_displayName%      | DisplayName of a game.                         |
| %mctennis_game_rawScoreTeamRed%  | Score of team red                              |
| %mctennis_game_rawScoreTeamBlue% | Score of team blue                             |
| %mctennis_game_score%            | Overall game score                             |
| %mctennis_game_state%            | State of the game: DISABLED, JOINABLE, RUNNING |
| %mctennis_game_stateDisplayName% | State of the game with color codes             |
| %mctennis_game_players%          | Current amount of players in the game          |
| %mctennis_game_maxPlayers%       | Max amount of players who can join this game   |

| Player PlaceHolders | Description                                     |   
|---------------------|-------------------------------------------------|
| %mctennis_player_isInGame% | true if the player is in the game, false if not |

| Game and Player PlaceHolders     | Description                                      |   
|----------------------------------|--------------------------------------------------|
| %mctennis_game_isTeamRedPlayer%  | true if the player is in team red, false if not  |
| %mctennis_game_isTeamBluePlayer% | true if the player is in team blue, false if not |
