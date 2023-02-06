package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.User;
import org.bukkit.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.earth2me.essentials.I18n.tl;

public class Commanddelhome extends EssentialsCommand {
    public Commanddelhome() {
        super("delhome");
    }

    @Override
    public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }

        User usersHome = ess.getUser(sender.getPlayer());
        final String name;
        final String[] expandedArg;

        //Allowing both formats /delhome khobbits house | /delhome khobbits:house
        final String[] nameParts = args[0].split(":");
        if (nameParts[0].length() != args[0].length()) {
            expandedArg = nameParts;
        } else {
            expandedArg = args;
        }

        if (expandedArg.length > 1 && (usersHome == null || usersHome.isAuthorized("essentials.delhome.others"))) {
            usersHome = getPlayer(server, expandedArg, 0, true, true);
            name = expandedArg[1];
        } else if (usersHome == null) {
            throw new NotEnoughArgumentsException();
        } else {
            name = expandedArg[0];
        }

        if (name.equalsIgnoreCase("bed")) {
            throw new Exception(tl("invalidHomeName"));
        }
        if (ess.getSettings().isConfirmHomeDelete() && usersHome.hasHome(name) && (!name.equals(usersHome.getLastDelhomeConfirmation()) || name.equals(usersHome.getLastDelhomeConfirmation()) && System.currentTimeMillis() - usersHome.getLastDelhomeConfirmationTimestamp() > TimeUnit.MINUTES.toMillis(2))) {
            usersHome.setLastDelhomeConfirmation(name);
            usersHome.setLastDelhomeConfirmationTimestamp();
            usersHome.sendMessage(tl("delhomeConfirmation", name));
            return;
        }

        usersHome.delHome(name.toLowerCase(Locale.ENGLISH));
        sender.sendMessage(tl("deleteHome", name));
        usersHome.setLastDelhomeConfirmation(null);
    }

    @Override
    protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
        final IUser user = sender.getUser(ess);
        final boolean canDelOthers = sender.isAuthorized("essentials.delhome.others", ess);
        if (args.length == 1) {
            final List<String> homes = user == null ? new ArrayList<>() : user.getHomes();
            if (canDelOthers) {
                final int sepIndex = args[0].indexOf(':');
                if (sepIndex < 0) {
                    getPlayers(server, sender).forEach(player -> homes.add(player + ":"));
                } else {
                    final String namePart = args[0].substring(0, sepIndex);
                    final User otherUser;
                    try {
                        otherUser = getPlayer(server, new String[] {namePart}, 0, true, true);
                    } catch (final Exception ex) {
                        return homes;
                    }
                    otherUser.getHomes().forEach(home -> homes.add(namePart + ":" + home));
                }
            }
            return homes;
        } else {
            return Collections.emptyList();
        }
    }
}
