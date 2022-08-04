package me.mykindos.betterpvp.core.framework.events.lunar;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.mykindos.betterpvp.core.framework.events.CustomCancellableEvent;
import org.bukkit.entity.Player;

@EqualsAndHashCode(callSuper = true)
@Data
public class LunarClientEvent extends CustomCancellableEvent {

    private final Player player;
    private final boolean registered;

}
