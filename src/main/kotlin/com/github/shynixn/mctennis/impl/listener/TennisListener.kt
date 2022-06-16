package com.github.shynixn.mctennis.impl.listener

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.MCTennisLanguage
import com.github.shynixn.mctennis.contract.TennisBall
import com.github.shynixn.mctennis.contract.TennisGame
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.Team
import com.github.shynixn.mctennis.event.TennisBallBounceGroundEvent
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.toLocation
import com.github.shynixn.mcutils.packet.api.*
import com.github.shynixn.mcutils.physicobject.api.PhysicObjectService
import com.google.inject.Inject
import kotlinx.coroutines.delay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class TennisListener @Inject constructor(
    private val physicObjectService: PhysicObjectService,
    private val plugin: Plugin
) : Listener {
    /**
     * Handles ground bouncing.
     */
    @EventHandler
    fun onBallBounceGroundEvent(event: TennisBallBounceGroundEvent) {
        if (event.isCancelled) {
            return
        }

        val game = event.game
        game.bounceCounter++

        val hitTeamArea = getHitTeamArea(event.tennisBall.getLocation(), game.arena)
        val player = game.lastHitPlayer!!

        if (!player.isOnline) {
            // Just assign it one randomly.
            game.lastHitPlayer = game.getPlayers().filter { e -> e.isOnline }.firstOrNull()
            return
        }

        val team = game.getTeamFromPlayer(player)
        val opponentTeam = getOppositeTeam(team)

        if (hitTeamArea == null) {
            printMessageAtScorePosition(
                game,
                event.tennisBall,
                MCTennisLanguage.bounceOutHologram
            )

            if (game.bounceCounter == 1) {
                // Player shot the ball directly outside the field.
                game.scorePoint(player, opponentTeam)
                return
            }

            // Enemy Player was not able to get the ball.
            game.scorePoint(player, team)
            return
        }

        if (hitTeamArea == team) {
            // Shot in own field, now allowed.
            printMessageAtScorePosition(
                game,
                event.tennisBall,
                MCTennisLanguage.bounceSecondHologram
            )
            game.scorePoint(player, opponentTeam)
            return
        }

        if (game.bounceCounter == 1) {
            // Fine continue game.
            return
        }

        printMessageAtScorePosition(
            game,
            event.tennisBall,
            MCTennisLanguage.bounceSecondHologram
        )
        game.scorePoint(player, team)
    }

    /**
     * Print at score position.
     */
    private fun printMessageAtScorePosition(game: TennisGame, ball: TennisBall, message: String) {
        val location = ball.getLocation().clone().addRelativeUp(-1.5).toLocation()
        val entityId = physicObjectService.createNewEntityId()
        val entitySpawnPacket = packetOutEntitySpawn {
            this.entityId = entityId
            this.entityType = EntityType.ARMOR_STAND
            this.target = location
        }
        val entityMetaDataPacket = packetOutEntityMetadata {
            this.entityId = entityId
            this.customNameVisible = true
            this.customname = message
            this.isInvisible = true
        }
        val players = game.getPlayers()

        for (player in players) {
            player.sendPacket(entitySpawnPacket)
            player.sendPacket(entityMetaDataPacket)
        }

        plugin.launch {
            delay(2000)
            val destroyPacket = packetOutEntityDestroy {
                this.entityId = entityId
            }

            for (player in players) {
                player.sendPacket(destroyPacket)
            }
        }
    }

    /**
     * Gets the opponent team.
     */
    private fun getOppositeTeam(team: Team): Team {
        if (team == Team.RED) {
            return Team.BLUE
        }

        return Team.RED
    }

    /**
     * Gets the team where the ball as hit the field.
     * e.g. ball has hit the ground on field of team blue -> returns team blue.
     */
    private fun getHitTeamArea(vector3d: Vector3d, arena: TennisArena): Team? {
        if (vector3d.x < arena.redTeamMeta.rightUpperCorner.x && vector3d.x > arena.redTeamMeta.leftLowerCorner.x) {
            if (vector3d.z < arena.redTeamMeta.rightUpperCorner.z && vector3d.z > arena.redTeamMeta.leftLowerCorner.z) {
                return Team.RED
            }
        }

        if (vector3d.x < arena.blueTeamMeta.rightUpperCorner.x && vector3d.x > arena.blueTeamMeta.leftLowerCorner.x) {
            if (vector3d.z < arena.blueTeamMeta.rightUpperCorner.z && vector3d.z > arena.blueTeamMeta.leftLowerCorner.z) {
                return Team.BLUE
            }
        }

        return null
    }
}
