package lab6.server.commands;


import lab6.models.MusicBand;
import lab6.server.managers.CollectionManager;
import lab6.util.CommandResult;

/**
 * Команда для очистки коллекции
 *
 * @author Михаил
 */
public class Clear extends Command {
    public Clear(CollectionManager collectionManager) {
        super("clear", "очистить коллекцию", 0, collectionManager);
    }

    @Override
    public CommandResult run(String[] args, MusicBand band) {
        CommandResult commandResult = checkArgAmount(args);
        if (!commandResult.isContinueFlag()) {

            return commandResult;
        }
        collectionManager.clearCollection();
        CollectionManager.setNextId(1);
        return commandResult;
    }
}