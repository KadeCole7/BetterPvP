package me.mykindos.betterpvp.champions.champions.skills.skills.ranger.bow;

import com.destroystokyo.paper.ParticleBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.mykindos.betterpvp.champions.Champions;
import me.mykindos.betterpvp.champions.champions.ChampionsManager;
import me.mykindos.betterpvp.champions.champions.skills.data.SkillActions;
import me.mykindos.betterpvp.champions.champions.skills.types.PrepareArrowSkill;
import me.mykindos.betterpvp.core.components.champions.Role;
import me.mykindos.betterpvp.core.components.champions.SkillType;
import me.mykindos.betterpvp.core.effects.EffectType;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilFormat;
import me.mykindos.betterpvp.core.utilities.UtilMessage;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Singleton
@BPvPListener
public class LevitatingShot extends PrepareArrowSkill {

    private double baseDuration;

    private double durationIncreasePerLevel;

    private int levitationStrength;

    @Inject
    public LevitatingShot(Champions champions, ChampionsManager championsManager) {
        super(champions, championsManager);
    }

    @Override
    public String getName() {
        return "Levitating Shot";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "Left click with a Bow to prepare",
                "",
                "Your next arrow is tipped with mysterious magic causing",
                "the target to receive <effect>Levitation " + UtilFormat.getRomanNumeral(levitationStrength + 1) + "</effect> for <val>" + getDuration(level) + "</val> seconds",
                "",
                "Players with levitation are unable to use abilities",
                "",
                "Cooldown: <val>" + getCooldown(level)
        };
    }

    public double getDuration(int level) {
        return baseDuration + level * durationIncreasePerLevel;
    }

    @Override
    public Role getClassType() {
        return Role.RANGER;
    }

    @Override
    public SkillType getType() {

        return SkillType.BOW;
    }

    @Override
    public void activate(Player player, int level) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 2.5F, 2.0F);
        active.add(player.getUniqueId());
    }

    @Override
    public void onHit(Player damager, LivingEntity target, int level) {
        if (target instanceof Player player) {
            championsManager.getEffects().addEffect(player, EffectType.LEVITATION, levitationStrength + 1, (int) (getDuration(level) * 1000));
            UtilMessage.message(damager, getClassType().getName(), UtilMessage.deserialize("You hit <yellow>%s</yellow> with <green>%s %s</green>.", player.getName(), getName(), level));
            UtilMessage.message(player, getClassType().getName(), UtilMessage.deserialize("You were hit by <yellow>%s</yellow> with <green>%s %s</green>", damager.getName(), getName(), level));
        } else {
            target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, (int) (getDuration(level) * 20), levitationStrength + 1));
            UtilMessage.message(damager, getClassType().getName(), UtilMessage.deserialize("You hit <yellow>%s</yellow> with <green>%s %s</green>.", target.getName(), getName(), level));
        }
    }

    @Override
    public void displayTrail(Location location) {
        new ParticleBuilder(Particle.ENCHANTMENT_TABLE)
                .location(location)
                .count(3)
                .offset(0.1, 0.1, 0.1)
                .extra(0)
                .receivers(60)
                .spawn();
    }

    @Override
    public Action[] getActions() {
        return SkillActions.LEFT_CLICK;
    }

    @Override
    public double getCooldown(int level) {

        return cooldown - ((level - 1) * cooldownDecreasePerLevel);
    }
    @Override
    public void loadSkillConfig() {
        baseDuration = getConfig("baseDuration", 1.0, Double.class);
        durationIncreasePerLevel = getConfig("durationIncreasePerLevel", 0.5, Double.class);

        levitationStrength = getConfig("levitationStrength", 3, Integer.class);
    }
}
