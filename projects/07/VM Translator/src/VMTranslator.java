import java.io.IOException;

/**
 * drives the process (VMTranslator)
 */
public class VMTranslator {
    public static void main(String[] args) {
        try {
            // Constructs a Parser to handle the input file.
            Parser parser = new Parser(args[0]);

            // Constructs a CodeWriter to handle the output file
            String outputFileName = args[0].replace(".vm", ".asm");
            try (CodeWriter codeWriter = new CodeWriter(outputFileName)) {

                // Marches through the input file, parsing each line and generating code from it
                while (parser.hasMoreCommands()) {
                    parser.advance();

                    switch (parser.commandType()) {
                        case C_PUSH:
                        case C_POP:
                            codeWriter.writePushPop(parser.commandType(), parser.arg1(), parser.arg2());
                            break;
                        case C_ARITHMETIC:
                            codeWriter.writeArithmetic(parser.arg1());
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            System.exit(1);
        }
    }
}
