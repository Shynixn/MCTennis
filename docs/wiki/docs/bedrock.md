# Bedrock 

MCTennis supports crossplay with Bedrock clients (SmartPhone,Windows,Console, etc.) using [GeyserMC](https://geysermc.org/).

### Initial Setup

* Install [GeyserMC](https://geysermc.org/) on your server  or on your proxy server.
* Install MCTennis on your Spigot/Paper based server
* Join your server with a BedRock client and join the MCTennis game ``/mctennis join game1``. 
* Observe that the ball will be displayed as a simple player_head because bedrock does not support custom player heads.
* Observe, that the ball will not be able to rotate.

### Fixing skin and rotations via GeyserMC (PatreonOnly)

Thanks to my **Patreon supporters**, who have funded this feature :heart: . 
They can download my [prepared zip file](https://www.patreon.com/Shynixn) to easily setup MCTennis for Bedrock. 

If you are using the free version of MCTennis, you can still configure MCTennis for Bedrock, but it is more work for you. If you want
to save time, become a patreon member at [https://www.patreon.com/Shynixn](https://www.patreon.com/Shynixn).

=== "Spigot/Paper"

    * Download the ``MCTennis-GeyserMC.zip`` file from [https://www.patreon.com/Shynixn](https://www.patreon.com/Shynixn).
    * Extract the ``MCTennis-GeyserMC.zip`` contents into your ``plugins\Geyser-Spigot``.
    * Extract the ``custom-skulls_mctennis.yml`` into ``plugins\Geyser-Spigot\custom-skulls_mctennis.yml``
    * Extract the ``packs/MCTennisPack.mcpack`` into ``plugins\Geyser-Spigot\packs\MCTennisPack.mcpack``
    * Copy the ``player-profiles`` values from ``plugins\Geyser-Spigot\custom-skulls_mctennis.yml`` into the ``plugins\Geyser-Spigot\custom-skulls.yml`` file.

=== "Proxies (BungeeCord, Velocity, etc.)"

    * Download the ``MCTennis-GeyserMC.zip`` file from [https://www.patreon.com/Shynixn](https://www.patreon.com/Shynixn).
    * Extract the ``MCTennis-GeyserMC.zip`` contents into your ``plugins\Geyser-<Proxy>``.
    * Extract the ``custom-skulls_mctennis.yml`` into ``plugins\Geyser-<Proxy>\custom-skulls_mctennis.yml``
    * Extract the ``packs/MCTennisPack.mcpack`` into ``plugins\Geyser-<Proxy>\packs\MCTennisPack.mcpack``
    * Copy the ``player-profiles`` values from ``plugins\Geyser-<Proxy>\custom-skulls_mctennis.yml`` into the ``plugins\Geyser-<Proxy>\custom-skulls.yml`` file.
