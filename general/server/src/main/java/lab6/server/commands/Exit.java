package lab6.server.commands;


import lab6.models.MusicBand;
import lab6.server.managers.CollectionManager;
import lab6.util.CommandResult;

/**
 * Команда для завершения работы программы (без сохранения в файл)
 *
 * @author Михаил
 */
public class Exit extends Command {
    public Exit(CollectionManager collectionManager) {
        super("exit", "завершить программу (без сохранения в файл)", 0, collectionManager);
    }

    @Override
    public CommandResult run(String[] args, MusicBand band) {
        CommandResult commandResult = checkArgAmount(args);
        if (!commandResult.isContinueFlag()) {
            return commandResult;
        }
        return commandResult;
    }
}