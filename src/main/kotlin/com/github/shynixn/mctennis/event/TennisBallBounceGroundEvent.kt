package com.github.shynixn.mctennis.event

import com.github.shynixn.mctennis.contract.TennisBall
import com.github.shynixn.mctennis.contract.TennisGame

class TennisBallBounceGroundEvent(val tennisBall: TennisBall, val game : TennisGame) : MCTennisEvent() {
}
