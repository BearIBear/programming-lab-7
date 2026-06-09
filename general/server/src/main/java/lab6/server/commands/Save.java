package lab6.server.commands;


import lab6.models.MusicBand;
import lab6.server.managers.CollectionManager;
import lab6.server.managers.FileManager;
import lab6.util.CommandResult;

/**
 * Команда для сохранения коллекции в файл
 *
 * @author Михаил
 */
public class Save extends Command {
    private FileManager fileManager;

    public Save(CollectionManager collectionManager, FileManager fileManager) {
        super("save", "сохранить коллекцию в файл", 0, collectionManager);
        this.fileManager = fileManager;
    }

    @Override
    public CommandResult run(String[] args, MusicBand band) {
        CommandResult commandResult = checkArgAmount(args);
        if (!commandResult.isContinueFlag()) {
            return commandResult;
        }
        
        fileManager.save(collectionManager);
        return commandResult;
    }
}