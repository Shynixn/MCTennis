package com.github.shynixn.mctennis.entity

import com.github.shynixn.mcutils.Vector3d
import org.bukkit.inventory.ItemStack

class TeamMetadata {
    /**
     * Team displayName.
     */
    var name: String = ""

    /**
     * All  player spawnpoints.
     */
    var spawnpoints = ArrayList<Vector3d>()

    /**
     * Spawnpoint in the team lobby.
     */
    var lobbySpawnpoint: Vector3d = Vector3d()

    /**
     * ItemStacks during the game.
     */
    var inventoryContents: Array<ItemStack?> = emptyArray()

    /**
     * ItemStacks during the game.
     */
    var armorInventoryContents: Array<ItemStack?> = emptyArray()
}
