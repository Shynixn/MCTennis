package com.github.shynixn.mctennis

import com.github.shynixn.mcutils.common.language.LanguageItem
import com.github.shynixn.mctennis.contract.MCTennisLanguage

class MCTennisLanguageImpl : MCTennisLanguage {
 override val names: List<String>
  get() = listOf("en_us")
 override var gameStartingMessage = LanguageItem("[&9MCTennis&f] Game is starting in %1$1d seconds.")

 override var gameStartCancelledMessage = LanguageItem("[&9MCTennis&f] Game start has been cancelled.")

 override var gameDoesNotExistMessage = LanguageItem("[&9MCTennis&f] Game %1$1s does not exist.")

 override var noPermissionForGameMessage = LanguageItem("[&9MCTennis&f] You do not have permission to join game %1$1s.")

 override var noPermissionMessage = LanguageItem("[&9MCTennis&f] You do not have permission.")

 override var locationTypeDoesNotExistMessage = LanguageItem("[&9MCTennis&f] This location type is not known. For more locations, open the arena.yml.")

 override var spawnPointSetMessage = LanguageItem("[&9MCTennis&f] Location was set on %1$1s.")

 override var gameAlreadyExistsMessage = LanguageItem("[&9MCTennis&f] Game %1$1s already exists.")

 override var enabledArenaMessage = LanguageItem("[&9MCTennis&f] Game enable state was set to %1$1s.")

 override var gameIsFullMessage = LanguageItem("[&9MCTennis&f] Game is already full.")

 override var gameCreatedMessage = LanguageItem("[&9MCTennis&f] Created game %1$1s.")

 override var reloadedAllGamesMessage = LanguageItem("[&9MCTennis&f] Reloaded all games.")

 override var reloadedGameMessage = LanguageItem("[&9MCTennis&f] Reloaded game %1$1s.")

 override var joinTeamRedMessage = LanguageItem("[&9MCTennis&f] Successfully joined team red.")

 override var joinTeamBlueMessage = LanguageItem("[&9MCTennis&f] Successfully joined team blue.")

 override var leftGameMessage = LanguageItem("[&9MCTennis&f] Left the game.")

 override var deletedGameMessage = LanguageItem("[&9MCTennis&f] Deleted game %1$1s.")

 override var notEnoughPlayersMessage = LanguageItem("[&9MCTennis&f] Not enough players! Game start was cancelled.")

 override var teamDoesNotExistMessage = LanguageItem("[&9MCTennis&f] Team %1$1s does not exist.")

 override var updatedInventoryMessage = LanguageItem("[&9MCTennis&f] Updated inventory of game.")

 override var updatedArmorMessage = LanguageItem("[&9MCTennis&f] Updated armor of game.")

 override var secondsRemaining = LanguageItem("[&9MCTennis&f] %1$1s second(s) remaining.")

 override var gameCancelledMessage = LanguageItem("[&9MCTennis&f] Game has been cancelled.")

 override var scoreRed = LanguageItem("&c&l%mctennis_game_score%")

 override var scoreBlue = LanguageItem("&9&l%mctennis_game_score%")

 override var winRed = LanguageItem("&c&lTeam Red")

 override var winSetRed = LanguageItem("&cTeam Red")

 override var winBlue = LanguageItem("&9Team Blue")

 override var winSetBlue = LanguageItem("&9Team Blue")

 override var winDraw = LanguageItem("&fDraw")

 override var readyMessage = LanguageItem("&6&lReady?")

 override var bounceOutHologram = LanguageItem("&lOut")

 override var bounceSecondHologram = LanguageItem("&l2nd Bounce")

 override var joinSignLine1 = LanguageItem("&f[&r&lMCTennis&r&f]")

 override var joinSignLine2 = LanguageItem("%mctennis_game_stateDisplayName%")

 override var joinSignLine3 = LanguageItem("%mctennis_game_players%/%mctennis_game_maxPlayers%")

 override var joinSignLine4 = LanguageItem("")

 override var leaveSignLine1 = LanguageItem("&f[&r&lMCTennis&r&f]")

 override var leaveSignLine2 = LanguageItem("&f&lLeave")

 override var leaveSignLine3 = LanguageItem("%mctennis_game_players%/%mctennis_game_maxPlayers%")

 override var leaveSignLine4 = LanguageItem("")

 override var gameStateJoinAble = LanguageItem("&aJoin")

 override var gameStateDisabled = LanguageItem("&4Disabled")

 override var gameStateRunning = LanguageItem("&1Running")

 override var rightClickOnSignMessage = LanguageItem("RightClick on a sign to convert it into a game sign.")

 override var signTypeDoesNotExist = LanguageItem("This sign type does not exist.")

 override var addedSignMessage = LanguageItem("A sign was added to the game.")

 override var commandDescription = LanguageItem("All commands for the MCTennis plugin.")

 override var commandUsage = LanguageItem("[&9MCTennis&f] Use /mctennis help to see more info about the plugin.")

 override var maxLength20Characters = LanguageItem("The text length has to be less than 20 characters.")

 override var commandSenderHasToBePlayer = LanguageItem("The command sender has to be a player!")

 override var freeVersionMessage = LanguageItem("This version of MCTennis does only allow 1 game per server. Go to https://patreon.com/Shynixn for the premium version.")

 override var commandPlaceHolderMessage = LanguageItem("Evaluated placeholder: %1$1s")

 override var queueTimeOutMessage = LanguageItem("[&9MCTennis&f]&c Not enough players joined in time to start the game.")
}
