package com.github.shynixn.mctennis.entity

class TennisBallSettings {
    // Ground

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
    var gravityAbsolute: Double = 0.03

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
}
