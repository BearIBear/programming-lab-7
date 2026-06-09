package lab6.util;

import java.io.Serializable;

public class CommandResult implements Serializable {
    private boolean continueFlag;
    private String message;
    private static final long serialVersionUID = 1L;

    public CommandResult(boolean continueFlag, String message) {
        this.continueFlag = continueFlag;
        this.message = message;
    }

    public boolean isContinueFlag() {
        return continueFlag;
    }

    public void setContinueFlag(boolean continueFlag) {
        this.continueFlag = continueFlag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void addToMessage(String messageToAdd) {
        this.setMessage(message.isBlank() ? messageToAdd : message + System.lineSeparator() + messageToAdd);
    }
}
