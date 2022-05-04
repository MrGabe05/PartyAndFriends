package com.gabrielhd.friends.Party.Events;

import com.gabrielhd.friends.Party.Party;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PartyInviteEvent extends PartyEvent {

    public PartyInviteEvent(Party party, ProxiedPlayer player) {
        super(party, player);
    }
}
