package me.mykindos.betterpvp.core.menu.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.mykindos.betterpvp.core.framework.events.CustomCancellableEvent;
import me.mykindos.betterpvp.core.menu.Menu;
import org.bukkit.entity.Player;

@EqualsAndHashCode(callSuper = true)
@Data
public class MenuCloseEvent extends CustomCancellableEvent {

    private final Player player;
    private final Menu menu;

}