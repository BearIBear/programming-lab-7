package lab6.server.commands;

import java.util.Arrays;
import java.util.function.Predicate;

import lab6.models.MusicBand;
import lab6.server.managers.CollectionManager;
import lab6.server.managers.CommandManager;
import lab6.server.managers.DatabaseManager;
import lab6.util.CommandResult;

/**
 * Абстрактный базовый класс для всех команд
 *
 * @author Михаил
 */
public abstract class Command {
    protected final String name;
    private final String desc;
    private final int argsAmount;
    protected CommandManager commandManager;
    protected CollectionManager collectionManager;
    protected DatabaseManager databaseManager;

    public Command(String name, String desc, int argsAmount, CollectionManager collectionManager) {
        this.name = name;
        this.desc = desc;
        this.argsAmount = argsAmount + 1;
        this.collectionManager = collectionManager;
    }

    public abstract CommandResult run(String[] args, MusicBand band);
    
    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
    
    public CommandManager getCommandManager() {
        return commandManager;
    }

    public CollectionManager getCollectionManager() {
        return collectionManager;
    }

    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public CommandResult checkArgAmount(String[] args) {
        if (args.length < this.argsAmount) {
            return new CommandResult(false, "\u001B[31m" + this.name + " : Недостаточно параметров" + "\u001B[0m");
        }
        if (args.length > this.argsAmount) {
            return new CommandResult(false, "\u001B[31m" + this.name + " : Слишком много параметров" + "\u001B[0m");
        }
        return new CommandResult(true, "");
    }

    public static String[] RemoveEmptyElements(String[] input) {
        return Arrays.stream(input).filter(Predicate.not(String::isBlank)).toArray(String[]::new);
    }
}