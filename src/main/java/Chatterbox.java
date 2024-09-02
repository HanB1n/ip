import chatterboxerrors.ChatterBoxDataFileError;
import chatterboxerrors.ChatterBoxError;
import chatterboxerrors.ChatterBoxNullTaskError;
import tasks.Deadline;
import tasks.Event;
import tasks.ToDo;
import utils.Commands;
import utils.Parser;
import utils.Storage;
import utils.StoredList;
import utils.Ui;


/**
 * Represents a Chatbot.
 */
public class Chatterbox {
    private final StoredList taskList;
    private final Storage storage;
    private final Ui ui;

    /**
     * Initialised an instance of Chatterbox with the given save file.
     * @param saveFilePath The filepath of a save file.
     */
    public Chatterbox(String saveFilePath) {
        storage = new Storage(saveFilePath);
        ui = new Ui();
        try {
            storage.readFromSave();
        } catch (ChatterBoxDataFileError e) {
            ui.printMessage(e.getMessage());
        }
        taskList = storage.getSaveList();
        ui.printWelcome();
        ui.printTasks(taskList);
    }

    /**
     * Runs Chatterbox until a bye input is provided by the user.
     */
    public void run() {
        boolean isRunningProgram = true;
        while (isRunningProgram) {
            String userInput = ui.getUserInput();
            try {
                isRunningProgram = doCommand(userInput);
            } catch (ChatterBoxError e) {
                ui.printMessage(e.getMessage());
            }
        }
    }

    /**
     * Performs the command given and returns whether the Chatbot should be terminated.
     * @param input The command from the user.
     * @return If command received should terminate the Chatbot
     * @throws ChatterBoxError For any ChatterBox related errors.
     */
    public boolean doCommand(String input) throws ChatterBoxError {
        String message;
        try {
            String[] command = Parser.processInput(input);
            switch (Commands.valueOf(command[0].toUpperCase())) {
            case BYE:
                storage.writeToSave(taskList);
                ui.printBye();
                return false;
            case LIST:
                ui.printTasks(taskList);
                break;
            case MARK:
                try {
                    message = taskList.getItem(Integer.parseInt(command[1])).setCompleted(true);
                    ui.printMessage(message);
                } catch (IndexOutOfBoundsException e) {
                    throw new ChatterBoxNullTaskError();
                }
                break;
            case UNMARK:
                try {
                    message = taskList.getItem(Integer.parseInt(command[1])).setCompleted(false);
                    ui.printMessage(message);
                } catch (IndexOutOfBoundsException e) {
                    throw new ChatterBoxNullTaskError();
                }
                break;
            case DELETE:
                message = taskList.removeItem(Integer.parseInt(command[1]));
                ui.printMessage(message);
                break;
            case TODO:
                message = taskList.addItem(new ToDo(command[1]));
                ui.printMessage(message);
                break;
            case DEADLINE:
                message = taskList.addItem(
                        new Deadline(command[1], Parser.processDateTime(command[2]))
                );
                ui.printMessage(message);
                break;
            case EVENT:
                message = taskList.addItem(
                        new Event(command[1], Parser.processDateTime(command[2]),
                                Parser.processDateTime(command[3]))
                );
                ui.printMessage(message);
                break;
            case FIND:
                message = taskList.findItem(command[1]);
                ui.printMessage(message);
                break;
            default:
                throw new ChatterBoxError();
            }
        } catch (ChatterBoxError e) {
            throw e;
        }
        return true;
    }

    public static void main(String[] args) {
        new Chatterbox("data/chatterbox_save.txt").run();
    }
}
