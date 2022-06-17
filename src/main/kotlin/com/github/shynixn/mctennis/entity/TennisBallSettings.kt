package com.github.shynixn.mctennis.entity

import com.github.shynixn.mctennis.enumeration.VisibilityType
import org.bukkit.Sound

class TennisBallSettings {
    // Ground
    var horizontalSpeedRelative: Double = 0.7

    var verticalSpeedAbsolute: Double = 0.25

    // The object keeps 99% of its current speed after each tick.
    var groundResistanceRelative = 0.99

    // Amount of negative acceleration is applied after each tick.
    // e.g. Reverse vector is created, normalized and multiplied by it.
    var groundResistanceAbsolute = 0.0001

    /**
     * The bouncing modifiers when a physicObject hits the ground.
     */
    var groundBouncing: Double = 0.7

    // Air
    /**
     * Amount of positive acceleration in y direction is applied after each tick.
     */
    var gravityAbsolute: Double = 0.02

    // The object keeps 99% of its current speed after each tick.
    var airResistanceRelative = 0.99

    // Amount of negative acceleration is applied after each tick.
    // e.g. Reverse vector is created, normalized and multiplied by it.
    var airResistanceAbsolute = 0.0001

    /**
     * Render visibility updates.
     */
    var renderVisibilityUpdateMs: Int = 5000

    /**
     * Render distance blocks.
     */
    var renderDistanceBlocks: Int = 70

    /**
     *  Size of the click hitBox.
     */
    var clickHitBoxSize: Int = 2

    /**
     * Base Multiplier for the speed when spinning.
     */
    var spinBaseMultiplier: Double = 1.0

    /**
     * Maximum spinning velocity.
     */
    var maximumSpinningVelocity = 0.08

    /**
     * Maximum spin angle.
     */
    var spinMaximum: Double = 60.0

    /**
     * Minimum spin angle
     */
    var spinMinimum: Double = 0.0

    /**
     * Default spin.
     */
    var spinDefault: Double = 20.0

    /**
     * Spin vertical
     */
    var spinVertical: Double = 0.6

    /**
     * Minimum amount of cooldown milliseconds between two clicks.
     */
    var clickCooldown: Int = 250

    /**
     * Slime visibility.
     */
    var slimeVisibility: VisibilityType = VisibilityType.BEDROCK

    /**
     * Armorstand visiblity.
     */
    var armorstandVisibility: VisibilityType = VisibilityType.JAVA

    /**
     * Sound played when hitting the ball.
     */
    var hitSound: SoundMeta = SoundMeta().also {
        it.name = "ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR"
        it.pitch = 2.0
        it.volume = 5.0
        it.effectType = EffectTargetType.EVERYONE
    }

    /**
     * Sound played when bouncing on ground.
     */
    var bounceSound: SoundMeta = SoundMeta().also {
        it.name = Sound.BLOCK_STONE_HIT.toString()
        it.pitch = 2.0
        it.volume = 5.0
        it.effectType = EffectTargetType.EVERYONE
    }
}
