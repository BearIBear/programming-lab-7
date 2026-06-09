package lab6.server.commands;


import lab6.models.MusicBand;
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

        collectionManager.addElement(band);
        commandResult.setMessage("Банда добавлена успешно");
        return commandResult;
    }
}