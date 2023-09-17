package com.github.shynixn.mctennis.contract

interface PhysicComponent {
    /**
     * Ticks on minecraft thread.
     */
    fun tickMinecraft() {}

    /**
     * Tick on async thread.
     */
    fun tickAsync() {}

    /**
     * Closes the component.
     */
    fun close() {}
}
