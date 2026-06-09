package lab6.server.commands;

import java.util.Arrays;


import lab6.models.MusicBand;
import lab6.server.managers.CollectionManager;
import lab6.util.CommandResult;

/**
 * Команда для вывода количества элементов, значение поля description которых меньше заданного
 *
 * @author Михаил
 */
public class CountLessThanDescription extends Command {
    public CountLessThanDescription(CollectionManager collectionManager) {
        super("count_less_than_description", "вывести количество элементов, значение поля description которых меньше заданного", 1, collectionManager);
    }

    @Override
    public CommandResult run(String[] args, MusicBand band) {
        String description = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String[] newArgs = {args[0], description};

        CommandResult commandResult = checkArgAmount(newArgs);
        if (!commandResult.isContinueFlag()) {
            return commandResult;
        }

        commandResult.addToMessage(Long.toString(collectionManager.getCollection().stream().filter(bandToCheck -> bandToCheck.getDescription().compareTo(description) < 0).count()));
        return commandResult;
    }
}