package com.github.shynixn.mctennis.entity

import org.bukkit.inventory.ItemStack

class PlayerData {
    /**
     * Inventory cache.
     */
    var inventoryContents: Array<ItemStack?>? = null

    /**
     * Inventory armor cache.
     */
    var armorContents: Array<ItemStack?>? = null

    /**
     * Was the player sneaking.
     */
    var wasSneaking: Boolean = false

    /**
     * Power Index.
     */
    var currentPower: Double = 1.0
}
