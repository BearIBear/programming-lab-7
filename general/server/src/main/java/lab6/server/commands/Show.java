package lab6.server.commands;


import lab6.models.MusicBand;
import lab6.server.managers.CollectionManager;
import lab6.util.CommandResult;

/**
 * Команда для вывода всех элементов коллекции в строковом представлении
 *
 * @author Михаил
 */
public class Show extends Command {
    public Show(CollectionManager collectionManager) {
        super("show", "вывести в стандартный поток вывода все элементы коллекции в строковом представлении", 0, collectionManager);
    }

    @Override
    public CommandResult run(String[] args, MusicBand band) {
        CommandResult commandResult = checkArgAmount(args);
        if (!commandResult.isContinueFlag()) {
            return commandResult;
        }

        collectionManager.getCollection().stream().forEach(musicBand -> {
            commandResult.addToMessage("ID группы: " + musicBand.getId());
            commandResult.addToMessage("Название группы: " + musicBand.getName());
            commandResult.addToMessage("Описание группы: " + musicBand.getDescription());
            commandResult.addToMessage("Фронтман: " + musicBand.getFrontMan());
            commandResult.addToMessage("Жанр: " + musicBand.getGenre());
            commandResult.addToMessage("Дата создания: " + musicBand.getCreationDate());
            commandResult.addToMessage("Количество участников: " + musicBand.getNumberOfParticipants());
            commandResult.addToMessage("Количество синглов: " + musicBand.getSinglesCount());
            commandResult.addToMessage("Координаты: " + musicBand.getCoordinates());
        });

        return commandResult;
    }
}