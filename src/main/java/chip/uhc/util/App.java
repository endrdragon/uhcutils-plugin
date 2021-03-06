package chip.uhc.util;

import org.bukkit.Bukkit;
//import org.bukkit.GameMode;
//import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandException;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Objective;
import org.bukkit.entity.Player;
import java.util.*;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.EntitySelector;
import dev.jorel.commandapi.arguments.ScoreHolderArgument.ScoreHolderType;
import dev.jorel.commandapi.wrappers.IntegerRange;
import dev.jorel.commandapi.wrappers.FunctionWrapper;
import dev.jorel.commandapi.CommandPermission;

public class App extends JavaPlugin {
    @Override
    public void onLoad() {
        CommandAPI.onLoad(false);
        ConsoleCommandSender console = Bukkit.getConsoleSender();

        // /dispatch, /cmd: runs a command, used to run any plugin command in /execute and functions
        List<Argument> dispatchArgs = new ArrayList<>();
        dispatchArgs.add(new GreedyStringArgument("command"));

        new CommandAPICommand("dispatch")
            .withArguments(dispatchArgs)
            .withPermission(CommandPermission.OP)
            .withAliases("cmd")
            .executes((sender, args) -> {
                String cmd = (String) args[0];
                Bukkit.dispatchCommand(sender, cmd);
            })
            .register();

        // /regen <seed>: regens dims game & game_nether with provided seed
        List<Argument> regenWorldArgs = new ArrayList<>();
        regenWorldArgs.add(new GreedyStringArgument("seed"));

        new CommandAPICommand("regen")
            .withArguments(regenWorldArgs)
            .withPermission(CommandPermission.OP)
            .executes((sender, args) -> {
                String seed = (String) args[0];
                if (seed.contains(" ")) seed = "\"" + seed + "\"";
                if (Bukkit.getWorld("game") != null && Bukkit.getWorld("game_nether") != null) {
                    sender.sendMessage("Reloading dimensions...");
                    Bukkit.dispatchCommand(console, "mvregen game -s " + seed);
                    Bukkit.dispatchCommand(console, "mvconfirm");
                    Bukkit.dispatchCommand(console, "mvregen game_nether -s " + seed);
                    Bukkit.dispatchCommand(console, "mvconfirm");
                    sender.sendMessage(ChatColor.GREEN + "Both dimensions have been regenerated successfully.");
                } else {
                    CommandAPI.fail("Dimensions game and game_nether must both exist in order to run this command.");
                }
            })
            .register();

        // /for <var> in m..n run <cmd>
        // /for <var> in m..n step s run <cmd>

        // examples:
        // /for i in 0..10 run say $i         # (0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        // /for i in 0..10 step 2 run say $i  # (0, 2, 4, 6, 8, 10)
        List<Argument> forArgs = new ArrayList<>();
        forArgs.add(new StringArgument("var"));
        forArgs.add(new LiteralArgument("in"));
        forArgs.add(new IntegerRangeArgument("range"));
        forArgs.add(new LiteralArgument("run"));
        forArgs.add(new GreedyStringArgument("cmd"));

        List<Argument> forStepArgs = new ArrayList<>();
        forStepArgs.add(new StringArgument("var"));
        forStepArgs.add(new LiteralArgument("in"));
        forStepArgs.add(new IntegerRangeArgument("range"));
        forStepArgs.add(new LiteralArgument("step"));
        forStepArgs.add(new IntegerArgument("stepamt", 1));
        forStepArgs.add(new LiteralArgument("run"));
        forStepArgs.add(new GreedyStringArgument("cmd"));

        new CommandAPICommand("for")
            .withArguments(forArgs)
            .withPermission(CommandPermission.OP)
            .executes((sender, args) -> {
                String vname = (String) args[0];
                IntegerRange vrange = (IntegerRange) args[1];
                String cmd = (String) args[2];

                try {
                    for (int i = vrange.getLowerBound(); i <= vrange.getUpperBound(); i++) {
                        String itercmd = cmd;
                        itercmd = itercmd.replaceAll("(?<!\\\\)\\$" + vname, Integer.toString(i));
                        itercmd = itercmd.replaceAll("\\\\\\$", "\\$");
                        Bukkit.dispatchCommand(console, itercmd);
                    }
                } catch (CommandException e) {
                    CommandAPI.fail("An error occurred while running the commands: " + e.getMessage());
                }
            })
            .register();

            new CommandAPICommand("for")
            .withArguments(forStepArgs)
            .withPermission(CommandPermission.OP)
            .executes((sender, args) -> {
                String vname = (String) args[0];
                IntegerRange vrange = (IntegerRange) args[1];
                int step = (int) args[2];
                String cmd = (String) args[3];

                try {
                    for (int i = vrange.getLowerBound(); i <= vrange.getUpperBound(); i += step) {
                        String itercmd = cmd;
                        itercmd = itercmd.replaceAll("(?<!\\\\)\\$" + vname, Integer.toString(i));
                        itercmd = itercmd.replaceAll("\\\\\\$", "\\$");
                        Bukkit.dispatchCommand(console, itercmd);
                    }
                } catch (CommandException e) {
                    sender.sendMessage(ChatColor.RED + "An error occurred while running the commands: " + e.getMessage());
                }
            })
            .register();

            // /runfn <fn> with <player> <objective> as <value> || /runfn <fn> with <player> <objective> as <player> <objective>
            // this fn sets scoreboard entry to <value> / <player> <objective>, runs the function, and returns the value on said scoreboard entry.
            // this simplifies syntax for functions with args and allows them to be used with /for
            // uhhhh, if you want to use multiple arguments just curry I guess
            List<Argument> runfnArgs = new ArrayList<>();
            runfnArgs.add(new FunctionArgument("function"));
            runfnArgs.add(new LiteralArgument("with"));
            runfnArgs.add(new ScoreHolderArgument("player", ScoreHolderType.SINGLE));
            runfnArgs.add(new ObjectiveArgument("objective"));
            runfnArgs.add(new LiteralArgument("as"));
            runfnArgs.add(new IntegerArgument("value"));

            List<Argument> runfnEntryArgs = new ArrayList<>();
            runfnEntryArgs.add(new FunctionArgument("function"));
            runfnEntryArgs.add(new LiteralArgument("with"));
            runfnEntryArgs.add(new ScoreHolderArgument("player", ScoreHolderType.SINGLE));
            runfnEntryArgs.add(new ObjectiveArgument("objective"));
            runfnEntryArgs.add(new LiteralArgument("as"));
            runfnEntryArgs.add(new ScoreHolderArgument("player value", ScoreHolderType.SINGLE));
            runfnEntryArgs.add(new ObjectiveArgument("objective value"));

            new CommandAPICommand("runfunction")
            .withArguments(runfnArgs)
            .withPermission(CommandPermission.OP)
            .withAliases("runfn")
            .executes((sender, args) -> {
                FunctionWrapper[] fns = (FunctionWrapper[]) args[0];
                String inputPl = (String) args[1];
                String inputOb = (String) args[2];
                int inputVal = (int) args[3];
                Score inputScore = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(inputOb).getScore(inputPl);
                inputScore.setScore(inputVal);
                // run fns after arg has been set
                for (FunctionWrapper fn : fns) {
                    fn.run();
                }
                // return score for use w/ execute store
                try {
                    int retVal = inputScore.getScore();
                    if (sender instanceof Player && !(sender instanceof ProxiedCommandSender)) sender.sendMessage("Function returned " + Integer.toString(retVal));
                    return retVal; 
                } catch (IllegalArgumentException | IllegalStateException e) {
                    CommandAPI.fail("Entry no longer exists");
                }
                return 0;
            })
            .register();

            new CommandAPICommand("runfunction")
            .withArguments(runfnEntryArgs)
            .withPermission(CommandPermission.OP)
            .withAliases("runfn")
            .executes((sender, args) -> {
                FunctionWrapper[] fns = (FunctionWrapper[]) args[0];
                String inputPl = (String) args[1];
                String inputOb = (String) args[2];
                String valuePl = (String) args[3];
                String valueOb = (String) args[4];
                
                // get (pl obj) slot and set it to val, effectively acting as the arg
                Score inputScore = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(inputOb).getScore(inputPl);
                int inputVal = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(valueOb).getScore(valuePl).getScore();
                
                inputScore.setScore(inputVal);
                // run fns after arg has been set
                for (FunctionWrapper fn : fns) {
                    fn.run();
                }
                // return score for use w/ execute store
                try {
                    int retVal = inputScore.getScore();
                    if (sender instanceof Player && !(sender instanceof ProxiedCommandSender)) sender.sendMessage("Function returned " + Integer.toString(retVal));
                    return retVal; 
                } catch (IllegalArgumentException | IllegalStateException e) {
                    CommandAPI.fail("Entry no longer exists");
                }
                return 0;
            })
            .register();
        
            // /let <var> = <player> <objective> run <cmd>: substs var with entry in cmd, same syntax as /for
            // /let <var> = <player> <objective> <scale: double> run <cmd>: multiplies by scale
            List<Argument> letArgs = new ArrayList<>();
            letArgs.add(new StringArgument("var"));
            letArgs.add(new LiteralArgument("="));
            letArgs.add(new ScoreHolderArgument("player", ScoreHolderType.SINGLE));
            letArgs.add(new ObjectiveArgument("objective"));
            letArgs.add(new LiteralArgument("run"));
            letArgs.add(new GreedyStringArgument("cmd"));

            List<Argument> letScaleArgs = new ArrayList<>();
            letScaleArgs.add(new StringArgument("var"));
            letScaleArgs.add(new LiteralArgument("="));
            letScaleArgs.add(new ScoreHolderArgument("player", ScoreHolderType.SINGLE));
            letScaleArgs.add(new ObjectiveArgument("objective"));
            letScaleArgs.add(new DoubleArgument("scale"));
            letScaleArgs.add(new LiteralArgument("run"));
            letScaleArgs.add(new GreedyStringArgument("cmd"));

            new CommandAPICommand("let")
            .withArguments(letArgs)
            .withPermission(CommandPermission.OP)
            .executes((sender, args) -> {
                String vname = (String) args[0];
                String valPl = (String) args[1];
                String valOb = (String) args[2];
                String cmd = (String) args[3];

                int v = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(valOb).getScore(valPl).getScore();
                cmd = cmd.replaceAll("(?<!\\\\)\\$" + vname, Integer.toString(v));
                cmd = cmd.replaceAll("\\\\\\$", "\\$");
                Bukkit.dispatchCommand(sender, cmd);
            })
            .register();

            new CommandAPICommand("let")
            .withArguments(letScaleArgs)
            .withPermission(CommandPermission.OP)
            .executes((sender, args) -> {
                String vname = (String) args[0];
                String valPl = (String) args[1];
                String valOb = (String) args[2];
                double scale = (Double) args[3];
                String cmd = (String) args[4];

                int u = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(valOb).getScore(valPl).getScore();
                double v = (double) u * scale;
                cmd = cmd.replaceAll("(?<!\\\\)\\$" + vname, Double.toString(v));
                cmd = cmd.replaceAll("\\\\\\$", "\\$");
                Bukkit.dispatchCommand(sender, cmd);
            })
            .register();

            // /respawn <targets> [destination]: respawns player in UHC, bypassing any forced spectator checks
            // targeted players will respawn at their death location or specified destination
            // used for unfair death
            List<Argument> respawnArgs = new ArrayList<>();
            respawnArgs.add(new EntitySelectorArgument("targets", EntitySelector.MANY_PLAYERS));

            List<Argument> respawnCoordArgs = new ArrayList<>();
            respawnCoordArgs.add(new EntitySelectorArgument("targets", EntitySelector.MANY_PLAYERS));
            respawnCoordArgs.add(new LocationArgument("destination", LocationType.PRECISE_POSITION));

            new CommandAPICommand("respawn")
            .withArguments(respawnArgs)
            .withPermission(CommandPermission.OP)
            .executes((sender, args) -> {
                int game_started = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("uhc.internal").getScore("game_started").getScore();
                if (game_started == 1) {
                    @SuppressWarnings("unchecked")
                    Collection<Player> targets = (Collection<Player>) args[0];

                    Objective deaths = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("uhc.deaths");
                    Objective alive = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("uhc.team_alive");
                    for (Player player : targets) {
                        // reset scoreboards
                        String name = player.getName();
                        deaths.getScore(name).setScore(0);
                        String teamid = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(name).getName().substring(4);
                        alive.getScore(teamid).setScore(1);
                        // respawn player
                        Location spawn = player.getBedSpawnLocation();
                        if (spawn == null) spawn = Bukkit.getWorld("game").getSpawnLocation();
                        player.teleport(spawn);
                        player.setGameMode(GameMode.SURVIVAL);
                    }

                } else {
                    CommandAPI.fail("Game has not started.");
                }
            })
            .register();

            new CommandAPICommand("respawn")
            .withArguments(respawnCoordArgs)
            .withPermission(CommandPermission.OP)
            .executes((sender, args) -> {
                int game_started = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("uhc.internal").getScore("game_started").getScore();
                if (game_started == 1) {
                    @SuppressWarnings("unchecked")
                    Collection<Player> targets = (Collection<Player>) args[0];
                    Location spawn = (Location) args[1];

                    Objective deaths = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("uhc.deaths");
                    Objective alive = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("uhc.team_alive");
                    for (Player player : targets) {
                        // reset scoreboards
                        String name = player.getName();
                        deaths.getScore(name).setScore(0);
                        String teamid = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(name).getName().substring(4);
                        alive.getScore(teamid).setScore(1);
                        // respawn player
                        player.teleport(spawn);
                        player.setGameMode(GameMode.SURVIVAL);
                    }

                } else {
                    CommandAPI.fail("Game has not started.");
                }
            })
            .register();
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable(this);
    }
}
