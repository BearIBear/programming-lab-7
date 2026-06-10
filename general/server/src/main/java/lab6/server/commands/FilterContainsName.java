package lab6.server.commands;

import java.util.Arrays;

import lab6.models.MusicBand;
import lab6.server.managers.CollectionManager;
import lab6.util.CommandResult;

/**
 * Команда для вывода элементов, значение поля name которых содержит заданную
 * подстроку
 *
 * @author Михаил
 */
public class FilterContainsName extends Command {
    public FilterContainsName(CollectionManager collectionManager) {
        super("filter_contains_name", "вывести элементы, значение поля name которых содержит заданную подстроку", 1,
                collectionManager);
    }

    @Override
    public CommandResult run(String[] args, MusicBand band) {
        String description = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String[] newArgs = { args[0], description };

        CommandResult commandResult = checkArgAmount(newArgs);
        if (!commandResult.isContinueFlag()) {
            return commandResult;
        }

        collectionManager.getCollection().stream().filter(band1 -> band1.getName().contains(description))
                .forEach(band1 -> commandResult.addToMessage(band1.toString()));
        return commandResult;
    }
}