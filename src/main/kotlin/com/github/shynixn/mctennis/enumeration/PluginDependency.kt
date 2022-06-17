package com.github.shynixn.mctennis.enumeration

/**
 * Plugin dependency.
 */
enum class PluginDependency(
        /**
         * Plugin name.
         */
        val pluginName: String) {
    /**
     * PlaceHolderApi plugin.
     */
    PLACEHOLDERAPI("PlaceholderAPI"),

    /**
     * Geyser
     */
    GEYSER_SPIGOT("Geyser-Spigot")
}
