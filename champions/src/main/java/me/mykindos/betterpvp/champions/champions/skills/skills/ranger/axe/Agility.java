package me.mykindos.betterpvp.champions.champions.skills.skills.ranger.axe;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.mykindos.betterpvp.champions.Champions;
import me.mykindos.betterpvp.champions.champions.ChampionsManager;
import me.mykindos.betterpvp.champions.champions.skills.Skill;
import me.mykindos.betterpvp.champions.champions.skills.data.SkillActions;
import me.mykindos.betterpvp.champions.champions.skills.types.CooldownSkill;
import me.mykindos.betterpvp.champions.champions.skills.types.InteractSkill;
import me.mykindos.betterpvp.core.combat.events.CustomDamageEvent;
import me.mykindos.betterpvp.core.components.champions.Role;
import me.mykindos.betterpvp.core.components.champions.SkillType;
import me.mykindos.betterpvp.core.framework.updater.UpdateEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilFormat;
import me.mykindos.betterpvp.core.utilities.UtilMessage;
import me.mykindos.betterpvp.core.utilities.UtilPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Singleton
@BPvPListener
public class Agility extends Skill implements InteractSkill, CooldownSkill, Listener {

    private final HashMap<UUID, Long> active = new HashMap<>();

    private double baseDuration;

    private double durationIncreasePerLevel;

    private double baseDamageReduction;

    private double damageReductionIncreasePerLevel;

    private int speedStrength;

    @Inject
    public Agility(Champions champions, ChampionsManager championsManager) {
        super(champions, championsManager);
    }

    @Override
    public String getName() {
        return "Agility";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "Right click with an Axe to activate",
                "",
                "Sprint with great agility, gaining",
                "<effect>Speed " + UtilFormat.getRomanNumeral(speedStrength + 1) + "</effect> for <val>" + (getDuration(level)) + "</val> seconds and ",
                "<stat>" + (getDamageReduction(level) * 100) + "%</stat> reduced damage while active",
                "",
                "Agility ends if you left click",
                "",
                "Cooldown: <val>" + getCooldown(level)
        };
    }

    public double getDuration(int level) {
        return baseDuration + level * durationIncreasePerLevel;
    }

    public double getDamageReduction(int level) {
        return baseDamageReduction + level * damageReductionIncreasePerLevel;
    }

    @Override
    public Role getClassType() {
        return Role.RANGER;
    }

    @Override
    public SkillType getType() {

        return SkillType.AXE;
    }

    @Override
    public double getCooldown(int level) {

        return cooldown - ((level - 1) * cooldownDecreasePerLevel);
    }

    @EventHandler
    public void endOnInteract(PlayerInteractEvent event) {
        if(event.getHand() != EquipmentSlot.HAND) return;
        if(!event.getAction().isLeftClick()) return;
        if (event.useItemInHand() == Event.Result.DENY) return;

        Player player = event.getPlayer();
        if (active.containsKey(player.getUniqueId())) {
            active.remove(player.getUniqueId());
            deactivate(player);
        }
    }

    @EventHandler
    public void onDamage(CustomDamageEvent event) {
        if (!(event.getDamagee() instanceof Player damagee)) return;
        if (active.containsKey(damagee.getUniqueId())) {
            int level = getLevel(damagee);
            event.setDamage(event.getDamage() * (1 - getDamageReduction(level)));
            event.setKnockback(false);
            if (event.getDamager() instanceof Player damager) {
                UtilMessage.message(damager, getClassType().getName(), damagee.getName() + " is using " + getName());
            }
            damagee.getWorld().playSound(damagee.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 0.5F, 2.0F);
        }
        if (!(event.getDamager() instanceof Player damager)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            if (active.containsKey(damager.getUniqueId())) {
                active.remove(damager.getUniqueId());
                deactivate(damager);
            }
        }
    }

    @UpdateEvent(delay = 250)
    public void onUpdate() {
        Iterator<Map.Entry<UUID, Long>> iterator = active.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                iterator.remove();
            } else {
                if (entry.getValue() - System.currentTimeMillis() <= 0) {
                    deactivate(player);
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void activate(Player player, int level) {
        if (!active.containsKey(player.getUniqueId())) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (getDuration(level) * 20), speedStrength));
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 0.5F);
            active.put(player.getUniqueId(), (long) (System.currentTimeMillis() + (getDuration(level) * 1000L)));
        }
    }

    public void deactivate(Player player) {
        UtilMessage.message(player, "Champions", UtilMessage.deserialize("<green>%s %s</green> has ended.", getName(), getLevel(player)));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 0.25F);
        if (!UtilPlayer.hasPotionEffect(player, PotionEffectType.SPEED, speedStrength + 1)) {
            player.removePotionEffect(PotionEffectType.SPEED);
        }
    }

    @Override
    public Action[] getActions() {
        return SkillActions.RIGHT_CLICK;
    }

    @Override
    public void loadSkillConfig() {
        baseDuration = getConfig("baseDuration", 3.0, Double.class);
        durationIncreasePerLevel = getConfig("durationIncreasePerLevel", 1.0, Double.class);
        baseDamageReduction = getConfig("baseDamageReduction", 0.60, Double.class);
        damageReductionIncreasePerLevel = getConfig("damageReductionIncreasePerLevel", 0.0, Double.class);

        speedStrength = getConfig("speedStrength", 1, Integer.class);
    }
}
