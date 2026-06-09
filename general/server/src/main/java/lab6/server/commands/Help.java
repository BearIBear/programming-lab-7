package lab6.server.commands;


import java.util.Map;

import lab6.models.MusicBand;
import lab6.server.managers.CollectionManager;
import lab6.util.CommandResult;

/**
 * Команда для вывода справки по доступным командам
 *
 * @author Михаил
 */
public class Help extends Command {
    public Help(CollectionManager collectionManager) {
        super("help", "вывести справку по доступным командам", 0, collectionManager);
    }

    @Override
    public CommandResult run(String[] args, MusicBand band) {
        CommandResult commandResult = checkArgAmount(args);
        if (!commandResult.isContinueFlag()) {
            return commandResult;
        }

        int padding = 5;
        Map<String, Command>  commandsList = commandManager.getCommandsList();
        int max_length = commandsList.keySet().stream().mapToInt(String::length).max().getAsInt();
        commandsList.entrySet().stream().filter(commandName -> !commandName.getKey()
            .equals("save"))
            .forEach(command -> {
                String name = command.getKey();
                String desc = command.getValue().getDesc();
                commandResult.addToMessage(name + " ".repeat(max_length + padding - name.length()) + desc);
            });

        commandResult.setContinueFlag(true);
        return commandResult;
    }
}
