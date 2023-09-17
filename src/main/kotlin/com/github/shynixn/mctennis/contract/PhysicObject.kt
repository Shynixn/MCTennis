package com.github.shynixn.mctennis.contract

import org.bukkit.entity.Player

interface PhysicObject {
    /**
     * Gets all entity ids.
     */
    val entityIds: List<Int>

    /**
     * Is the physicObject dead.
     */
    val isDead: Boolean

    /**
     * LeftClick on the physic object.
     */
    fun leftClick(player: Player) {}

    /**
     * RightClick on the physic object.
     */
    fun rightClick(player: Player) {}

    /**
     * Gets called when the playeris riding the entity.
     */
    fun ride(player: Player, forward: Double, sideward: Double, isJumping: Boolean) {}

    /**
     * Ticks on minecraft thread.
     */
    fun tickMinecraft()

    /**
     * Tick on async thread.
     */
    fun tickAsync()

    /**
     * Removes the physic object.
     */
    fun remove()
}
