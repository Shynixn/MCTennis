package com.github.shynixn.mctennis.impl.exception

import com.github.shynixn.mctennis.entity.TennisArena

class TennisGameException(val arena: TennisArena, message: String) : RuntimeException(message) {

}
