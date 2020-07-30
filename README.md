# UHCUtils Plugin

Main repo: [UHC Datapack](https://github.com/Bazinga9000/UHC-datapack)

This plugin adds commands via [1.13 Command API](https://github.com/JorelAli/1.13-Command-API) to help facilitate UHCs.

## Added Commands

* `/dispatch` (alias `/cmd`)
  * Runs a command. Allows for running almost any Spigot command in functions and `/execute`.
  * Syntax
    * `/dispatch <cmd>`
* `/for`
  * Runs a command through a for loop.
  * Syntax
    * `/for <var> in <range> (step <step>) run <cmd>`
  * Examples
    * `/for a in 1..9 run say hi` (posts `hi` 9x)
    * `/for a in 1..9 run say $a` (posts `1`, `2`, `3`, `4`, `5`, `6`, `7`, `8`, `9`)
    * `/for a in 1..9 run say \$a` (posts `$a` 9x)
    * `/for a in 1..9 step 2 run say $a` (posts `1`, `3`, `5`, `7`, `9`)
* `/let`
  * Runs a command with a variable that can be substituted for a scoreboard entry.
  * Syntax
    * `/let <var> = <player> <objective> run <cmd>`
  * Examples
    * `/let a = @s health run worldborder set $a`
* `/regen`
  * Regenerates both dimensions `game` and `game_nether` using provided seed.
  * Syntax
    * `/regen <seed>`
* `/runfn`
  * Sets player-objective entry to provided value, runs the function, and returns whatever was in that specified entry. Simplifies `/function` with argument syntax and allows for some complicated multi-command functionalities with `/for`.
  * Syntax
    * `/runfn <function> with <player> <objective> as <value>`
    * `/runfn <function> with <player> <objective> as <value player> <value objective>`
