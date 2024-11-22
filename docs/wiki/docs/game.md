# Creating a new game

This page explains step by step to setup a new game.

### Create a new game

Execute the following command to create a new arena file.

```
/mctennis create game1 My first game
```

### Confirm that the arena has been created

Execute the following command to list all games:

```
/mctennis list
```

Output:

```
game1 [My first game] [disabled]
```

You can see that the arena is still disabled and cannot be joined yet.

### Setting the leave spawnpoint

Move to the location where you want your players to respawn when they leave the tennis game.

Execute the following:

```
/mctennis location game1 leave
```

### Set the lobby spawnpoint for team red

Executing the following:

```
/mctennis location game1 lobbyRed
```

### Set the spawnpoints for team red

Add the first spawnpoint by executing the following:

```
/mctennis location game1 spawnRed1
```

You can add multiple spawnpoints to create 2vs2 or 4vs4 matches. However, this can only be configured in the arena.yml file. For this, open the plugins/MCTennis/arena/<name>.yml file.

```yaml
redTeamMeta:
  spawnpoints:
  - world: "world"
    x: 0.0
    y: 0.0
    z: 0.0
    yaw: 0.0
    pitch: 0.0
  - world: "world"
    x: 0.0
    y: 0.0
    z: 0.0
    yaw: 0.0
    pitch: 0.0
```

### Set the playing area for team red

![img](assets/fieldselection.png)

When taking a look at this example field, the playing field of team red is defined by the two corners indicated by the 2 **diamond blocks**.

Move to the first corner and execute the following

```
/mctennis location game1 cornerRed1
```

Move to the second corner and execute the following

```
/mctennis location game1 cornerRed2
```

### Set the lobby spawnpoint for team blue

Executing the following:

```
/mctennis location game1 lobbyBlue
```

### Set the spawnpoints for team blue

Add the first spawnpoint by executing the following:

```
/mctennis location game1 spawnBlue1
```

You can add multiple spawnpoints to create 2vs2 or 4vs4 matches. However, this can only be configured in the arena.yml file. For this, open the plugins/MCTennis/arena/<name>.yml file.

```yaml
blueTeamMeta:
  spawnpoints:
  - world: "world"
    x: 0.0
    y: 0.0
    z: 0.0
    yaw: 0.0
    pitch: 0.0
  - world: "world"
    x: 0.0
    y: 0.0
    z: 0.0
    yaw: 0.0
    pitch: 0.0
```

### Set the playing area for team blue

![img](assets/fieldselection.png)

When taking a look at this example field, the playing field of team blue is defined by the two corners indicated by the 2 **gold blocks**.

Move to the first corner and execute the following

```
/mctennis location game1 cornerBlue1
```

```
/mctennis location game1 cornerBlue2
```

### Enable the game

Once every location has been set, you can try to activate the game by executing the following:

```
/mctennis toggle game1
```

Confirm that the game is listed as enabled.

```
/mctennis list
```

Output:

```
game1 [My first game] [enabled]
```

### Changing more options

Further customization options can be found in the ``plugins/MCTennis/arena/<name>.yml`` file.

The ``arena_sample.yml`` file contains explanations for each property.

Execute the reload command to load your file changes.

```
/mctennis reload game1
```

### Changing the speed of the ball

Players can change the speed of the ball by pressing SHIFT. The amount of green power bars define how fast the ball flies. 

However, the base speed of the ball may not fit the size of your arena. You should edit the following properties in your ``plugins/MCTennis/arena/<name>.yml`` file.

* horizontalBaseMultiplier
* verticalSpeedAbsolute
* gravityAbsolute

Try very minor adjustments (comma values), execute the reload command and rejoin your arena. Repeat until it works for you.

### Charge shots and power levels

By pressing SHIFT (sneak), you can charge your shots to shoot the ball further. Green bars appear when you hold SHIFT.
You can configure the multipliers and disable it under ``servePowerLevelSettings`` and ``defaultPowerLevelSettings`` in your ``arena.yml``.

### Changing the player inventory

In order to change the inventory a player receives, use the command ``/mctennis inventory`` and ``/mctennis armor``.
These commands copy your current inventory and store it into your ``arena.yml`` file.
