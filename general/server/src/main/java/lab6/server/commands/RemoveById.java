package lab6.server.commands;


import lab6.models.MusicBand;
import lab6.server.MainServer;
import lab6.server.managers.CollectionManager;
import lab6.util.CommandResult;

/**
 * Команда для удаления элемента из коллекции по его id
 *
 * @author Михаил
 */
public class RemoveById extends Command {
    public RemoveById(CollectionManager collectionManager) {
        super("remove_by_id", "удалить элемент из коллекции по его id", 1, collectionManager);
    }

    @Override
    public CommandResult run(String[] args, MusicBand band) {
        args = Command.RemoveEmptyElements(args);
        CommandResult commandResult = checkArgAmount(args);
        if (!commandResult.isContinueFlag()) {
            return commandResult;
        }
        try {
            long id = Long.parseLong(args[1]);
            String username = MainServer.currentUser.get();
            
            MusicBand existingBand = collectionManager.getCollection().stream()
                    .filter(b -> b.getId() == id)
                    .findFirst()
                    .orElse(null);

            if (existingBand == null) {
                commandResult.addToMessage("\u001B[31m" + this.name + " : Элемент с id = " + id + " не найден" + "\u001B[0m");
                return commandResult;
            }

            if (!existingBand.getOwnerUsername().equals(username)) {
                commandResult.addToMessage("\u001B[31m" + this.name + " : Вы не являетесь владельцем этого элемента и не можете его удалить" + "\u001B[0m");
                return commandResult;
            }

            if (databaseManager.removeBand(id, username)) {
                collectionManager.removeElement(id);
                commandResult.addToMessage("Элемент с id = " + id + " успешно удалён");
            } else {
                commandResult.addToMessage("\u001B[31m" + this.name + " : Не удалось удалить элемент из базы данных." + "\u001B[0m");
            }
        } catch (NumberFormatException e) {
            commandResult.addToMessage("\u001B[31m" + this.name + " remove_by_id : Позиционный параметр id принимает только значения формата long" + "\u001B[0m");
        }
        return commandResult;
    }
}