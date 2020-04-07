import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * writes the assembly code that implements the parsed command
 */
public class CodeWriter implements AutoCloseable {

    private static final int staticBaseAddress = 16;
    private static final int tempBaseAddress = 5;
    private BufferedWriter bw;
    private int jumpIndex;

    /**
     * Open the the output file/ stream and gets ready to write into it.
     */
    public CodeWriter(String outputFile) throws IOException {
        bw = new BufferedWriter(new FileWriter(outputFile));
        jumpIndex = 0;
    }

    /**
     * Writes to the output file the assembly code that implements the given arithmetic command.
     */
    public void writeArithmetic(String command) throws IOException {

        StringBuilder assemblyCommands = new StringBuilder();

        switch (command) {
            case "add":
            case "sub":
            case "and":
            case "or":
                assemblyCommands
                        .append("@SP").append(System.lineSeparator())
                        .append("AM=M-1").append(System.lineSeparator())
                        .append("D=M").append(System.lineSeparator())
                        .append("A=A-1").append(System.lineSeparator())
                        .append("M=M" + getOperation(command) + "D").append(System.lineSeparator());
                break;
            case "eq":
            case "gt":
            case "lt":
                assemblyCommands
                        .append("@SP").append(System.lineSeparator())
                        .append("AM=M-1").append(System.lineSeparator())
                        .append("D=M").append(System.lineSeparator())
                        .append("A=A-1").append(System.lineSeparator())
                        .append("D=M-D").append(System.lineSeparator())
                        .append("@IF" + jumpIndex).append(System.lineSeparator())
                        .append("D;" + getOperation(command)).append(System.lineSeparator())
                        .append("@SP").append(System.lineSeparator())
                        .append("A=M-1").append(System.lineSeparator())
                        .append("M=-1").append(System.lineSeparator())
                        .append("@ELSE" + jumpIndex).append(System.lineSeparator())
                        .append("0;JMP").append(System.lineSeparator())
                        .append("(IF" + jumpIndex + ")").append(System.lineSeparator())
                        .append("@SP").append(System.lineSeparator())
                        .append("A=M-1").append(System.lineSeparator())
                        .append("M=0").append(System.lineSeparator())
                        .append("(ELSE" + jumpIndex + ")").append(System.lineSeparator());
                jumpIndex++;
                break;
            case "neg":
                assemblyCommands
                        .append("D=0").append(System.lineSeparator())
                        .append("@SP").append(System.lineSeparator())
                        .append("A=M-1").append(System.lineSeparator())
                        .append("M=D-M").append(System.lineSeparator());
                break;
            case "not":
                assemblyCommands
                        .append("@SP").append(System.lineSeparator())
                        .append("A=M-1").append(System.lineSeparator())
                        .append("M=!M").append(System.lineSeparator());
                break;
            default:
                throw new RuntimeException("Illegal arithmetic command");
        }

        bw.write(assemblyCommands.toString());
    }

    private String getOperation(String command) {
        switch (command) {
            case "add":
                return "+";
            case "sub":
                return "-";
            case "and":
                return "&";
            case "or":
                return "|";
            case "eq":
                return "JNE";
            case "gt":
                return "JLE";
            case "lt":
                return "JGE";
            default:
                throw new RuntimeException("Illegal command");
        }
    }

    /**
     * Writes to the output file assembly code that implements the given command,
     * where command is either C_PUSH or C_POP.
     */
    public void writePushPop(CommandType commandType, String segment, int index) throws IOException {

        switch (commandType) {
            case C_PUSH:
                writePush(segment, index);
                break;
            case C_POP:
                writePop(segment, index);
                break;
            default:
                throw new RuntimeException("Illegal command type");
        }
    }

    private void writePush(String segment, int index) throws IOException {

        StringBuilder assemblyCommands = new StringBuilder();

        switch (segment) {
            case "constant":
                assemblyCommands
                        .append("@").append(index).append(System.lineSeparator())
                        .append("D=A").append(System.lineSeparator());
                break;
            case "local":
            case "argument":
            case "this":
            case "that":
                assemblyCommands
                        .append("@").append(index).append(System.lineSeparator())
                        .append("D=A").append(System.lineSeparator())
                        .append("@").append(getSegmentPointer(segment)).append(System.lineSeparator())
                        .append("A=D+M").append(System.lineSeparator()) // address = i + segmentPointer
                        .append("D=M").append(System.lineSeparator()); // D = *address
                break;
            case "static":
                assemblyCommands
                        .append("@").append(staticBaseAddress + index).append(System.lineSeparator())
                        .append("D=M").append(System.lineSeparator());
                break;
            case "temp":
                assemblyCommands
                        .append("@").append(tempBaseAddress + index).append(System.lineSeparator())
                        .append("D=M").append(System.lineSeparator());
                break;
            case "pointer":
                assemblyCommands
                        .append("@").append(index == 0 ? "THIS" : "THAT").append(System.lineSeparator())
                        .append("D=M").append(System.lineSeparator());
                break;
            default:
                throw new RuntimeException("Illegal segment");
        }

        assemblyCommands
                .append("@SP").append(System.lineSeparator())
                .append("A=M").append(System.lineSeparator())
                .append("M=D").append(System.lineSeparator()) // *SP=D
                .append("@SP").append(System.lineSeparator())
                .append("M=M+1").append(System.lineSeparator()); // SP++

        bw.write(assemblyCommands.toString());
    }

    private String getSegmentPointer(String segment) {
        switch (segment) {
            case "local":
                return "LCL";
            case "argument":
                return "ARG";
            case "this":
                return "THIS";
            case "that":
                return "THAT";
            default:
                throw new RuntimeException("Illegal segment");
        }
    }

    private void writePop(String segment, int index) throws IOException {

        StringBuilder assemblyCommands = new StringBuilder();

        switch (segment) {
            case "local":
            case "argument":
            case "this":
            case "that":
                assemblyCommands
                        .append("@").append(index).append(System.lineSeparator())
                        .append("D=A").append(System.lineSeparator())
                        .append("@").append(getSegmentPointer(segment)).append(System.lineSeparator())
                        .append("D=D+M").append(System.lineSeparator()) // address = i + segmentPointer
                        .append("@R13").append(System.lineSeparator())
                        .append("M=D").append(System.lineSeparator()) // M[13] = address
                        .append("@SP").append(System.lineSeparator())
                        .append("AM=M-1").append(System.lineSeparator()) // SP--
                        .append("D=M").append(System.lineSeparator()) // D = *SP
                        .append("@R13").append(System.lineSeparator())
                        .append("A=M").append(System.lineSeparator());
                break;
            case "static":
                assemblyCommands
                        .append("@SP").append(System.lineSeparator())
                        .append("AM=M-1").append(System.lineSeparator()) // SP--
                        .append("D=M").append(System.lineSeparator()) // D = *SP
                        .append("@").append(staticBaseAddress + index).append(System.lineSeparator());
                break;
            case "temp":
                assemblyCommands
                        .append("@SP").append(System.lineSeparator())
                        .append("AM=M-1").append(System.lineSeparator()) // SP--
                        .append("D=M").append(System.lineSeparator()) // D = *SP
                        .append("@").append(tempBaseAddress + index).append(System.lineSeparator());
                break;
            case "pointer":
                assemblyCommands
                        .append("@SP").append(System.lineSeparator())
                        .append("AM=M-1").append(System.lineSeparator()) // SP--
                        .append("D=M").append(System.lineSeparator()) // D = *SP
                        .append("@").append(index == 0 ? "THIS" : "THAT").append(System.lineSeparator());
                break;
            default:
                throw new RuntimeException("Illegal segment");
        }

        assemblyCommands
                .append("M=D").append(System.lineSeparator()); // *address = *SP

        bw.write(assemblyCommands.toString());
    }

    /**
     * Closes the output file.
     */
    public void close() throws IOException {
        bw.close();
    }
}
