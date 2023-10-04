PlaceHolders
=====================

MCTennis offers PlaceHolder to be used in other plugins via `PlaceholderAPI <https://www.spigotmc.org/resources/placeholderapi.6245/>`__

These placeholders can be used to display scores and other information on things such as: Messages, Signs, Holograms, Bossbars,...

Placeholderlist
~~~~~~~~~~~~~~~

.. note::  Replace **<name>** with the actual name of a game.

======================================================================   ====================================               ===================
Description                                                              Placeholder                                        Examples
======================================================================   ====================================               ===================
Marker if a player is in any MCTennis game or not.                       %mctennis_global_isInGame%                         true,false
Marker if a game is enabled                                              %mctennis_<name>_isEnabled%                        true,false
Marker if a game can be joined                                           %mctennis_<name>_isJoinAble%                       true,false
Marker if a game is running                                              %mctennis_<name>_isRunning%                        true,false
Displayname of a game                                                    %mctennis_<name>_displayName%                      My amazing game
Marker if a player is part of team red                                   %mctennis_<name>_isTeamRedPlayer%                  true,false
Marker if a player is part of team blue                                  %mctennis_<name>_isTeamBluePlayer%                 true,false
Current score of a game in tennis format                                 %mctennis_<name>_score%                            30 - 40
Score of the red team in numeric format                                  %mctennis_<name>_rawScoreTeamRed%                  0,1,2,3
Score of the blue team in numeric format                                 %mctennis_<name>_rawScoreTeamBlue%                 0,1,2,3
======================================================================   ====================================               ===================

.. note::  You can use **currentGame** instead of using the name of the game and the current game of a player is used instead.
    e.g. *%mctennis_currentGame_score%* would display the score of the game the player has joined.
