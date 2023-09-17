Game
=====================

This page explains step by step to setup a new game.

1. Create a new game
~~~~~~~~~~~~~~~~~~

Execute the following command to create a new arena file.

``/mctennis create <name> <displayName>``

Example:

.. code-block:: yaml

    /mctennis create game1 My first game


2. Confirm that the arena has been created
~~~~~~~~~~~~~~~~~~

Execute the following command to list all games:

``/mctennis list``

Output:

.. code-block:: yaml

    game1 [My first game] [disabled]

You can see that the arena is still disabled and cannot be joined yet.

3. Setting the global locations
~~~~~~~~~~~~~~~~~~

> Open the yaml file of your arena found in **plugins/MCTennis/arena/<name>.yml**

> Set the location where your players respawn when they leave the game.

arena.yml:

.. code-block:: yaml

    leaveSpawnpoint:
      world: "world"
      x: 0.0
      y: 0.0
      z: 0.0
      yaw: 0.0
      pitch: 0.0

4. Set the team locations for team red
~~~~~~~~~~~~~~~~~~

> Set the player spawnpoints inside of the field.

.. code-block:: yaml

    redTeamMeta:
      spawnpoints:
      - world: "world"
        x: 0.0
        y: 0.0
        z: 0.0
        yaw: 0.0
        pitch: 0.0

You can add multiple spawnpoints here to create 2vs2 or 4vs4 matches.

> Set the location of the lobby for this team.

.. code-block:: yaml

    redTeamMeta:
      lobbySpawnpoint:
        world: "world"
        x: 0.0
        y: 0.0
        z: 0.0
        yaw: 0.0
        pitch: 0.0

> Set the corners of the field of the team.

.. image:: ../_static/images/fieldselection.png

When taking a look at this example field, the playing field of team red is defined by the
two corners indicated by the 2 **diamond blocks**. Enter the coordinates of the two diamond blocks for
corner1 and corner2. If you set it for team blue, enter the coordinates of the two gold blocks.

.. code-block:: yaml

    redTeamMeta:
      corner1:
        world: "world"
        x: 0.0
        y: 0.0
        z: 0.0
        yaw: 0.0
        pitch: 0.0
      corner2:
        world: "world"
        x: 0.0
        y: 0.0
        z: 0.0
        yaw: 0.0
        pitch: 0.0

5. Set the team locations
~~~~~~~~~~~~~~~~~~

Repeat step 4 for team blue.

6. Enable the game
~~~~~~~~~~~~~~~~~~

> Once every location has been set, you can activate the game by setting enabled to true.

.. code-block:: yaml

    enabled: true


> Execute the following command to reload all games-

``/mctennis reload``

> Confirm that the game is listed as enabled.

``/mctennis list``

Output:

.. code-block:: yaml

    game1 [My first game] [enabled]










