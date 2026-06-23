package xy177.tt2.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import xy177.tt2.TT2;
import xy177.tt2.events.ScoutArmorEvents;

public class CommandTT2Debug extends CommandBase {

    @Override
    public String getName() {
        return "tt2debug";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/tt2debug scout_ranged <on|off|status>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length < 2 || !"scout_ranged".equals(args[0])) {
            sender.sendMessage(new TextComponentString(getUsage(sender)));
            return;
        }

        if ("on".equals(args[1]) || "true".equals(args[1])) {
            ScoutArmorEvents.debugScoutRanged = true;
        } else if ("off".equals(args[1]) || "false".equals(args[1])) {
            ScoutArmorEvents.debugScoutRanged = false;
        } else if (!"status".equals(args[1])) {
            sender.sendMessage(new TextComponentString(getUsage(sender)));
            return;
        }

        sender.sendMessage(new TextComponentString(
            "TT2 scout ranged debug: " + (ScoutArmorEvents.debugScoutRanged ? "on" : "off")
        ));
        if (TT2.logger != null) {
            TT2.logger.info("TT2 scout ranged debug: {}", ScoutArmorEvents.debugScoutRanged ? "on" : "off");
        }
    }
}
