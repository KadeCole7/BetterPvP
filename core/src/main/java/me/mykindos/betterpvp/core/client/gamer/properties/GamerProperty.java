package me.mykindos.betterpvp.core.client.gamer.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum GamerProperty {

    // Misc
    CLAN_MENU_ENABLED,

    // Chat modes
    CLAN_CHAT,
    ALLY_CHAT,

    // Currency
    BALANCE,
    FRAGMENTS,

    // Blocks
    BLOCKS_PLACED,
    BLOCKS_BROKEN,

    // Damage
    DAMAGE_DEALT,
    DAMAGE_TAKEN,

}
