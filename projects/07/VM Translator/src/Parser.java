import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * parses each VM command into its lexical elements
 */
public class Parser {

    private String[] currentLineParts = null;
    private int currentLineIndex = 0;
    private List<String> lines;

    /**
     * Open the the input file/ stream and gets ready to parse it.
     */
    public Parser(String input) throws IOException {
        // Reads all the lines from the assembly file
        lines = Files.readAllLines(Paths.get(input));

        /// Remove white space
        List<String> linesToRemove = new LinkedList<>();
        // Mark empty lines and line comments
        for (String line : lines) {
            if (line.isEmpty() || line.startsWith("//")) {
                linesToRemove.add(line);
            }
        }

        // Remove empty lines and line comments
        for (String line : linesToRemove) {
            lines.remove(line);
        }

        // Remove in-line comments and indentation
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int index = line.indexOf("//");
            if (index != -1) {
                line = line.substring(0, index);
            }
            lines.set(i, line.trim());
        }
    }

    /**
     * Are there more commands in the input?
     */
    public boolean hasMoreCommands() {
        return currentLineIndex < lines.size();
    }

    /**
     * Reads the next command from the input and make it the current command.
     * Should be called only if hasMoreCommands() is true. Initially there is no current command.
     */
    public void advance() {
        currentLineParts = lines.get(currentLineIndex).split(" ");
        currentLineIndex++;
    }

    /**
     * Returns a constant representing the type of the current command.
     * C_ARITHMETIC is returned for all arithmetic/ logical commands.
     */
    public CommandType commandType() {
        switch (currentLineParts[0]) {
            case "push":
                return CommandType.C_PUSH;
            case "pop":
                return CommandType.C_POP;
            default:
                return CommandType.C_ARITHMETIC;
        }
    }

    /**
     * Returns the first argument of the current command.
     * In the case of C_ARITHMETIC the command itself (add, sub, etc.) is returned.
     * Should not be called if the current command is C_RETURN.
     *
     * @return
     */
    public String arg1() {
        return commandType() != CommandType.C_ARITHMETIC ? currentLineParts[1] : currentLineParts[0];
    }

    /**
     * Returns the second argument of the current command.
     * Should be called only if the current command is C_PUSH, C_POP, C_FUNCTION or C_CALL.
     *
     * @return
     */
    public int arg2() {
        return Integer.parseInt(currentLineParts[2]);
    }
}
