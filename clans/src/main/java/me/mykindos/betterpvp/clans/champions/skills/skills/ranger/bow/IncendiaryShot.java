package me.mykindos.betterpvp.clans.champions.skills.skills.ranger.bow;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.mykindos.betterpvp.clans.Clans;
import me.mykindos.betterpvp.clans.champions.ChampionsManager;
import me.mykindos.betterpvp.clans.champions.roles.Role;
import me.mykindos.betterpvp.clans.champions.skills.config.SkillConfigFactory;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillActions;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillType;
import me.mykindos.betterpvp.clans.champions.skills.types.PrepareArrowSkill;
import me.mykindos.betterpvp.core.effects.EffectType;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilServer;
import org.bukkit.ChatColor;
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
public class IncendiaryShot extends PrepareArrowSkill {

    @Inject
    public IncendiaryShot(Clans clans, ChampionsManager championsManager, SkillConfigFactory configFactory) {
        super(clans, championsManager, configFactory);
    }

    @Override
    public String getName() {
        return "Incendiary Shot";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "Left click to activate.",
                "",
                "Shoot an ignited arrow",
                "burning anyone hit for " + ChatColor.GREEN + (level * 1.5) + ChatColor.GRAY + " seconds",
                "",
                "Cooldown: " + ChatColor.GREEN + getCooldown(level)
        };
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
        UtilServer.runTaskLater(clans, () -> target.setFireTicks(level * 30), 2);
    }

    @Override
    public void displayTrail(Location location) {
        Particle.REDSTONE.builder().location(location).color(255, 0, 0).count(3).extra(0).receivers(60, true).spawn();
    }

    @Override
    public Action[] getActions() {
        return SkillActions.LEFT_CLICK;
    }

    @Override
    public double getCooldown(int level) {

        return 12 - ((level - 1));
    }

}
