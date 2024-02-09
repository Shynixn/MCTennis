# Api

MCTennis offers a Developer Api, however it is not published to Maven Central or any other distribution system yet.
You need to directly reference the MCTennis.jar file.

## Usage

Add a dependency in your plugin.yml

```yaml
softdepend: [ MCTennis]
```

Take a look at the following example:
```java
public class YourPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        Player player = Bukkit.getPlayer("YourPlayerName");

        // Creating a tennis ball independent of a game.
        TennisBallFactory tennisBallFactory = Bukkit.getServicesManager().load(TennisBallFactory.class);
        TennisBall tennisBall = tennisBallFactory.createTennisBall(player.getLocation(), new TennisBallSettings(), null);
        // Launch the ball.
        tennisBall.setVelocity(new Vector(0.0, 0.5, 0.0));

        // Letting a player join a game.
        GameService gameService = Bukkit.getServicesManager().load(GameService.class);
        TennisGame game = gameService.getByName("myGameName");
        game.join(player, Team.BLUE);
    }
}
```
