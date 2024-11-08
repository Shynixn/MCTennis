package com.github.shynixn.mctennis

import com.github.shynixn.mctennis.contract.Language
import com.github.shynixn.mcutils.common.language.LanguageItem
import com.github.shynixn.mcutils.common.language.LanguageProviderImpl

class MCTennisLanguageImpl() : Language, LanguageProviderImpl() {
    override val names: List<String>
        get() = listOf("en_us")

    override var gameStartingMessage: LanguageItem = LanguageItem()

    override var gameStartCancelledMessage: LanguageItem = LanguageItem()

    override var gameDoesNotExistMessage: LanguageItem = LanguageItem()

    override var noPermissionForGameMessage: LanguageItem = LanguageItem()

    override var noPermissionMessage: LanguageItem = LanguageItem()

    override var locationTypeDoesNotExistMessage: LanguageItem = LanguageItem()

    override var spawnPointSetMessage: LanguageItem = LanguageItem()

    override var gameAlreadyExistsMessage: LanguageItem = LanguageItem()

    override var enabledArenaMessage: LanguageItem = LanguageItem()

    override var gameIsFullMessage: LanguageItem = LanguageItem()

    override var gameCreatedMessage: LanguageItem = LanguageItem()

    override var reloadedAllGamesMessage: LanguageItem = LanguageItem()

    override var reloadedGameMessage: LanguageItem = LanguageItem()

    override var joinTeamRedMessage: LanguageItem = LanguageItem()

    override var joinTeamBlueMessage: LanguageItem = LanguageItem()

    override var leftGameMessage: LanguageItem = LanguageItem()

    override var deletedGameMessage: LanguageItem = LanguageItem()

    override var notEnoughPlayersMessage: LanguageItem = LanguageItem()

    override var teamDoesNotExistMessage: LanguageItem = LanguageItem()

    override var updatedInventoryMessage: LanguageItem = LanguageItem()

    override var updatedArmorMessage: LanguageItem = LanguageItem()

    override var secondsRemaining: LanguageItem = LanguageItem()

    override var gameCancelledMessage: LanguageItem = LanguageItem()

    override var scoreRed: LanguageItem = LanguageItem()

    override var scoreBlue: LanguageItem = LanguageItem()

    override var winRed: LanguageItem = LanguageItem()

    override var winSetRed: LanguageItem = LanguageItem()

    override var winBlue: LanguageItem = LanguageItem()

    override var winSetBlue: LanguageItem = LanguageItem()

    override var winDraw: LanguageItem = LanguageItem()

    override var readyMessage: LanguageItem = LanguageItem()

    override var bounceOutHologram: LanguageItem = LanguageItem()

    override var bounceSecondHologram: LanguageItem = LanguageItem()

    override var joinSignLine1: LanguageItem = LanguageItem()

    override var joinSignLine2: LanguageItem = LanguageItem()

    override var joinSignLine3: LanguageItem = LanguageItem()

    override var joinSignLine4: LanguageItem = LanguageItem()

    override var leaveSignLine1: LanguageItem = LanguageItem()

    override var leaveSignLine2: LanguageItem = LanguageItem()

    override var leaveSignLine3: LanguageItem = LanguageItem()

    override var leaveSignLine4: LanguageItem = LanguageItem()

    override var gameStateJoinAble: LanguageItem = LanguageItem()

    override var gameStateDisabled: LanguageItem = LanguageItem()

    override var gameStateRunning: LanguageItem = LanguageItem()

    override var rightClickOnSignMessage: LanguageItem = LanguageItem()

    override var signTypeDoesNotExist: LanguageItem = LanguageItem()

    override var addedSignMessage: LanguageItem = LanguageItem()

    override var commandDescription: LanguageItem = LanguageItem()

    override var commandUsage: LanguageItem = LanguageItem()

    override var maxLength20Characters: LanguageItem = LanguageItem()

    override var commandSenderHasToBePlayer: LanguageItem = LanguageItem()

    override var freeVersionMessage: LanguageItem = LanguageItem()

    override var commandPlaceHolderMessage: LanguageItem = LanguageItem()
}
