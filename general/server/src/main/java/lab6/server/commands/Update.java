package lab6.server.commands;

import lab6.models.MusicBand;
import lab6.server.MainServer;
import lab6.server.managers.CollectionManager;
import lab6.util.CommandResult;

/**
 * Команда для обновления значения элемента коллекции, id которого равен заданному
 *
 * @author Михаил
 */
public class Update extends Command {
    public Update(CollectionManager collectionManager) {
        super("update", "обновить значение элемента коллекции, id которого равен заданному", 1, collectionManager);
    }

    @Override
    public CommandResult run(String[] args, MusicBand band) {
        args = Command.RemoveEmptyElements(args);
        CommandResult commandResult = checkArgAmount(args);
        if (!commandResult.isContinueFlag()) {
            return commandResult;
        }

        long id;
        try {
            id = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            commandResult.setContinueFlag(false);
            commandResult.setMessage("Ошибка: ID должен быть числом.");
            return commandResult;
        }

        String username = MainServer.currentUser.get();
        MusicBand existingBand = collectionManager.getCollection().stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElse(null);

        if (existingBand == null) {
            commandResult.setMessage("\u001B[31m" + this.name + " : Элемент с id = " + id + " не найден" + "\u001B[0m");
            return commandResult;
        }

        if (!existingBand.getOwnerUsername().equals(username)) {
            commandResult.setMessage("\u001B[31m" + this.name + " : Вы не являетесь владельцем этого элемента и не можете его обновить" + "\u001B[0m");
            return commandResult;
        }

        if (databaseManager.updateBand(id, band, username)) {
            collectionManager.removeElement(id);
            band.setId(id);
            band.setOwnerUsername(username);
            collectionManager.addLoadedElement(band);
            commandResult.setMessage("Элемент с id = " + id + " успешно обновлен.");
        } else {
            commandResult.setMessage("Ошибка: не удалось обновить элемент в базе данных.");
        }

        return commandResult;
    }
}