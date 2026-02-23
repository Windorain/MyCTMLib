package com.github.wohaopa.MyCTMLib.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.github.wohaopa.MyCTMLib.MyCTMLib;
import com.github.wohaopa.MyCTMLib.resource.BlockTextureDumpUtil;
import com.github.wohaopa.MyCTMLib.resource.DebugErrorCollector;
import com.github.wohaopa.MyCTMLib.resource.RegisteredSpritesDumpUtil;
import com.github.wohaopa.MyCTMLib.resource.RegistryDumpUtil;
import com.github.wohaopa.MyCTMLib.resource.ResourceLoadTrace;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CTMLibClientCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "ctmlib";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/ctmlib <debug|dump <registry|sprites|textures|errors|load|hud> [path]>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "debug", "dump");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("dump")) {
            return getListOfStringsMatchingLastWord(
                args,
                "registry",
                "sprites",
                "textures",
                "errors",
                "load",
                "hud");
        }
        return Collections.emptyList();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            send(sender, EnumChatFormatting.RED + getCommandUsage(sender));
            return;
        }
        switch (args[0].toLowerCase()) {
            case "debug" -> processDebug(sender);
            case "dump" -> processDump(sender, args);
            default -> send(sender, EnumChatFormatting.RED + "Unknown subcommand: " + args[0]);
        }
    }

    private void processDebug(ICommandSender sender) {
        MyCTMLib.debugMode = !MyCTMLib.debugMode;
        send(sender, "debug = " + MyCTMLib.debugMode);
    }

    private void processDump(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            send(sender, EnumChatFormatting.RED + "Usage: /ctmlib dump <registry|sprites|textures|errors|load|hud> [path]");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "registry" -> processDumpRegistry(sender);
            case "sprites" -> processDumpRegisteredSprites(sender, args);
            case "textures" -> processDumpTextures(sender);
            case "errors" -> processDumpDebugErrors(sender);
            case "load" -> processDumpDebugLoad(sender);
            case "hud" -> processDumpHUD(sender, args);
            default -> send(sender, EnumChatFormatting.RED + "Unknown dump type: " + args[1]);
        }
    }

    private void processDumpRegistry(ICommandSender sender) {
        File f = new File(Minecraft.getMinecraft().mcDataDir, "config/ctmlib_registry_dump.json");
        RegistryDumpUtil.dumpToFile(f);
        send(sender, "Registry dump written to " + f.getAbsolutePath());
    }

    private void processDumpRegisteredSprites(ICommandSender sender, String[] args) {
        String path = args.length >= 3 ? args[2] : "config/ctmlib_registered_sprites_dump.json";
        File f = new File(Minecraft.getMinecraft().mcDataDir, path);
        RegisteredSpritesDumpUtil.dumpToFile(f);
        send(sender, "Registered sprites dump written to " + f.getAbsolutePath());
    }

    private void processDumpTextures(ICommandSender sender) {
        File f = new File(Minecraft.getMinecraft().mcDataDir, "config/ctmlib_block_texture_dump.json");
        BlockTextureDumpUtil.dumpToFile(f);
        send(sender, "Block texture dump written to " + f.getAbsolutePath());
    }

    private void processDumpDebugErrors(ICommandSender sender) {
        File f = new File(Minecraft.getMinecraft().mcDataDir, "config/ctmlib_debug_errors.json");
        DebugErrorCollector.getInstance()
            .flushToFile(f);
        send(sender, "Debug errors written to " + f.getAbsolutePath());
    }

    private void processDumpDebugLoad(ICommandSender sender) {
        File f = new File(Minecraft.getMinecraft().mcDataDir, "config/ctmlib_debug_load.json");
        ResourceLoadTrace.getInstance()
            .flushToFile(f);
        send(sender, "Debug load trace written to " + f.getAbsolutePath());
    }

    private void processDumpHUD(ICommandSender sender, String[] args) {
        String path = args.length >= 3 ? args[2] : "config/ctmlib_hud_dump.json";
        File f = new File(Minecraft.getMinecraft().mcDataDir, path);
        
        try {
            DebugOverlayDumpUtil.dumpCurrentHUDToFile(f);
            send(sender, EnumChatFormatting.GREEN + "HUD dump written to " + f.getAbsolutePath());
        } catch (Exception e) {
            send(sender, EnumChatFormatting.RED + "Failed to dump HUD: " + e.getMessage());
            com.github.wohaopa.MyCTMLib.MyCTMLib.LOG.warn("HUD dump failed", e);
        }
    }

    private static void send(ICommandSender sender, String msg) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI()
            .printChatMessage(new ChatComponentText(msg));
    }
}
