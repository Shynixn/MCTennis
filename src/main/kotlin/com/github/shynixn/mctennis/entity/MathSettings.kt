package com.github.shynixn.mctennis.entity

class MathSettings {
    // Ground

    // The object keeps 99% of its current speed after each tick.
    var groundResistanceRelative = 0.99

    // Amount of negative acceleration is applied after each tick.
    // e.g. Reverse vector is created, normalized and multiplied by it.
    var groundResistanceAbsolute = 0.0001

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
     * Sometimes the object needs a higher raytracing origin to be able to pass through objects better.
     * e.g. 1.0 for player npcs.
     */
    var rayTraceYOffset: Double = 0.0

    /**
     * Should the ball collide with water blocks or lava?
     */
    var collideWithWater: Boolean = false

    /**
     * Should the ball collide with cobwebs and similar?
     */
    var collideWithPassableBlocks: Boolean =  true
}
