package com.gabrielhd.friends.Party.Events;

import com.gabrielhd.friends.Party.Party;

public class PartyDisbandEvent extends PartyEvent {

    public PartyDisbandEvent(Party party) {
        super(party, party.getLeader());
    }
}
