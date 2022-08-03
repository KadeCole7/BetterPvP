package me.mykindos.betterpvp.clans.energy;

import com.google.inject.Inject;
import me.mykindos.betterpvp.clans.energy.events.DegenerateEnergyEvent;
import me.mykindos.betterpvp.clans.energy.events.RegenerateEnergyEvent;
import me.mykindos.betterpvp.core.framework.updater.UpdateEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;

@BPvPListener
public class EnergyListener implements Listener {

    private final EnergyHandler energyHandler;

    @Inject
    public EnergyListener(EnergyHandler energyHandler) {
        this.energyHandler = energyHandler;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRegen(RegenerateEnergyEvent event) {
        energyHandler.regenerateEnergy(event.getPlayer(), event.getEnergy());

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDegen(DegenerateEnergyEvent event) {
        energyHandler.degenerateEnergy(event.getPlayer(), event.getEnergy());
    }

    @EventHandler
    public void handleRespawn(PlayerRespawnEvent event) {
        energyHandler.setEnergy(event.getPlayer(), 1.0f);
    }

    @EventHandler
    public void handleExp(PlayerExpChangeEvent event) {
        event.setAmount(0);
    }

    @UpdateEvent
    public void update() {
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (players.getExp() == 0.999) continue;
            energyHandler.updateEnergy(players);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_AIR) {
            energyHandler.use(player, "Attack", 0.02, false);
        }
    }
}
