package com.github.shynixn.mctennis.impl

import com.github.shynixn.mctennis.entity.TennisArena

class TennisArenaException(val arena: TennisArena, message: String) : RuntimeException(message) {

}
