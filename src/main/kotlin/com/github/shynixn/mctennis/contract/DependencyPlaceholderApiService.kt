package com.github.shynixn.mctennis.contract

interface DependencyPlaceholderApiService {
    /**
     * Registers the placeholder hook if it is not already registered.
     */
    fun registerListener()
}
