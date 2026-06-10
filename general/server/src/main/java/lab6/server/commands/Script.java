package lab6.server.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

import lab6.models.MusicBand;
import lab6.server.managers.CollectionManager;
import lab6.util.CommandResult;

/**
 * Команда для считывания и исполнения скрипта из указанного файла с контролем
 * рекурсии
 *
 * @author Михаил
 */
public class Script extends Command {
    public Script(CollectionManager collectionManager) {
        super("script", "считать и исполнить скрипт из указанного файла", 1, collectionManager);
    }

    @Override
    public CommandResult run(String[] args, MusicBand band) {
        args = Command.RemoveEmptyElements(args);
        CommandResult commandResult = checkArgAmount(args);
        if (!commandResult.isContinueFlag()) {
            return commandResult;
        }

        try {
            File file = new File("data/scripts/" + args[1]);
            Scanner fileReader = new Scanner(file);
            CommandResult result = new CommandResult(true, "");
            while ((result.isContinueFlag()) && fileReader.hasNextLine()) {
                String input = fileReader.nextLine().trim();
                String[] tokens = input.split(" ");
                Map<String, Command> commandsList = commandManager.getCommandsList();

                if (commandManager.checkRecursionExhaustion(file)) {
                    if (!commandManager.isRecursionForcedExit()) {
                        commandResult.addToMessage(
                                "\u001B[31m" + this.name + " : Превышен лимит глубины рекурсии" + "\u001B[0m");
                    }
                    commandManager.setRecursionForcedExit(true);
                    fileReader.close();
                    return commandResult;
                }

                if (commandsList.containsKey(tokens[0])) {
                    commandManager.addScriptFile(file);
                    commandResult = commandsList.get(tokens[0].toLowerCase()).run(tokens, null);
                } else {
                    commandResult.addToMessage("\u001B[31m" + input + " не распознано как имя команды." + "\u001B[0m");
                }
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            commandResult.addToMessage("\u001B[31m" + this.name + " : Файл не найден. Попытались прочитать файл "
                    + args[1] + ", но не вышло :(" + "\u001B[0m");
            return commandResult;
        }
        return commandResult;
    }
}