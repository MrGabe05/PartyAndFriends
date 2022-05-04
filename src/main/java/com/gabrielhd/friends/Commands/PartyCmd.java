package com.gabrielhd.friends.Commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class PartyCmd extends Command {
    public PartyCmd() {
        super("party", "", "p");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if((sender instanceof ProxiedPlayer)) {
            ProxiedPlayer player = (ProxiedPlayer) sender;


        }
    }
}
