package me.mykindos.betterpvp.clans.champions.builds.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.mykindos.betterpvp.clans.champions.builds.RoleBuild;
import me.mykindos.betterpvp.clans.champions.roles.Role;
import me.mykindos.betterpvp.clans.champions.skills.Skill;
import me.mykindos.betterpvp.clans.champions.skills.SkillManager;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillType;
import me.mykindos.betterpvp.clans.gamer.Gamer;
import me.mykindos.betterpvp.core.config.Config;
import me.mykindos.betterpvp.core.database.Database;
import me.mykindos.betterpvp.core.database.query.Statement;
import me.mykindos.betterpvp.core.database.query.values.BooleanStatementValue;
import me.mykindos.betterpvp.core.database.query.values.IntegerStatementValue;
import me.mykindos.betterpvp.core.database.query.values.StringStatementValue;
import me.mykindos.betterpvp.core.database.repository.IRepository;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class BuildRepository implements IRepository<RoleBuild> {

    @Inject
    @Config(path = "clans.database.prefix")
    private String tablePrefix;

    private final Database database;
    private final SkillManager skillManager;

    @Inject
    public BuildRepository(Database database, SkillManager skillManager) {
        this.database = database;
        this.skillManager = skillManager;
    }

    @Override
    public List<RoleBuild> getAll() {

        return null;
    }

    public void loadBuilds(Gamer gamer) {
        String query = "SELECT * FROM " + tablePrefix + "champions_builds WHERE Gamer = ?";
        CachedRowSet result = database.executeQuery(new Statement(query, new StringStatementValue(gamer.getUuid())));
        try {
            while (result.next()) {
                String uuid = result.getString(1);
                String role = result.getString(2);
                int id = result.getInt(3);
                RoleBuild build = new RoleBuild(uuid, Role.valueOf(role.toUpperCase()), id);

                String sword = result.getString(4);
                setSkill(build, SkillType.SWORD, sword);

                String axe = result.getString(5);
                setSkill(build, SkillType.AXE, axe);

                String bow = result.getString(6);
                setSkill(build, SkillType.BOW, bow);

                String passiveA = result.getString(7);
                setSkill(build, SkillType.PASSIVE_A, passiveA);

                String passiveB = result.getString(8);
                setSkill(build, SkillType.PASSIVE_B, passiveB);

                String global = result.getString(9);
                setSkill(build, SkillType.GLOBAL, global);

                boolean active = result.getBoolean(10);
                build.setActive(active);

                if (active) {
                    gamer.getActiveBuilds().put(role, build);
                }

                gamer.getBuilds().add(build);

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    private void setSkill(RoleBuild build, SkillType type, String value) {

        if (value != null && !value.equals("")) {
            String[] split = value.split(",");
            Skill skill = skillManager.getObjects().get(split[0]);

            int level = Integer.parseInt(split[1]);
            build.setSkill(type, skill, level);
            build.takePoints(level);


        }
    }

    @Override
    public void save(RoleBuild build) {
        String query = "INSERT IGNORE INTO " + tablePrefix + "champions_builds VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        var swordStatement = new SkillStatementValue(build.getSwordSkill());
        var axeStatement = new SkillStatementValue(build.getAxeSkill());
        var bowStatement = new SkillStatementValue(build.getBow());
        var passiveAStatement = new SkillStatementValue(build.getPassiveA());
        var passiveBStatement = new SkillStatementValue(build.getPassiveB());
        var globalStatement = new SkillStatementValue(build.getGlobal());

        database.executeUpdateAsync(new Statement(query, new StringStatementValue(build.getUuid()), new StringStatementValue(build.getRole().getName()),
                new IntegerStatementValue(build.getId()),
                swordStatement, axeStatement, bowStatement,
                passiveAStatement, passiveBStatement, globalStatement,
                new BooleanStatementValue(build.isActive())));
    }

    public void update(RoleBuild build) {
        String query = "INSERT INTO " + tablePrefix + "champions_builds VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE Sword = ?, Axe = ?, Bow = ?, PassiveA = ?, PassiveB = ?, Global = ?, Active = ?";

        var swordStatement = new SkillStatementValue(build.getSwordSkill());
        var axeStatement = new SkillStatementValue(build.getAxeSkill());
        var bowStatement = new SkillStatementValue(build.getBow());
        var passiveAStatement = new SkillStatementValue(build.getPassiveA());
        var passiveBStatement = new SkillStatementValue(build.getPassiveB());
        var globalStatement = new SkillStatementValue(build.getGlobal());

        database.executeUpdateAsync(new Statement(query, new StringStatementValue(build.getUuid()), new StringStatementValue(build.getRole().getName()),
                new IntegerStatementValue(build.getId()),
                swordStatement, axeStatement, bowStatement,
                passiveAStatement, passiveBStatement, globalStatement,
                new BooleanStatementValue(build.isActive()),
                swordStatement, axeStatement, bowStatement,
                passiveAStatement, passiveBStatement, globalStatement,
                new BooleanStatementValue(build.isActive())));
    }

    public void loadDefaultBuilds(Gamer gamer) {

        if (!gamer.getBuilds().isEmpty()) return;

        List<RoleBuild> builds = new ArrayList<>();
        for (int d = 1; d < 5; d++) {

            RoleBuild assassin = new RoleBuild(gamer.getUuid(), Role.valueOf("ASSASSIN"), d);
            if (d == 1) {
                assassin.setActive(true);

            }
            assassin.setSkill(SkillType.SWORD, skillManager.getObjects().get("Sever"), 3);
            assassin.setSkill(SkillType.AXE, skillManager.getObjects().get("Leap"), 5);
            assassin.setSkill(SkillType.PASSIVE_A, skillManager.getObjects().get("Backstab"), 1);
            assassin.setSkill(SkillType.PASSIVE_B, skillManager.getObjects().get("Smoke Bomb"), 3);
            assassin.takePoints(12);
            deductPoints(assassin);

            RoleBuild gladiator = new RoleBuild(gamer.getUuid(), Role.valueOf("GLADIATOR"), d);
            if (d == 1) {
                gladiator.setActive(true);
            }
            gladiator.setSkill(SkillType.SWORD, skillManager.getObjects().get("Takedown"), 5);
            gladiator.setSkill(SkillType.AXE, skillManager.getObjects().get("Seismic Slam"), 3);
            gladiator.setSkill(SkillType.PASSIVE_A, skillManager.getObjects().get("Colossus"), 1);
            gladiator.setSkill(SkillType.PASSIVE_B, skillManager.getObjects().get("Stampede"), 3);
            deductPoints(gladiator);

            RoleBuild ranger = new RoleBuild(gamer.getUuid(), Role.valueOf("RANGER"), d);
            if (d == 1) {
                ranger.setActive(true);
            }
            ranger.setSkill(SkillType.SWORD, skillManager.getObjects().get("Disengage"), 3);
            ranger.setSkill(SkillType.BOW, skillManager.getObjects().get("Incendiary Shot"), 5);
            ranger.setSkill(SkillType.PASSIVE_A, skillManager.getObjects().get("Longshot"), 3);
            ranger.setSkill(SkillType.PASSIVE_B, skillManager.getObjects().get("Sharpshooter"), 1);
            ranger.takePoints(12);
            deductPoints(ranger);

            RoleBuild paladin = new RoleBuild(gamer.getUuid(), Role.valueOf("PALADIN"), d);
            if (d == 1) {
                paladin.setActive(true);
            }
            paladin.setSkill(SkillType.SWORD, skillManager.getObjects().get("Inferno"), 5);
            paladin.setSkill(SkillType.AXE, skillManager.getObjects().get("Molten Blast"), 3);
            paladin.setSkill(SkillType.PASSIVE_A, skillManager.getObjects().get("Holy Light"), 2);
            paladin.setSkill(SkillType.PASSIVE_B, skillManager.getObjects().get("Immolate"), 2);
            paladin.takePoints(12);
            deductPoints(paladin);

            RoleBuild knight = new RoleBuild(gamer.getUuid(), Role.valueOf("KNIGHT"), d);
            if (d == 1) {
                knight.setActive(true);
            }
            knight.setSkill(SkillType.SWORD, skillManager.getObjects().get("Riposte"), 3);
            knight.setSkill(SkillType.AXE, skillManager.getObjects().get("Bulls Charge"), 5);
            knight.setSkill(SkillType.PASSIVE_A, skillManager.getObjects().get("Fury"), 3);
            knight.setSkill(SkillType.PASSIVE_B, skillManager.getObjects().get("Swordsmanship"), 1);
            knight.takePoints(12);
            deductPoints(knight);

            RoleBuild warlock = new RoleBuild(gamer.getUuid(), Role.valueOf("WARLOCK"), d);
            if (d == 1) {
                warlock.setActive(true);
            }

            warlock.setSkill(SkillType.SWORD, skillManager.getObjects().get("Leech"), 4);
            warlock.setSkill(SkillType.AXE, skillManager.getObjects().get("Bloodshed"), 5);
            warlock.setSkill(SkillType.PASSIVE_A, skillManager.getObjects().get("Frailty"), 1);
            warlock.setSkill(SkillType.PASSIVE_B, skillManager.getObjects().get("Soul Harvest"), 2);
            deductPoints(warlock);

            builds.addAll(List.of(knight, ranger, gladiator, paladin, assassin, warlock));

        }

        builds.forEach(build -> {
            save(build);
            gamer.getBuilds().add(build);
            if (build.isActive()) {
                gamer.getActiveBuilds().put(build.getRole().getName(), build);
            }
        });
    }

    private void deductPoints(RoleBuild roleBuild) {
        int points = 12;
        for (Skill skill : roleBuild.getActiveSkills()) {
            if (skill == null) continue;
            points -= roleBuild.getBuildSkill(skill.getType()).getLevel();
        }
        roleBuild.setPoints(points);
    }
}
