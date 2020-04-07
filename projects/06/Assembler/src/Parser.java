import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Unpacks each instruction into its underlying fields
 */
public class Parser {

    private String currentLine = null;
    private int currentLineIndex = 0;
    private List<String> lines;

    /**
     * Opens the input file/stream and gets ready to parse it.
     *
     * @param input Input file or stream
     */
    public Parser(String input) throws IOException {

        // Reads all the lines from the assembly file
        lines = Files.readAllLines(Paths.get(input));

        /// Remove white space
        List<String> linesToRemove = new LinkedList<>();
        // Mark empty lines and line comments
        for (String line: lines) {
            if(line.isEmpty() || line.startsWith("//")) {
                linesToRemove.add(line);
            }
        }

        // Remove empty lines and line comments
        for (String line: linesToRemove) {
            lines.remove(line);
        }

        // Remove in-line comments and indentation
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int index = line.indexOf("//");
            if(index != -1) {
                line = line.substring(0, index);
            }
            lines.set(i, line.trim());
        }
    }

    /**
     * Are there more lines to the input?
     */
    public boolean hasMoreCommands() {
        return currentLineIndex < lines.size();
    }

    /**
     * 1. Reads the next command from the input and makes it the current command.
     * 2. Takes care of whitespace, if necessary.
     * 3. Should be called only if hasMoreCommands() is true.
     * 4. Initially there is no current command.
     */
    public void advance() {
        currentLine = lines.get(currentLineIndex);
        currentLineIndex++;
    }

    /**
     * Returns the type of the current command:
     * A_COMMAND for @xxx where xxx is either a symbol or a decimal number.
     * C_COMMAND for dest=comp;jump
     * L_COMMAND for (xxx) where xxx is a symbol.
     *
     * @return
     */
    public CommandType commandType() {
        switch (currentLine.charAt(0)) {
            case '@':
                return CommandType.A_COMMAND;
            case '(':
                return CommandType.L_COMMAND;
            default:
                return CommandType.C_COMMAND;
        }
    }

    /**
     * 1. Returns the symbol or decimal xxx of the current command @xxx or (xxx)
     * 2. Should be called only when commandType() is A_COMMAND or L_COMMAND.
     *
     * @return
     */
    public String symbol() {
        if(commandType() == CommandType.L_COMMAND) {
            return currentLine.substring(1, currentLine.length() - 1);
        } else {
            return currentLine.substring(1);
        }
    }

    /**
     * 1. Returns the dest mnemonic in the current C-command (8 possibilities).
     * 2. Should be called only when commandType() is C_COMMAND.
     *
     * @return
     */
    public String dest() {
        int index = currentLine.indexOf('=');
        return index != -1 ? currentLine.substring(0, currentLine.indexOf('=')) : "null";
    }

    /**
     * 1. Returns the comp mnemonic in the current C-command (28 possibilities).
     * 2. Should be called only when commandType() is C_COMMAND.
     *
     * @return
     */
    public String comp() {
        if (currentLine.indexOf(';') != -1) {
            return currentLine.substring(currentLine.indexOf('=') + 1, currentLine.indexOf(';'));
        } else {
            return currentLine.substring(currentLine.indexOf('=') + 1);
        }
    }

    /**
     * 1. Returns the jump mnemonic in the current C-command (8 possibilities).
     * 2. Should be called only when commandType() is C_COMMAND.
     *
     * @return
     */
    public String jump() {

        int index = currentLine.indexOf(';');
        return index != -1 ? currentLine.substring(index + 1) : "null";
    }

    public void reset() {
        currentLineIndex = 0;
    }
}
