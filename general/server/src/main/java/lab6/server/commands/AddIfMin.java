package lab6.server.commands;


import java.util.PriorityQueue;

import lab6.models.MusicBand;
import lab6.server.MainServer;
import lab6.server.managers.CollectionManager;
import lab6.util.CommandResult;

/**
 * Команда для добавления элемента, если его значение меньше наименьшего элемента коллекции
 *
 * @author Михаил
 */
public class AddIfMin extends Command {
    public AddIfMin(CollectionManager collectionManager) {
        super("add_if_min", "добавить новый элемент в коллекцию, если его значение меньше значения наименьшего элемента этой коллекции", 0, collectionManager);
    }

    @Override
    public CommandResult run(String[] args, MusicBand bandToAdd) {
        CommandResult commandResult = checkArgAmount(args);
        if (!commandResult.isContinueFlag()) {
            return commandResult;
        }

        PriorityQueue<MusicBand> bands = collectionManager.getCollection();
        if (bands.stream().anyMatch(band -> band.compareTo(bandToAdd) < 1)) {
            commandResult.setMessage("Банда не добавлена (не минимальная)");
            return commandResult;
        }
        
        String username = MainServer.currentUser.get();
        if (databaseManager.addBand(bandToAdd, username)) {
            collectionManager.addLoadedElement(bandToAdd);
            commandResult.setMessage("Группа добавлена успешно!");
        } else {
            commandResult.setContinueFlag(false);
            commandResult.setMessage("Ошибка: не удалось сохранить группу в базу данных.");
        }
        return commandResult;
    }
}