package lab6.util;

import java.io.Serializable;

import lab6.models.MusicBand;

public class CommandPayload implements Serializable {
    public String commandName;
    public String[] args;
    public MusicBand band;
    private static final long serialVersionUID = 1L;

    public CommandPayload(String commandName, String[] args, MusicBand band) {
        this.commandName = commandName;
        this.args = args;
        this.band = band;
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
    
    
}
