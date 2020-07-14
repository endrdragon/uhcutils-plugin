package chip.uhc.util;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.LinkedHashMap;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.CommandPermission;

public class App extends JavaPlugin {
    @Override
    public void onLoad() {
        LinkedHashMap<String, Argument> dispatchArgs = new LinkedHashMap<>();
        dispatchArgs.put("command", new GreedyStringArgument());
        
        // /dispatch, /cmd: runs a command, used to run any plugin command in /execute and functions
        new CommandAPICommand("dispatch")
            .withArguments(dispatchArgs)
            .withPermission(CommandPermission.OP)
            .withAliases("cmd")
            .executes((sender, args) -> {
                String cmd = (String) args[0];
                Bukkit.dispatchCommand(sender, cmd);
            })
            .register();
    }
}
