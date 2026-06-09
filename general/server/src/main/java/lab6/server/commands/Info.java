package lab6.server.commands;


import lab6.models.MusicBand;
import lab6.server.managers.CollectionManager;
import lab6.util.CommandResult;

/**
 * Команда для вывода информации о коллекции (тип, количество элементов, дата инициализации)
 *
 * @author Михаил
 */
public class Info extends Command {
    public Info(CollectionManager collectionManager) {
        super("info", "вывести информацию о коллекции", 0, collectionManager);
    }

    @Override
    public CommandResult run(String[] args, MusicBand band) {
        CommandResult commandResult = checkArgAmount(args);
        if (!commandResult.isContinueFlag()) {
            return commandResult;
        }
        commandResult.addToMessage("Тип коллекции: PriorityQueue");
        commandResult.addToMessage("Количество элементов: " + collectionManager.getCollection().size());
        commandResult.addToMessage("Дата инициализации: " + collectionManager.getInitTime());
        return commandResult;
    }
}