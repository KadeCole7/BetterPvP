package me.mykindos.betterpvp.champions.champions.skills.skills.brute.sword;

import me.mykindos.betterpvp.champions.Champions;
import me.mykindos.betterpvp.champions.champions.ChampionsManager;
import me.mykindos.betterpvp.champions.champions.skills.data.SkillActions;
import me.mykindos.betterpvp.champions.champions.skills.types.ChannelSkill;
import me.mykindos.betterpvp.champions.champions.skills.types.CooldownSkill;
import me.mykindos.betterpvp.champions.champions.skills.types.EnergySkill;
import me.mykindos.betterpvp.champions.champions.skills.types.InteractSkill;
import me.mykindos.betterpvp.core.client.gamer.Gamer;
import me.mykindos.betterpvp.core.components.champions.Role;
import me.mykindos.betterpvp.core.components.champions.SkillType;
import me.mykindos.betterpvp.core.framework.updater.UpdateEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilBlock;
import me.mykindos.betterpvp.core.utilities.UtilEntity;
import me.mykindos.betterpvp.core.utilities.UtilMath;
import me.mykindos.betterpvp.core.utilities.UtilVelocity;
import me.mykindos.betterpvp.core.utilities.math.VelocityData;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.UUID;

@Singleton
@BPvPListener
public class BattleTaunt extends ChannelSkill implements InteractSkill, CooldownSkill, EnergySkill, Listener {

    private int radius;

    @Inject
    public BattleTaunt(Champions champions, ChampionsManager championsManager) {
        super(champions, championsManager);
    }

    @Override
    public String getName() {
        return "Battle Taunt";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "Hold right click with a Sword to channel",
                "",
                "While channelling, any enemies within <val>" + (radius + level) + "</val> blocks",
                "get slowly pulled towards you",
                "",
                "Energy / Second: <val>" + getEnergy(level)};
    }

    @Override
    public Role getClassType() {
        return Role.BRUTE;
    }

    @Override
    public SkillType getType() {
        return SkillType.SWORD;
    }


    @UpdateEvent
    public void energy() {
        Iterator<UUID> activeIterator = active.iterator();
        while (activeIterator.hasNext()) {
            UUID uuid = activeIterator.next();
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                Gamer gamer = championsManager.getClientManager().search().online(player).getGamer();
                if (gamer.isHoldingRightClick()) {
                    int level = getLevel(player);
                    if(level <= 0){
                        activeIterator.remove();
                    }else if (!championsManager.getEnergy().use(player, getName(), getEnergy(level) / 2, true)) {
                        activeIterator.remove();
                    } else if (!player.getInventory().getItemInMainHand().getType().name().contains("SWORD")) {
                        activeIterator.remove();
                    } else if (UtilBlock.isInLiquid(player)) {
                        activeIterator.remove();
                    } else {

                        player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, Material.DIAMOND_BLOCK);

                        for (int i = 0; i <= (radius + level); i++) {
                            pull(player, player.getEyeLocation().add(player.getLocation().getDirection().multiply(i)));
                        }
                    }
                }
            } else {
                activeIterator.remove();
            }
        }


    }

    private void pull(Player player, Location location) {
        for (LivingEntity target : UtilEntity.getNearbyEnemies(player, location, radius)) {
            VelocityData velocityData = new VelocityData(UtilVelocity.getTrajectory(target, player), 0.3D, false, 0.0D, 0.0D, 1.0D, true);
            if (target instanceof Player) {

                if (UtilMath.offset(player.getLocation(), target.getLocation()) >= radius) {
                    UtilVelocity.velocity(target, player, velocityData);
                }

            } else {
                UtilVelocity.velocity(target, player, velocityData);
            }
        }
    }


    @Override
    public float getEnergy(int level) {

        return (float) (energy - ((level - 1) * energyDecreasePerLevel));
    }

    @Override
    public void activate(Player player, int level) {
        active.add(player.getUniqueId());
    }

    @Override
    public Action[] getActions() {
        return SkillActions.RIGHT_CLICK;
    }

    @Override
    public double getCooldown(int level) {
        return cooldown - level * cooldownDecreasePerLevel;
    }

    @Override
    public boolean showCooldownFinished(){
        return false;
    }

    @Override
    public void loadSkillConfig() {
        radius = getConfig("radius", 2, Integer.class);
    }
}
