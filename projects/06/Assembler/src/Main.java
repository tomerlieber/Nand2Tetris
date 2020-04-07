import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Initializes I/O files and drives the process.
 */
public class Main {
    public static void main(String[] args) {

        // 1. Initialization

        // Create an empty symbol table.
        SymbolTable symbolTable = new SymbolTable();
        symbolTable.addPreDefinedSymbols();

        // Change file name extension to hack.
        String outputFileName = args[0].replace(".asm", ".hack");

        // The generated code is written into a text file named xxx.hack
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName))) {

            // Open a text file (containing the source code) with a given name, e.g. Pong.asm
            Parser parser = new Parser(args[0]);

            // 2. First pass
            int lineNumber = 0;
            while (parser.hasMoreCommands()) {
                parser.advance();

                // Adds the found labels to the symbol table.
                if (parser.commandType() == CommandType.L_COMMAND) {
                    String s = parser.symbol();
                    symbolTable.addEntry(s, lineNumber);
                    continue;
                }

                lineNumber++;
            }

            // 3. Second pass
            parser.reset();
            int n = 16;

            while (parser.hasMoreCommands()) {
                parser.advance();

                // Parse the instruction: break it into its underlying fields
                switch (parser.commandType()) {
                    // Translates A-instruction
                    case A_COMMAND:
                        String s = parser.symbol();
                        if (symbolTable.contains(s)) {
                            int address = symbolTable.getAddress(s);
                            // Translates the decimal value into a binary value
                            long binaryValue = Long.parseLong(Long.toBinaryString(address));
                            // Write the 16-bit A instruction to the output file.
                            bw.write(String.format("%016d", binaryValue) + "\n");

                        } else {
                            long binaryValue;

                            // Checks if the value is a non-negative decimal constant
                            // or a symbol referring to such a constant.
                            if (isNumber(s)) {
                                binaryValue = Long.parseLong(Long.toBinaryString(Long.parseLong(s)));
                            } else {
                                // Handling symbols that denote variables.
                                symbolTable.addEntry(s, n);
                                // Translates the decimal value into a binary value
                                binaryValue = Long.parseLong(Long.toBinaryString(n));
                                n++;
                            }
                            bw.write(String.format("%016d", binaryValue) + "\n");
                        }
                        break;
                        // Translate C-instruction
                    case C_COMMAND:
                        // Parses the command.
                        String c = parser.comp();
                        String d = parser.dest();
                        String j = parser.jump();

                        // Translates the command.
                        String cc = Code.comp(c);
                        String dd = Code.dest(d);
                        String jj = Code.jump(j);

                        // Assembles and writes the translated fields
                        bw.write("111" + cc + dd + jj + "\n");
                        break;
                }
            }
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            System.exit(1);
        }
    }

    private static boolean isNumber(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
