package chip.uhc.util;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandException;
import org.bukkit.ChatColor;
import java.util.LinkedHashMap;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.CommandPermission;

public class App extends JavaPlugin {
    @Override
    public void onLoad() {
        // /dispatch, /cmd: runs a command, used to run any plugin command in /execute and functions
        LinkedHashMap<String, Argument> dispatchArgs = new LinkedHashMap<>();
        dispatchArgs.put("command", new GreedyStringArgument());

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
        LinkedHashMap<String, Argument> regenWorldArgs = new LinkedHashMap<>();
        regenWorldArgs.put("seed", new GreedyStringArgument());

        new CommandAPICommand("regen")
            .withArguments(regenWorldArgs)
            .withPermission(CommandPermission.OP)
            .executes((sender, args) -> {
                String seed = (String) args[0];
                if (seed.contains(" ")) seed = "\"" + seed + "\"";
                if (Bukkit.getWorld("game") != null && Bukkit.getWorld("game_nether") != null) {
                    sender.sendMessage("Reloading dimensions...");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvregen game -s " + seed);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvconfirm");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvregen game_nether -s " + seed);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvconfirm");
                    sender.sendMessage(ChatColor.GREEN + "Both dimensions have been regenerated successfully.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Dimensions game and game_nether must both exist in order to run this command.");
                }
            })
            .register();
    }
}
