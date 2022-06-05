package com.github.shynixn.mctennis.enumeration

enum class CommandType {
    SERVER, // One time command
    SERVER_PER_PLAYER, // One time command replaces player placeholder,
    PER_PLAYER // simply dispatches the command
}
