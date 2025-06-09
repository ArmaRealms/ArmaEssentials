package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Kit;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.AdventureUtil;
import com.earth2me.essentials.utils.StringUtil;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class Commandkit extends EssentialsCommand {
    public Commandkit() {
        super("kit");
    }

    @Override
    public void run(final Server server, final User user, final String commandLabel, final String @NotNull [] args) throws Exception {
        boolean silent = false;
        if (args.length < 1) {
            final String kitList = ess.getKits().listKits(ess, user);
            user.sendTl(!kitList.isEmpty() ? "kits" : "noKits", AdventureUtil.parsed(kitList));
            throw new NoChargeException();
        } else if (args.length > 1 && user.isAuthorized("essentials.kit.others")) {
            if (args.length >= 3 && args[2].equalsIgnoreCase("-s")) {
                silent = true;
            }
            giveKits(getPlayer(server, user, args, 1), user, StringUtil.sanitizeString(args[0].toLowerCase(Locale.ENGLISH)).trim(), silent);
        } else {
            giveKits(user, user, StringUtil.sanitizeString(args[0].toLowerCase(Locale.ENGLISH)).trim(), silent);
        }
    }

    @Override
    public void run(final Server server, final CommandSource sender, final String commandLabel, final String @NotNull [] args) throws Exception {
        if (args.length < 2) {
            final String kitList = ess.getKits().listKits(ess, null);
            sender.sendTl(!kitList.isEmpty() ? "kits" : "noKits", AdventureUtil.parsed(kitList));
            throw new NoChargeException();
        } else {
            final User userTo = getPlayer(server, args, 1, true, false);

            for (final String kitName : args[0].toLowerCase(Locale.ENGLISH).split(",")) {
                new Kit(kitName, ess).expandItems(userTo);

                sender.sendTl("kitGiveTo", kitName, userTo.getDisplayName());
                userTo.sendTl("kitReceive", kitName);
            }
        }
    }

    private void giveKits(final User userTo, final User userFrom, final @NotNull String kitNames, final boolean silent) throws Exception {
        if (kitNames.isEmpty()) {
            throw new TranslatableException("kitNotFound");
        }

        final List<Kit> kits = new ArrayList<>();

        for (final String kitName : kitNames.split(",")) {
            if (kitName.isEmpty()) {
                throw new TranslatableException("kitNotFound");
            }

            final Kit kit = new Kit(kitName, ess);
            kit.checkPerms(userFrom);
            kit.checkDelay(userFrom);
            kit.checkAffordable(userFrom);
            kits.add(kit);
        }

        for (final Kit kit : kits) {
            try {

                kit.checkDelay(userFrom);
                kit.checkAffordable(userFrom);
                if (!kit.expandItems(userTo))
                    continue;
                kit.setTime(userFrom);
                kit.chargeUser(userTo);

                if (!userFrom.equals(userTo)) {
                    userFrom.sendTl("kitGiveTo", kit.getName(), userTo.getDisplayName());
                }

                if (!silent) {
                    userTo.sendTl("kitReceive", kit.getName());
                }

            } catch (final NoChargeException ex) {
                if (ess.getSettings().isDebug()) {
                    ess.getLogger().log(Level.INFO, "Soft kit error, abort spawning " + kit.getName(), ex);
                }
            } catch (final Exception ex) {
                ess.showError(userFrom.getSource(), ex, "\\ kit: " + kit.getName());
            }
        }
    }

    @Override
    protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String @NotNull [] args) {
        if (args.length == 1) {
            final List<String> options = new ArrayList<>();
            // TODO: Move all of this to its own method
            for (final String kitName : ess.getKits().getKitKeys()) {
                if (!user.isAuthorized("essentials.kits." + kitName)) { // Only check perm, not time or money
                    continue;
                }
                options.add(kitName);
            }
            return options;
        } else if (args.length == 2 && user.isAuthorized("essentials.kit.others")) {
            return getPlayers(server, user);
        } else if (args.length == 3 && user.isAuthorized("essentials.kit.others")) {
            final List<String> options = new ArrayList<>();
            options.add("-s");
            return options;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String @NotNull [] args) {
        if (args.length == 1) {
            // TODO: Move this to its own method
            return new ArrayList<>(ess.getKits().getKitKeys());
        } else if (args.length == 2) {
            return getPlayers(server, sender);
        } else if (args.length == 3 && sender.isAuthorized("essentials.kit.others.silent")) {
            final List<String> options = new ArrayList<>();
            options.add("-s");
            return options;
        } else {
            return Collections.emptyList();
        }
    }
}
