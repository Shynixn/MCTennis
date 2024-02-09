# Commands

The following commands are available in MCTennis. You can access them by typing:

```
/mctennis help 1
```

### /mctennis create

```
/mctennis create <name> <displayName>
```

Creates a new arena for a MCTennis minigame. For the free version, only 1 arena can be created per server.

* Name: Identifier of a game
* DisplayName: Arbitrary Display for your game. Can contain ChatColors.

### /mctennis delete

```
/mctennis delete <name>
```

Deletes a MCTennis game.

* Name: Identifier of a game

### /mctennis list

```
/mctennis list [player]
```

Lists all games you have created.

### /mctennis toggle

```
/mctennis toggle <name>
```

Enables or disables your game. If a game is disabled, nobody can join.

* Name: Identifier of a game

### /mctennis join

```
/mctennis join <name> [team]
```

Lets the player executing the command join the game. The optional team argument allows to directly join a specific team.
If the team is full, the other team will be chosen. If no team is specified, a random team will be selected.

* Name: Identifier of a game
* Team: Name of the team. Is always red or blue.

### /mctennis leave

```
/mctennis leave
```

Lets the player executing the command leave the game.

### /mctennis location

```
/mctennis location <name> <type>
```

Updates the location of a part of the arena. Setting all locations is necessary to create an arena.

* Name: Identifier of a game
* Type of location to set. Possible values: spawnRed1, spawnRed2, lobbyRed, lobbyBlue, leave, cornerBlue1, cornerBlue2, cornerRed1, cornerRed2

### /mctennis inventory

```
/mctennis inventory <name> <team>
```

Copies the inventory of the player executing the command. This copy will be applied to players when they join a game.

* Name: Identifier of a game
* Team: Name of the team. Is always red or blue.

### /mctennis armor

```
/mctennis armor <name> <team>
```

Copies the armor inventory of the player executing the command. This copy will be applied to players when they join a game.

* Name: Identifier of a game
* Team: Name of the team. Is always red or blue.

### /mctennis sign

```
/mctennis sign <name> <type>
```

Enables the player to add a specific sign by rightclicking any sign. You can remove signs by simply breaking the block.

* Name: Identifier of a game
* Type: Type of sign to create. Possible values: join, leave

### /mctennis reload

```
/mctennis reload [name]
```

Allows to reload all games or a specific single one.

* Name: Optional identifier of a game
