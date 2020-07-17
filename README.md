# UHCUtils Plugin

Main repo: [UHC Datapack](https://github.com/Bazinga9000/UHC-datapack)

This plugin adds commands via [1.13 Command API](https://github.com/JorelAli/1.13-Command-API) to help facilitate UHCs.

## Added Commands

* `/dispatch <cmd>` (alias `/cmd`): Runs a command. Allows for running almost any Spigot command in functions.
* `/regen <seed>`: Regenerates both dimensions `game` and `game_nether` using provided seed.
* `/for <var> in <range> (step <step>) run <cmd>`: Runs a command through a for loop.
  * `/for a in 1..9 run say hi` (posts `hi` 9x)
  * `/for a in 1..9 run say $a` (posts `1`, `2`, `3`, `4`, `5`, `6`, `7`, `8`, `9`)
  * `/for a in 1..9 run say \$a` (posts `$a` 9x)
  * `/for a in 1..9 step 2 run say $a` (posts `1`, `3`, `5`, `7`, `9`)
