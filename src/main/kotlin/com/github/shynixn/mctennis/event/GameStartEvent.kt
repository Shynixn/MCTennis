package com.github.shynixn.mctennis.event

import com.github.shynixn.mctennis.contract.TennisGame

class GameStartEvent(val game : TennisGame) : MCTennisEvent() {
}
