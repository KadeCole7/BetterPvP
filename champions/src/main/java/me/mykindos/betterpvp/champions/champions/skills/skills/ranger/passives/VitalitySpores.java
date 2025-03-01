package me.mykindos.betterpvp.champions.champions.skills.skills.ranger.passives;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.mykindos.betterpvp.champions.Champions;
import me.mykindos.betterpvp.champions.champions.ChampionsManager;
import me.mykindos.betterpvp.champions.champions.skills.Skill;
import me.mykindos.betterpvp.champions.champions.skills.types.PassiveSkill;
import me.mykindos.betterpvp.core.client.gamer.Gamer;
import me.mykindos.betterpvp.core.combat.events.CustomDamageEvent;
import me.mykindos.betterpvp.core.components.champions.Role;
import me.mykindos.betterpvp.core.components.champions.SkillType;
import me.mykindos.betterpvp.core.framework.updater.UpdateEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilFormat;
import me.mykindos.betterpvp.core.utilities.UtilTime;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Singleton
@BPvPListener
public class VitalitySpores extends Skill implements PassiveSkill {

    private double baseDuration;

    private double durationDecreasePerLevel;

    private int regenerationStrength;

    @Inject
    public VitalitySpores(Champions champions, ChampionsManager championsManager) {
        super(champions, championsManager);
    }

    @Override
    public String getName() {
        return "Vitality Spores";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "After <val>" + getDuration(level) + "</val> seconds of not taking damage,",
                "forest spores surround you, giving",
                "you <effect>Regeneration " + UtilFormat.getRomanNumeral(regenerationStrength + 1) + "</effect>",
                "",
                "You will keep the buff until you take damage"};
    }

    public double getDuration(int level) {
        return baseDuration - level * durationDecreasePerLevel;
    }

    @Override
    public Role getClassType() {
        return Role.RANGER;
    }

    @UpdateEvent(delay = 500)
    public void onUpdate() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            int level = getLevel(player);
            if (level > 0) {
                Gamer gamer = championsManager.getClientManager().search().online(player).getGamer();
                if (UtilTime.elapsed(gamer.getLastDamaged(), (long) (getDuration(level) * 1000L))) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, regenerationStrength));
                }
            }
        }

    }

    @EventHandler
    public void onDamageReceived(CustomDamageEvent event) {
        if (!(event.getDamagee() instanceof Player player)) return;

        if (hasSkill(player)) {
            if (player.hasPotionEffect(PotionEffectType.REGENERATION)) {
                player.removePotionEffect(PotionEffectType.REGENERATION);
            }
        }
    }

    @Override
    public SkillType getType() {

        return SkillType.PASSIVE_B;
    }

    @Override
    public void loadSkillConfig(){
        baseDuration = getConfig("baseDuration", 7.0, Double.class);
        durationDecreasePerLevel = getConfig("durationDecreasePerLevel", 1.0, Double.class);

        regenerationStrength = getConfig("regenerationStrength", 0, Integer.class);
    }

}
