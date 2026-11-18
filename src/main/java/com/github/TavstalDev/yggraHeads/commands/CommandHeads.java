package com.github.TavstalDev.yggraHeads.commands;

import com.github.TavstalDev.yggraHeads.YggraHeads;
import com.github.TavstalDev.yggraHeads.gui.CategoryGUI;
import com.github.TavstalDev.yggraLib.api.localization.ITranslationService;
import com.github.TavstalDev.yggraLib.api.logger.YggraLogger;
import com.github.TavstalDev.yggraLib.api.utils.MapUtil;
import com.github.TavstalDev.yggraLib.bukkit.commands.SubCommand;
import com.github.TavstalDev.yggraLib.bukkit.gui.GuiService;
import com.github.TavstalDev.yggraLib.bukkit.services.IChatService;
import com.github.TavstalDev.yggraLib.bukkit.utils.AdventureUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command executor for the {@code /heads} command.
 * <p>
 * Handles opening the heads GUI and subcommands such as help, version, and reload.
 */
@SuppressWarnings("FieldCanBeLocal")
public class CommandHeads implements CommandExecutor {
    /** Base command label handled by this executor. */
    private final String baseCommand = "heads";

    /**
     * Executes the given command, returning its success.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        IChatService chatService = YggraHeads.instance.getService(IChatService.class);
        GuiService guiService = YggraHeads.instance.getService(GuiService.class);
        if (chatService == null || guiService == null)
            return false;

        if (sender instanceof ConsoleCommandSender) {
            chatService.sendCommandReply(sender, "commands.console-caller");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("yggraHeads.commands.heads")) {
            chatService.sendCommandReply(player, "general.no-permission");
            return true;
        }

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "help":
                case "?": {
                    int page = 1;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (Exception ex) {
                            chatService.sendCommandReply(player, "commandsc.common.invalid-page");
                            return true;
                        }
                    }

                    help(player, page);
                    return true;
                }
                case "version": {
                    Map<String, Object> parameters = new HashMap<>();

                    parameters.put("version", YggraHeads.instance.version());
                    chatService.sendLocalizedMsg(player, "commands.version.current", parameters);

                    YggraHeads.instance.isUpToDate().thenAccept(upToDate -> {
                        if (upToDate == null || upToDate) {
                            chatService.sendLocalizedMsg(player, "commands.version.up-to-date");
                        } else {
                            chatService.sendLocalizedMsg(player, "commands.version.outdated", MapUtil.of("link", YggraHeads.instance.downloadUrl()));
                        }
                    }).exceptionally(e -> {
                        chatService.sendLocalizedMsg(player, "commands.version.exception");
                        return null;
                    });
                    return true;
                }
                case "reload": {
                    if (!player.hasPermission("yggraHeads.commands.reload")) {
                        chatService.sendLocalizedMsg(player, "general.no-permission");
                        return true;
                    }

                    YggraHeads.instance.reload();
                    chatService.sendLocalizedMsg(player, "commands.reload.done");
                    return true;
                }
            }

            chatService.sendLocalizedMsg(player, "commands.common.invalid-arguments");
            return true;
        }
        
        guiService.open(player, CategoryGUI.ID);
        return true;
    }
    
    /** Subcommand definitions used for help rendering and permission checks. */
    private final List<SubCommand> _subCommands = new ArrayList<SubCommand>() {
        {
            // HELP
            add(new SubCommand("help", "", MapUtil.of(
                    "syntax", null,
                    "description", "commands.help.desc"
            )));
            // VERSION
            add(new SubCommand("version", "", MapUtil.of(
                    "syntax", null,
                    "description", "commands.version.desc"
            )));
            // RELOAD
            add(new SubCommand("reload", "yggraHeads.commands.reload", MapUtil.of(
                    "syntax", null,
                    "description", "commands.reload.desc"
            )));
            // OPEN
            add(new SubCommand("", "", MapUtil.of(
                    "syntax", null,
                    "description", "commands.gui.desc"
            )));
        }
    };

    /**
     * Sends a paginated help message for the {@code /heads} command.
     *
     * @param sender the command sender receiving the help output
     * @param page   the requested help page (1-based)
     */
    private void help(CommandSender sender, int page) {
        ITranslationService translator = YggraHeads.instance.getService(ITranslationService.class);
        IChatService chatService = YggraHeads.instance.getService(IChatService.class);
        if (translator == null || chatService == null)
            return;
        
        int maxPage = 1 + (_subCommands.size() / 15);

        // Ensure the page number is within valid bounds
        if (page > maxPage)
            page = maxPage;
        if (page < 1)
            page = 1;
        int finalPage = page;

        // Send the help menu title and info
        chatService.sendCommandReply(sender, "commands.help.title", MapUtil.of(
                        "current_page", finalPage,
                        "max_page", maxPage
                )
        );
        chatService.sendCommandReply(sender, "commands.help.info");

        boolean reachedEnd = false;
        int itemIndex = 0;

        // Display up to 15 subcommands per page
        for (int i = 0; i < 15; i++) {
            int index = itemIndex + (page - 1) * 15;
            if (index >= _subCommands.size()) {
                reachedEnd = true;
                break;
            }
            itemIndex++;

            SubCommand subCommand = _subCommands.get(index);
            if (!subCommand.hasPermission(sender)) {
                i--;
                continue;
            }

            subCommand.sendHelp(YggraHeads.instance, sender, baseCommand);
        }

        // Display navigation buttons for the help menu
        String previousBtn, nextBtn, bottomMsg;
        if (sender instanceof Player)
        {
            Player player = (Player)sender; 
            previousBtn = translator.localize(player.getUniqueId(), "commands.help.prev-btn");
            nextBtn = translator.localize(player.getUniqueId(), "commands.help.next-btn");
            bottomMsg = translator.localize(player.getUniqueId(), "commands.help.bottom", MapUtil.of(
                    "current_page", page,
                    "max_page", maxPage
            ));
        }
        else {
            previousBtn = translator.localize("commands.help.prev-btn");
            nextBtn = translator.localize("commands.help.next-btn");
            bottomMsg = translator.localize("commands.help.bottom", MapUtil.of(
                    "current_page", page,
                    "max_page", maxPage
            ));
        }

        Map<String, Component> bottomParams = new HashMap<>();
        if (page > 1)
            bottomParams.put("previous_btn", AdventureUtils.parse(previousBtn, true)
                    .clickEvent(ClickEvent.runCommand(String.format("/%s help %s", baseCommand, page - 1))));
        else
            bottomParams.put("previous_btn", AdventureUtils.parse(previousBtn, true));

        if (!reachedEnd && maxPage >= page + 1)
            bottomParams.put("next_btn", AdventureUtils.parse(nextBtn, true)
                    .clickEvent(ClickEvent.runCommand(String.format("/%s help %s", baseCommand, page + 1))));
        else
            bottomParams.put("next_btn", AdventureUtils.parse(nextBtn, true));

        Component bottomComp = chatService.buildWithButtons(bottomMsg, bottomParams);
        chatService.sendCommandReply(sender, bottomComp);
    }
}
