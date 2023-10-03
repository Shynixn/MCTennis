Interactions
=====================

MCTennis does not offer specific settings regarding prohibiting commands, destroying blocks, setting certain gameMode, etc.

In order to customize the experience to fit the needs of your server, you can setup multiple commands when a player joins
the match or leaves the match.

**arena.yml**

.. code-block:: yaml

    joinCommands:
    - type: 'SERVER_PER_PLAYER'
      command: 'say Hello %player_name% '
    - type: 'SERVER_PER_PLAYER'
      command: 'experience add %player_name% 1'
    leaveCommands:
    - type: 'SERVER_PER_PLAYER'
      command: 'say Bye %player_name% '
    - type: 'SERVER_PER_PLAYER'
      command: 'experience add %player_name% 1'

.. note::  The type **SERVER_PER_PLAYER** executes commands using the SERVER level permission, which means players do not
    have to have the permission to the command. If you want to execute commands using the PLAYER level permission, use **PER_PLAYER**.

Permission Based Interactions
~~~~~~~~~~~~~~~~~~

The right way to prohibit certain commands and actions during games is to use a permission plugin to configure it.
The most popular plugin `LuckPerms <https://www.spigotmc.org/resources/luckperms.28140/>`__ can be used for that.

**1. Create a new group called mctennis**

``/luckperms creategroup mctennis``

**2. Add all your permissions you want to allow or not allow during games to this group**

.. note::  Examples are permissions to certain commands, scoreboards or bossbar plugins. This allows displaying
    scores only during MCTennis games.

You can use the web editor or the following command.
As long as the players are inside that group, they can only do (e.g. not break blocks) and see (e.g. scoreboards) what you have configured.

``/luckperms group mctennis permission set <permission>``

**4. Add a new join command which adds the player to the group while he is in the match**

**arena.yml**

.. code-block:: yaml

    joinCommands:
    - type: 'SERVER_PER_PLAYER'
      command: 'lp user %player_name% parent add mctennis'

**5. Add a new leave command which removes the player from the group when he quits the match**

**arena.yml**

.. code-block:: yaml

    leaveCommands:
    - type: 'SERVER_PER_PLAYER'
      command: 'lp user %player_name% parent remove mctennis'

**6. Set the player to certain states**

For example, if you want to set the player to gamemode ``adventure`` during games, add another command called:

.. code-block:: yaml

    joinCommands:
    - type: 'SERVER_PER_PLAYER'
      command: 'lp user %player_name% parent add mctennis'
    - type: 'SERVER_PER_PLAYER'
      command: 'gamemode adventure %player_name%

Region Based Interactions
~~~~~~~~~~~~~~~~~~

Another way to prohibit certain commands and actions during games is to use a region plugin to configure it.

Put a region around the arena and the lobby with certain flags to disable destroying the arena.

