package lab6.server.commands;


import lab6.models.MusicBand;
import lab6.server.managers.CollectionManager;
import lab6.util.CommandResult;

/**
 * Команда для вывода первого элемента коллекции (head)
 *
 * @author Михаил
 */
public class Head extends Command {
    public Head(CollectionManager collectionManager) {
        super("head", "вывести первый элемент коллекции", 0, collectionManager);
    }

    @Override
    public CommandResult run(String[] args, MusicBand band) {
        CommandResult commandResult = checkArgAmount(args);
        if (!commandResult.isContinueFlag()) {
            return commandResult;
        }
        
        if (collectionManager.getCollection().size() == 0) {
            commandResult.addToMessage("\u001B[31m" + this.name + " : Коллекция пустая" + "\u001B[0m");
            return commandResult;
        }

        commandResult.addToMessage(collectionManager.getCollection().peek().toString());
        return commandResult;
    }
}