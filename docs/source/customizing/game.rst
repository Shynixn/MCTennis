Game
=====================

This page explains step by step to setup a new game.

1. Create a new game
~~~~~~~~~~~~~~~~~~

Execute the following command to create a new arena file.

``/mctennis create <name> <displayName>``

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

3. Setting the leave spawnpoint
~~~~~~~~~~~~~~~~~~

Move to the location where you want your players to respawn when they leave the tennis game.

Execute the following:

``/mctennis location <name> leave``

.. code-block:: yaml

    /mctennis location game1 leave

4. Set the lobby spawnpoint for team red
~~~~~~~~~~~~~~~~~~

Executing the following:

``/mctennis location <name> lobbyRed``

.. code-block:: yaml

    /mctennis location game1 lobbyRed

5. Set the spawnpoints for team red
~~~~~~~~~~~~~~~~~~

Add the first spawnpoint by executing the following:

``/mctennis location <name> spawnRed1``

.. code-block:: yaml

    /mctennis location game1 spawnRed1

You can add multiple spawnpoints to create 2vs2 or 4vs4 matches.
However, this can only be configured in the arena.yml file.
For this, open the **plugins/MCTennis/arena/<name>.yml** file.

.. code-block:: yaml

    redTeamMeta:
      spawnpoints:
      - world: "world"
        x: 0.0
        y: 0.0
        z: 0.0
        yaw: 0.0
        pitch: 0.0

6. Set the playing area for team red
~~~~~~~~~~~~~~~~~~

.. image:: ../_static/images/fieldselection.png

When taking a look at this example field, the playing field of team red is defined by the two corners indicated by the 2 **diamond blocks**.

Move to the first corner and execute the following

``/mctennis location <name> cornerRed1``

.. code-block:: yaml

    /mctennis location game1 cornerRed1


Move to the second corner and execute the following


``/mctennis location <name> cornerRed2``

.. code-block:: yaml

    /mctennis location game1 cornerRed2

4. Set the lobby spawnpoint for team blue
~~~~~~~~~~~~~~~~~~

Executing the following:

``/mctennis location <name> lobbyBlue``

.. code-block:: yaml

    /mctennis location game1 lobbyBlue

7. Set the spawnpoints for team blue
~~~~~~~~~~~~~~~~~~

Add the first spawnpoint by executing the following:

``/mctennis location <name> spawnBlue1``

.. code-block:: yaml

    /mctennis location game1 spawnBlue1

You can add multiple spawnpoints to create 2vs2 or 4vs4 matches.
However, this can only be configured in the arena.yml file.
For this, open the **plugins/MCTennis/arena/<name>.yml** file.

.. code-block:: yaml

    blueTeamMeta:
      spawnpoints:
      - world: "world"
        x: 0.0
        y: 0.0
        z: 0.0
        yaw: 0.0
        pitch: 0.0

8. Set the playing area for team blue
~~~~~~~~~~~~~~~~~~

.. image:: ../_static/images/fieldselection.png

When taking a look at this example field, the playing field of team blue is defined by the two corners indicated by the 2 **gold blocks**.

Move to the first corner and execute the following

``/mctennis location <name> cornerBlue1``

.. code-block:: yaml

    /mctennis location game1 cornerBlue1


Move to the second corner and execute the following


``/mctennis location <name> cornerBlue2``

.. code-block:: yaml

    /mctennis location game1 cornerBlue2


9. Enable the game
~~~~~~~~~~~~~~~~~~

Once every location has been set, you can try to activate the game by executing the following:

``/mctennis toggle <name>``

.. code-block:: yaml

    /mctennis toggle game1

Confirm that the game is listed as enabled.

``/mctennis list``

Output:

.. code-block:: yaml

    game1 [My first game] [enabled]


10. Changing more options
~~~~~~~~~~~~~~~~~~

Further customization options can be found in the  **plugins/MCTennis/arena/<name>.yml** file.

Execute the reload command to load your file changes.

``/mctennis reload <name>``







