package com.github.shynixn.mctennis.contract

import com.github.shynixn.mcutils.common.language.LanguageItem
import com.github.shynixn.mcutils.common.language.LanguageProvider

interface MCTennisLanguage : LanguageProvider {
  var gameStartingMessage: LanguageItem

  var gameStartCancelledMessage: LanguageItem

  var gameDoesNotExistMessage: LanguageItem

  var noPermissionForGameMessage: LanguageItem

  var noPermissionMessage: LanguageItem

  var locationTypeDoesNotExistMessage: LanguageItem

  var spawnPointSetMessage: LanguageItem

  var gameAlreadyExistsMessage: LanguageItem

  var enabledArenaMessage: LanguageItem

  var gameIsFullMessage: LanguageItem

  var gameCreatedMessage: LanguageItem

  var reloadedAllGamesMessage: LanguageItem

  var reloadedGameMessage: LanguageItem

  var joinTeamRedMessage: LanguageItem

  var joinTeamBlueMessage: LanguageItem

  var leftGameMessage: LanguageItem

  var deletedGameMessage: LanguageItem

  var notEnoughPlayersMessage: LanguageItem

  var teamDoesNotExistMessage: LanguageItem

  var updatedInventoryMessage: LanguageItem

  var updatedArmorMessage: LanguageItem

  var secondsRemaining: LanguageItem

  var gameCancelledMessage: LanguageItem

  var scoreRed: LanguageItem

  var scoreBlue: LanguageItem

  var winRed: LanguageItem

  var winSetRed: LanguageItem

  var winBlue: LanguageItem

  var winSetBlue: LanguageItem

  var winDraw: LanguageItem

  var readyMessage: LanguageItem

  var bounceOutHologram: LanguageItem

  var bounceSecondHologram: LanguageItem

  var joinSignLine1: LanguageItem

  var joinSignLine2: LanguageItem

  var joinSignLine3: LanguageItem

  var joinSignLine4: LanguageItem

  var leaveSignLine1: LanguageItem

  var leaveSignLine2: LanguageItem

  var leaveSignLine3: LanguageItem

  var leaveSignLine4: LanguageItem

  var gameStateJoinAble: LanguageItem

  var gameStateDisabled: LanguageItem

  var gameStateRunning: LanguageItem

  var rightClickOnSignMessage: LanguageItem

  var signTypeDoesNotExist: LanguageItem

  var addedSignMessage: LanguageItem

  var commandDescription: LanguageItem

  var commandUsage: LanguageItem

  var maxLength20Characters: LanguageItem

  var commandSenderHasToBePlayer: LanguageItem

  var freeVersionMessage: LanguageItem

  var commandPlaceHolderMessage: LanguageItem

  var queueTimeOutMessage: LanguageItem
}
