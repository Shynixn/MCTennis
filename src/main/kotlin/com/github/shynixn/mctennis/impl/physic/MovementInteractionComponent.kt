package com.github.shynixn.mctennis.impl.physic

import com.github.shynixn.mctennis.contract.PhysicComponent
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.toVector3d
import org.bukkit.GameMode
import org.bukkit.entity.Player

class MovementInteractionComponent(
    private val playerComponent: PlayerComponent,
    private val physicComponent: MathComponent,
    private val interactionCooldown: Int,
    private val hitboxSize: Int
) :
    PhysicComponent {
    /**
     * Function being called when a player interacts with a physic object.
     */
    var onInteractEvent: MutableList<(Player, Vector3d) -> Unit> = arrayListOf()
    private var skipCounter = 0

    /**
     * Ticks on minecraft thread.
     */
    override fun tickMinecraft() {
        if (skipCounter > 0) {
            skipCounter--
            return
        }

        for (player in playerComponent.visiblePlayers) {
            val playerLocation = player.location.toVector3d()

            if (player.gameMode == GameMode.SPECTATOR) {
                continue
            }

            if (playerLocation.distance(physicComponent.position) > hitboxSize) {
                continue
            }

            // We do not want arbitrary player distances to change the vector. It is always normalized.
            val vector = physicComponent.position
                .clone()
                .subtract(playerLocation)
                .normalize()
            onInteractEvent.forEach { e -> e.invoke(player, vector) }

            this.skipCounter = interactionCooldown
            return
        }
    }

    /**
     * Closes the component.
     */
    override fun close() {
        this.onInteractEvent.clear()
    }
}
