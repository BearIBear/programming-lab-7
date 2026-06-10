package lab6.util;

import java.io.Serializable;

import lab6.models.MusicBand;

public class CommandPayload implements Serializable {
    public String commandName;
    public String[] args;
    public MusicBand band;
    public String username;
    public String password;
    public boolean isRegister;
    private static final long serialVersionUID = 1L;

    public CommandPayload(String commandName, String[] args, MusicBand band) {
        this.commandName = commandName;
        this.args = args;
        this.band = band;
    }

    public CommandPayload(String commandName, String[] args, MusicBand band, String username, String password, boolean isRegister) {
        this.commandName = commandName;
        this.args = args;
        this.band = band;
        this.username = username;
        this.password = password;
        this.isRegister = isRegister;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }

    public MusicBand getBand() {
        return band;
    }

    public void setBand(MusicBand band) {
        this.band = band;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRegister() {
        return isRegister;
    }

    public void setRegister(boolean register) {
        isRegister = register;
    }
}
