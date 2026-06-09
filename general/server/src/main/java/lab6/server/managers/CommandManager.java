package lab6.server.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lab6.server.commands.Command;

/**
 * Менеджер для регистрации и исполнения команд
 *
 * @author Михаил
 */
public class CommandManager {
    private final Map<String, Command> commandsList = new HashMap<>();
    private ArrayList<File> scriptFiles = new ArrayList<>();
    private int recursionLimit = 5;
    private boolean recursionForcedExit;
    
    public CommandManager() {}

    public void register(Command command) {
        command.setCommandManager(this);
        commandsList.put(command.getName(), command);
    }

    public Map<String, Command> getCommandsList() {
        return commandsList;
    }

    public ArrayList<File> getScriptFiles() {
        return scriptFiles;
    }

    public void addScriptFile(File file) {
        scriptFiles.add(file);
    }

    public void clearScriptFiles() {
        scriptFiles.clear();
    }

    public boolean checkRecursionExhaustion(File file) {
        int count = 0;
        for (File scriptFile : scriptFiles) {
            if (file.equals(scriptFile)) {
                count += 1;
            }
        }
        if (this.recursionLimit < count) {
            return true;
        }
        return false;
    }

    public int getRecursionLimit() {
        return recursionLimit;
    }

    public boolean isRecursionForcedExit() {
        return recursionForcedExit;
    }

    public void setRecursionForcedExit(boolean recursionForcedExit) {
        this.recursionForcedExit = recursionForcedExit;
    }
}
