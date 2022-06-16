package com.github.shynixn.mctennis.impl.listener

import com.github.shynixn.mctennis.contract.GameService
import com.github.shynixn.mctennis.entity.TennisArena
import com.github.shynixn.mctennis.enumeration.Team
import com.github.shynixn.mctennis.event.TennisBallBounceGroundEvent
import com.github.shynixn.mcutils.common.Vector3d
import com.google.inject.Inject
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class TennisListener @Inject constructor(private val gameService: GameService) : Listener {
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
        val team = game.getTeamFromPlayer(player)
        val opponentTeam = getOppositeTeam(team)

        println("HIT: " + hitTeamArea)

        if (hitTeamArea == null) {
            game.sendMessageToPlayers("Out")

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
            game.sendMessageToPlayers("Net")
            game.scorePoint(player, opponentTeam)
            return
        }

        if (game.bounceCounter == 1) {
            // Fine continue game.
            return
        }

        game.sendMessageToPlayers("2nd bounce")
        game.scorePoint(player, team)
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
