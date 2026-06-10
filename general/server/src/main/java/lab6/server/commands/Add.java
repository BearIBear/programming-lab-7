package lab6.server.commands;


import lab6.models.MusicBand;
import lab6.server.MainServer;
import lab6.server.managers.CollectionManager;
import lab6.util.CommandResult;

/**
 * Команда для добавления нового элемента в коллекцию
 *
 * @author Михаил
 */
public class Add extends Command {
    public Add(CollectionManager collectionManager) {
        super("add", "добавить новый элемент в коллекцию", 0, collectionManager);
    }

    @Override
    public CommandResult run(String[] args, MusicBand band) {
        CommandResult commandResult = checkArgAmount(args);
        if (!commandResult.isContinueFlag()) {
            return commandResult;
        }

        String username = MainServer.currentUser.get();
        if (databaseManager.addBand(band, username)) {
            collectionManager.addLoadedElement(band); // Добавляем с ID из БД
            commandResult.setMessage("Группа добавлена успешно!");
        } else {
            commandResult.setContinueFlag(false);
            commandResult.setMessage("Ошибка: не удалось сохранить группу в базу данных.");
        }
        return commandResult;
    }
}