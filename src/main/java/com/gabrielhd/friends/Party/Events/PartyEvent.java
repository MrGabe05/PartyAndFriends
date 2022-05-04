package com.gabrielhd.friends.Party.Events;

import com.gabrielhd.friends.Party.Party;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

@Getter
public class PartyEvent extends Event {

    private final Party party;
    private final ProxiedPlayer player;

    public PartyEvent(Party party, ProxiedPlayer player) {
        this.party = party;
        this.player = player;
    }
}
