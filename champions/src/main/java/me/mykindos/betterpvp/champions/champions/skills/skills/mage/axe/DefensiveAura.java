package me.mykindos.betterpvp.champions.champions.skills.skills.mage.axe;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.mykindos.betterpvp.champions.Champions;
import me.mykindos.betterpvp.champions.champions.ChampionsManager;
import me.mykindos.betterpvp.champions.champions.skills.Skill;
import me.mykindos.betterpvp.champions.champions.skills.data.SkillActions;
import me.mykindos.betterpvp.champions.champions.skills.types.CooldownSkill;
import me.mykindos.betterpvp.champions.champions.skills.types.InteractSkill;
import me.mykindos.betterpvp.core.components.champions.Role;
import me.mykindos.betterpvp.core.components.champions.SkillType;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilFormat;
import me.mykindos.betterpvp.core.utilities.UtilPlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Singleton
@BPvPListener
public class DefensiveAura extends Skill implements InteractSkill, CooldownSkill {

    private double baseRadius;
    
    private double radiusIncreasePerLevel;

    private double baseDuration;

    private double durationIncreasePerLevel;

    private int healthBoostStrength;

    @Inject
    public DefensiveAura(Champions champions, ChampionsManager championsManager) {
        super(champions, championsManager);
    }

    @Override
    public String getName() {
        return "Defensive Aura";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "Right click with an Axe to activate",
                "",
                "Gives you, and all allies within <val>" + getRadius(level) + "</val> blocks",
                "<effect>Health Boost " + UtilFormat.getRomanNumeral(healthBoostStrength + 1) + "</effect> for <stat>" + baseDuration + "</stat> seconds",
                "",
                "Cooldown: <val>" + getCooldown(level)
        };
    }

    public double getRadius(int level) {
        return baseRadius + level * radiusIncreasePerLevel;
    }

    public double getDuration(int level) {
        return baseDuration + durationIncreasePerLevel;
    }

    @Override
    public Role getClassType() {
        return Role.MAGE;
    }

    @Override
    public SkillType getType() {

        return SkillType.AXE;
    }


    @Override
    public double getCooldown(int level) {
        return cooldown - ((level - 1) * cooldownDecreasePerLevel);
    }


    @Override
    public void activate(Player player, int level) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, (int) (getDuration(level) * 20), healthBoostStrength));
        AttributeInstance playerMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (playerMaxHealth != null) {
            player.setHealth(Math.min(player.getHealth() + 4 * (healthBoostStrength + 1), playerMaxHealth.getValue()));
            for (Player target : UtilPlayer.getNearbyAllies(player, player.getLocation(), getRadius(level))) {

                target.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, (int) (getDuration(level) * 20), healthBoostStrength));
                AttributeInstance targetMaxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (targetMaxHealth != null) {
                    target.setHealth(Math.min(target.getHealth() + 4 * (healthBoostStrength + 1), targetMaxHealth.getValue()));
                }
            }
        }
    }

    @Override
    public void loadSkillConfig() {
        baseDuration = getConfig("baseDuration", 10.0, Double.class);
        durationIncreasePerLevel = getConfig("durationIncreasePerLevel", 0.0, Double.class);
        baseRadius = getConfig("baseRadius", 6.0, Double.class);
        radiusIncreasePerLevel = getConfig("radiusIncreasePerLevel", 1.0, Double.class);
        healthBoostStrength = getConfig("radiusIncreasePerLevel", 0, Integer.class);
    }

    @Override
    public Action[] getActions() {
        return SkillActions.RIGHT_CLICK;
    }
}
