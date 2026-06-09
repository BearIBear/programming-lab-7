package lab6.server.commands;

import lab6.models.MusicBand;
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

        int id = Integer.parseInt(args[1]); 
        if (collectionManager.removeElement(id)) {
            band.setId(id);
            collectionManager.addElement(band);
            return commandResult;
        }

        commandResult.addToMessage("\u001B[31m" + this.name + " : Элемент с id = " + id + " не найден" + "\u001B[0m");
        return commandResult;
    }
}