package openspacecore.command.completer;

import openspacecore.Main;
import openspacecore.stellar.StellarObject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;
import java.util.stream.Collectors;

public class LaunchRocketTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            buildTargetList(completions, Main.stellars.entrySet());
            completions = completions.stream().filter(p -> p.startsWith(args[0])).collect(Collectors.toList());
        }
        if (args.length == 2) {
            completions.add("confirm");
        }
        return completions;
    }
    public void buildTargetList(List<String> completions, Set<Map.Entry<String, StellarObject>> list) {
        for (Map.Entry<String, StellarObject> stellare : list) {
            completions.add(stellare.getValue().getPathedName());
            if (stellare.getValue().getChildren().isEmpty()) continue;
            buildTargetList(completions, stellare.getValue().getChildren());
        }
    }
}
