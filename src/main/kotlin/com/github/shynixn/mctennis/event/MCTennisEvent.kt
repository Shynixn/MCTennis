package com.github.shynixn.mctennis.event

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class MCTennisEvent: Event(), Cancellable {
    private var cancelled: Boolean = false

    /**
     * Event.
     */
    companion object {
        private var handlers = HandlerList()

        /**
         * Handlerlist.
         */
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }
    }

    /**
     * Returns all handles.
     */
    override fun getHandlers(): HandlerList {
        return MCTennisEvent.handlers
    }

    /**
     * Is the event cancelled.
     */
    override fun isCancelled(): Boolean {
        return cancelled
    }

    /**
     * Sets the event cancelled.
     */
    override fun setCancelled(flag: Boolean) {
        this.cancelled = flag
    }
}
