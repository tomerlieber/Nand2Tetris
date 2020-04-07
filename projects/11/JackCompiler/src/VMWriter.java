
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {

    private BufferedWriter bw;

    public enum Segment {
        CONST("constant"),
        ARG("argument"),
        LOCAL("local"),
        STATIC("static"),
        THIS("this"),
        THAT("that"),
        POINTER("pointer"),
        TEMP("temp");

        public final String label;

        Segment(String label) {
            this.label = label;
        }

        public static Segment fromKind(SymbolTable.Kind kind) {
            switch (kind) {
                case STATIC:
                    return Segment.STATIC;
                case FIELD:
                    return Segment.THIS;
                case ARG:
                    return Segment.ARG;
                case VAR:
                    return Segment.LOCAL;
                default:
                    return null;
            }
        }
    }

    public enum Command {
        ADD("add"),
        SUB("sub"),
        NEG("neg"),
        EQ("eq"),
        GT("gt"),
        LT("lt"),
        AND("and"),
        OR("or"),
        NOT("not");

        public final String label;

        Command(String label) {
            this.label = label;
        }

        public static Command fromString(String str) {
            switch (str) {
                case "+":
                    return Command.ADD;
                case "-":
                    return Command.SUB;
                case "<":
                    return Command.LT;
                case ">":
                    return Command.GT;
                case "=":
                    return Command.EQ;
                case "&":
                    return Command.AND;
                case "|":
                    return Command.OR;
                default:
                    throw new RuntimeException("Illegal string input");
            }
        }
    }

    // Create a new output .vm file and prepare it for writing.
    public VMWriter(String output) throws IOException {
        bw = new BufferedWriter(new FileWriter(output));
    }

    // Writes a VM push command.
    public void writePush(Segment segment, int index) throws IOException {
        writeCommand("push " + segment.label + " " + index);
    }

    // Writes a VM pop command.
    public void writePop(Segment segment, int index) throws IOException {
        writeCommand("pop " + segment.label + " " + index);
    }

    // Write a VM arithmetic-logical command.
    public void writeArithmetic(Command command) throws IOException {
        writeCommand(command.label);
    }

    // Writes a VM label command.
    public void writeLabel(String label) throws IOException {
        writeCommand("label " + label);
    }

    // Writes a VM goto command.
    public void writeGoto(String label) throws IOException {
        writeCommand("goto " + label);
    }

    // Writes a VM if-goto command.
    public void writeIf(String label) throws IOException {
        writeCommand("if-goto " + label);
    }

    // Writes a VM call command.
    public void writeCall(String name, int nArgs) throws IOException {
        writeCommand("call " + name + " " + nArgs);
    }

    // Writes a VM function command.
    public void writeFunction(String name, int nLocals) throws IOException {
        writeCommand("function " + name + " " + nLocals);
    }

    // Writes a VM return command.
    public void writeReturn() throws IOException {
        writeCommand("return");
    }

    private void writeCommand(String cmd) throws IOException {
        bw.write(cmd + System.lineSeparator());
    }

    // Closes the output file
    public void close() throws IOException {
        bw.close();
    }
}
