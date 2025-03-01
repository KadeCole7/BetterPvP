package me.mykindos.betterpvp.champions.champions.skills.skills.ranger.passives;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.mykindos.betterpvp.champions.Champions;
import me.mykindos.betterpvp.champions.champions.ChampionsManager;
import me.mykindos.betterpvp.champions.champions.skills.Skill;
import me.mykindos.betterpvp.champions.champions.skills.types.PassiveSkill;
import me.mykindos.betterpvp.core.combat.death.events.CustomDeathEvent;
import me.mykindos.betterpvp.core.combat.events.CustomDamageEvent;
import me.mykindos.betterpvp.core.components.champions.Role;
import me.mykindos.betterpvp.core.components.champions.SkillType;
import me.mykindos.betterpvp.core.components.champions.events.PlayerCanUseSkillEvent;
import me.mykindos.betterpvp.core.framework.updater.UpdateEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilMath;
import me.mykindos.betterpvp.core.utilities.UtilServer;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.WeakHashMap;

@Singleton
@BPvPListener
public class Longshot extends Skill implements PassiveSkill {

    private final WeakHashMap<Arrow, Location> arrows = new WeakHashMap<>();

    private double baseMaxDamage;

    private double maxDamageIncreasePerLevel;

    private double baseDamage;

    private double damageIncreasePerLevel;
    private double deathMessageThreshold;

    @Inject
    public Longshot(Champions champions, ChampionsManager championsManager) {
        super(champions, championsManager);
    }


    @Override
    public String getName() {
        return "Longshot";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "Shoot an arrow that deals an extra <stat>" + getDamage(level),
                "damage per block it travels",
                "",
                "Caps out at <val>" + getMaxDamage(level) + "</val> damage",
                "",
                "Cannot be used in own territory"};
    }

    public double getMaxDamage(int level) {
        return baseMaxDamage + level * maxDamageIncreasePerLevel;
    }

    public double getDamage(int level) {
        return baseDamage + level* damageIncreasePerLevel;
    }

    @Override
    public Role getClassType() {
        return Role.RANGER;
    }


    @UpdateEvent(delay=200)
    public void update() {
        Iterator<Arrow> it = arrows.keySet().iterator();
        while (it.hasNext()) {
            Arrow next = it.next();
            if (next == null) {
                it.remove();
            } else if (next.isDead()) {
                it.remove();
            } else {
                Location location = next.getLocation().add(new Vector(0, 0.25, 0));
                Particle.FIREWORKS_SPARK.builder().location(location).receivers(60).extra(0).spawn();

            }
        }

    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if(!(event.getProjectile() instanceof Arrow arrow)) return;

        int level = getLevel(player);
        if(level > 0) {
            PlayerCanUseSkillEvent skillEvent = UtilServer.callEvent(new PlayerCanUseSkillEvent(player, this));
            if(!skillEvent.isCancelled()) {
                arrows.put(arrow, arrow.getLocation());
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(CustomDamageEvent event) {
        if(!(event.getProjectile() instanceof Arrow arrow)) return;
        if(!(event.getDamager() instanceof Player damager)) return;
        if(!arrows.containsKey(arrow)) return;

        Location loc = arrows.remove(arrow);
        int level = getLevel(damager);
        double length = UtilMath.offset(loc, event.getDamagee().getLocation());
        double damage = Math.min(getMaxDamage(level), length * getDamage(level));

        event.setDamage(event.getDamage() + (damage));
        event.addReason(getName() + (length > deathMessageThreshold ? " (" + (int) length + " blocks)" : ""));

    }

    @EventHandler
    public void onDeath(CustomDeathEvent event) {

    }


    @Override
    public SkillType getType() {

        return SkillType.PASSIVE_A;
    }

    @Override
    public void loadSkillConfig(){
        baseMaxDamage = getConfig("baseMaxDamage", 12.0, Double.class);
        maxDamageIncreasePerLevel = getConfig("maxDamageIncreasePerLevel", 1.0, Double.class);
        baseDamage = getConfig("baseDamage", 0.25, Double.class);
        damageIncreasePerLevel = getConfig("damageIncreasePerLevel", 0.0, Double.class);

        deathMessageThreshold = getConfig("deathMessageThreshold", 40.0, Double.class);
    }

}
