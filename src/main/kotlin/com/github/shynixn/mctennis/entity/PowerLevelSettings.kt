package com.github.shynixn.mctennis.entity

class PowerLevelSettings {
    /**
     * Are power levels enabled.
     */
    var enabled: Boolean = true

    /**
     * Amount of bars.
     */
    var levelsAmount: Int = 5

    /**
     * How fast the power level increases.
     */
    var increaseSteps: Double = 0.5

    /**
     * Multiplier of the base horizontal velocity.
     */
    var powerLevelMultiplierPerLevel : Double = 1.0
}
