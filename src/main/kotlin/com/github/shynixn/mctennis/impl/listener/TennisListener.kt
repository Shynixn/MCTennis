package com.github.shynixn.mctennis.impl.listener

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mctennis.MCTennisLanguage
import com.github.shynixn.mctennis.contract.TennisBall
import com.github.shynixn.mctennis.contract.TennisGame
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.Team
import com.github.shynixn.mctennis.event.TennisBallBounceGroundEvent
import com.github.shynixn.mcutils.common.toLocation
import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.api.meta.enumeration.EntityType
import com.github.shynixn.mcutils.packet.api.packet.PacketOutEntityDestroy
import com.github.shynixn.mcutils.packet.api.packet.PacketOutEntityMetadata
import com.github.shynixn.mcutils.packet.api.packet.PacketOutEntitySpawn
import com.google.inject.Inject
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class TennisListener @Inject constructor(
    private val packetService: PacketService,
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

            if (game.lastHitPlayer == null) {
                game.dispose()
            }

            return
        }

        val team = game.getTeamFromPlayer(player) ?: return

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
        val location = ball.getLocation().toVector3d().addRelativeUp(-1.5).toLocation()
        val entityId = packetService.getNextEntityId()
        val players = game.getPlayers()

        for (player in players) {
            packetService.sendPacketOutEntitySpawn(player, PacketOutEntitySpawn().also {
                it.entityId = entityId
                it.entityType = EntityType.ARMOR_STAND
                it.target = location
            })

            packetService.sendPacketOutEntityMetadata(player, PacketOutEntityMetadata().also {
                it.entityId = entityId
                it.customNameVisible = true
                it.customname = message
                it.isInvisible = true
            })
        }

        plugin.launch {
            delay(2000)
            for (player in players) {
                packetService.sendPacketOutEntityDestroy(player, PacketOutEntityDestroy().also {
                    it.entityIds = listOf(entityId)
                })
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
    private fun getHitTeamArea(vector3d: Location, arena: TennisArena): Team? {
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
