# Permission

The following permissions are available in MCTennis.

#### Levels

* User: A permission all players can have.
* Admin: A permission only admins should have.

### Recommended Permissions

| Permission                          | Level | Description                                                                     |   
|-------------------------------------|-------|---------------------------------------------------------------------------------|
| mctennis.command                    | User  | Allows to use the /mctennis command.                                            |   
| mctennis.join.*                     | User  | Allows to join all games. The **mctennis.command** permission is also required. |  
| mctennis.shyscoreboard.scoreboard.* | User  | Allows to see all mctennis scoreboards during games.                            |

### All Permissions

| Permission                                            | Level | Description                                                                           |   
|-------------------------------------------------------|-------|---------------------------------------------------------------------------------------|
| mctennis.command                                      | User  | Allows to use the /mctennis command.                                                  |   
| mctennis.join.*                                       | User  | Allows to join all games. The **mctennis.command** permission is also required.       |  
| mctennis.join.[name]                                  | User  | Allows to join a specific game. The **mctennis.command** permission is also required. |  
| mctennis.edit                                         | Admin | Allows to create, edit and delete games.                                              |
| mctennis.shyscoreboard.scoreboard.*                   | User  | Allows to see all scoreboards                                                         |
| mctennis.shyscoreboard.scoreboard.\[scoreboard-name\] | User  | Allows to see a specific scoreboard                                                   |
| mctennis.shyscoreboard.command                        | Admin | Allows to use the /mctennisscoreboard command.                                        |
| mctennis.shyscoreboard.reload                         | Admin | Allows to reload configurations.                                                      |
| mctennis.shyscoreboard.add                            | Admin | Allows to add a scoreboard to a player                                                |
| mctennis.shyscoreboard.remove                         | Admin | Allows to remove a scoreboard from a player                                           |
| mctennis.shyscoreboard.update                         | Admin | Allows to refresh a scoreboard                                                        |
