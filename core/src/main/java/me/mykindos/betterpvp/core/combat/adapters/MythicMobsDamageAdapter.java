package me.mykindos.betterpvp.core.combat.adapters;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.mykindos.betterpvp.core.combat.events.CustomDamageEvent;
import me.mykindos.betterpvp.core.framework.adapter.PluginAdapter;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import javax.inject.Singleton;

@PluginAdapter("MythicMobs")
@Singleton
@BPvPListener
public class MythicMobsDamageAdapter implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void processDamageModidiers(CustomDamageEvent event) {
        var mobManager = MythicBukkit.inst().getMobManager();
        ActiveMob damagee = mobManager.getActiveMob(event.getDamagee().getUniqueId()).orElse(null);
        if (damagee != null) {
            if (damagee.getType().getDamageModifiers().containsKey(event.getCause().name())) {
                event.setDamage(event.getDamage() * damagee.getType().getDamageModifiers().get(event.getCause().name()));
            }
        }
    }

}
