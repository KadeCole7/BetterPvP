package me.mykindos.betterpvp.progression.commands.loader;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import me.mykindos.betterpvp.core.command.Command;
import me.mykindos.betterpvp.core.command.CommandManager;
import me.mykindos.betterpvp.core.command.SubCommand;
import me.mykindos.betterpvp.core.command.loader.CommandLoader;
import me.mykindos.betterpvp.progression.Progression;
import org.reflections.Reflections;

import java.util.Set;

@Slf4j
public class ProgressionCommandLoader extends CommandLoader {


    @Inject
    public ProgressionCommandLoader(Progression plugin, CommandManager commandManager) {
        super(plugin, commandManager);
    }

    public void loadCommands(String packageName) {
        Reflections reflections = new Reflections(packageName);

        Set<Class<? extends Command>> classes = reflections.getSubTypesOf(Command.class);
        loadAll(classes);

        Set<Class<?>> subCommandClasses = reflections.getTypesAnnotatedWith(SubCommand.class);
        loadSubCommands(subCommandClasses);

        plugin.saveConfig();
        log.info("Loaded {} commands for Progression", count);
    }

}
