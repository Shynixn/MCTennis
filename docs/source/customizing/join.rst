Joining
=====================

There are multiple different ways, how you can configure joining games.
MCTennis does not ship any specific way how you can join games but offers
easy integration into other plugins.

Command Signing
~~~~~~~~~~~~~~~~~~

This way of joining works by executing the following command

``/mctennis join <name>``


Sign Joining
~~~~~~~~~~~~~~~~~~

This way of joining works by clicking on a sign to join a match.

MCTennis does not have its own signs, but you can install sign plugins, which are able
to execute commands on click.

For example, you can use the plugin `CommandSigns <https://www.spigotmc.org/resources/command-signs.10512//>`__ to attach the command
``/mctennis join <name>`` to a particular sign. Combine it with plugins such as `Colored-Signs <https://www.spigotmc.org/resources/colored-signs.31676/>`__ to create
fancy signs.

MCTennis also offers placeholders using `PlaceholderAPI <https://www.spigotmc.org/resources/placeholderapi.6245/>`__, which may be compatible to some sign plugins as well.

GUI Joining
~~~~~~~~~~~~~~~~~~

This way of joining works by offering a game selection in a GUI.

MCTennis does not have its own GUI, but you can install gui plugins, which are able to execute
commands on click.

For example, you can use the plugin `DeluxeMenus <https://www.spigotmc.org/resources/deluxemenus.11734/>`__ to attach the command
``/mctennis join <name>`` to a particular item in the GUI.

MCTennis also offers placeholders using `PlaceholderAPI <https://www.spigotmc.org/resources/placeholderapi.6245/>`__, which may be compatible to some gui plugins as well.

The configuration below is an example configuration of DeluxeMenu, where we have got 1 EMERALD_BLOCK in the gui. When clicking
on this block, the command ``/mctennis join test1`` is executed (change test1 to your arena name). Additionally, we check if the game
is currently joinable using the placeholder ``%mctennis_test1_isJoinAble%`` (change test1 to your arena name) and only display it if it is joinable.

A second item on the same slot is shown when the game is not joinable. Replace the placeholder here too.

*basics_menu.yml*

.. code-block:: yaml

    items:
      'joinmatch1':
        priority: 1
        material: EMERALD_BLOCK
        data: 1
        slot: 0
        display_name: "&f&lGame: %mctennis_test1_displayName%"
        lore:
          - "&7Click me to join the match."
        view_requirement:
                requirements:
                    joinable:
                        type: string equals
                        input: "%mctennis_test1_isJoinAble%"
                        output: "true"
        left_click_commands:
            - "[player] mctennis join test1"
      'fullmatch1':
        priority: 2
        material: STONE
        data: 1
        slot: 0
        display_name: "&f&lGame: %mctennis_test1_displayName%"
        view_requirement:
                requirements:
                    joinable:
                        type: string equals
                        input: "%mctennis_test1_isJoinAble%"
                        output: "false"
        lore:
          - "&7Game is already full."






