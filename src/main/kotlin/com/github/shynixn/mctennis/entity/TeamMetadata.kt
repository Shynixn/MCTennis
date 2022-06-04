package com.github.shynixn.mctennis.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.github.shynixn.mcutils.common.Vector3d
import org.bukkit.inventory.ItemStack

class TeamMetadata {
    /**
     * Team displayName.
     */
    var name: String = ""

    /**
     * All  player spawnpoints.
     */
    @JsonIgnoreProperties(value = arrayOf("blockX", "blockY", "blockZ", "empty"))
    var spawnpoints = ArrayList<Vector3d>()

    /**
     * Spawnpoint in the team lobby.
     */
    @JsonIgnoreProperties(value = arrayOf("blockX", "blockY", "blockZ", "empty"))
    var lobbySpawnpoint: Vector3d = Vector3d("world")

    /**
     * ItemStacks during the game.
     */
    var inventoryContents: Array<HashMap<String, Any>?> = emptyArray()

    /**
     * ItemStacks during the game.
     */
    var armorInventoryContents: Array<HashMap<String, Any>?> = emptyArray()
}
