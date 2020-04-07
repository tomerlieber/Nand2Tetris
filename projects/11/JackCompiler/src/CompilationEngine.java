import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CompilationEngine {

    private JackTokenizer tokenizer;
    private SymbolTable symbolTable;
    private VMWriter vmWriter;

    private int labelsCounter;
    private String currentToken;

    private JackTokenizer.TokenType currentTokenType;

    private List<String> statements = Arrays.asList("let", "if", "while", "do", "return");
    private List<String> binaryOps = Arrays.asList("+", "-", "*", "/", "&", "|", "<", ">", "=");
    private List<String> unaryOps = Arrays.asList("-", "~");
    private List<String> keywordConstants = Arrays.asList("true", "false", "null", "this");

    private String className; // needed for methods.
    private String subroutineName;
    private String subroutineType;

    // Create a new compilation engine with the given input and output. The next routine called must be compileClass.
    public CompilationEngine(String input, String output) throws IOException {
        tokenizer = new JackTokenizer(input);
        symbolTable = new SymbolTable();
        vmWriter = new VMWriter(output);
        labelsCounter = 0;
        currentToken = "";
    }

    // This method implements the rule class.
    // 'class' className '{' classVarDec* subroutineDec* '}'
    // It should be called if the current token is 'while'.
    public void compileClass() throws IOException {
        process("class");
        className = process(JackTokenizer.TokenType.IDENTIFIER); // className
        process("{");
        while (isClassVarDecRule()) {
            compileClassVarDec();
        }
        while (isSubroutineDecRule()) {
            compileSubroutineDec();
        }
        process("}");
        vmWriter.close();
    }

    public boolean isClassVarDecRule() {
        return currentToken.equals("static") || currentToken.equals("field");
    }

    // This methods implements the rule classVarDec.
    // ('static' | 'field' ) type varName (',' varName) * ';'
    // It should be called if the current token is 'static' or 'field'
    public void compileClassVarDec() throws IOException {
        String kind = process(); // 'static' | 'field'
        String type = process(); // type
        String name = process(); // varName

        SymbolTable.Kind kindEnum = SymbolTable.Kind.fromString(kind);

        symbolTable.define(name, type, kindEnum);
        while (currentToken.equals(",")) {
            process(); // ,
            name = process(); // varName
            symbolTable.define(name, type, kindEnum);
        }
        process(";");
    }

    public boolean isSubroutineDecRule() {
        return currentToken.equals("constructor") || currentToken.equals("function") || currentToken.equals("method");
    }

    // This method implements the rule subroutineDec.
    // ('constructor' | 'function' | 'method') ('void' | type) subroutineName '(' parameterList ')' subroutineBody
    public void compileSubroutineDec() throws IOException {

        subroutineType = process(JackTokenizer.TokenType.KEYWORD); // 'constructor' | 'function' | 'method'
        symbolTable.startSubroutine();
        if (subroutineType.equals("method")) {
            symbolTable.define("this", className, SymbolTable.Kind.ARG);
        }

        process(Arrays.asList(JackTokenizer.TokenType.KEYWORD, JackTokenizer.TokenType.IDENTIFIER)); // 'void' | type
        subroutineName = process(JackTokenizer.TokenType.IDENTIFIER); // subroutineName

        process("(");
        compileParameterList();
        process(")");
        compileSubroutineBody();
    }

    // This method implements the rule parameterList
    // ( (type varName) (',' type varName)*)?
    public void compileParameterList() throws IOException {
        if (!currentToken.equals(")")) {
            String type = process(Arrays.asList(JackTokenizer.TokenType.KEYWORD, JackTokenizer.TokenType.IDENTIFIER)); // type
            String name = process(JackTokenizer.TokenType.IDENTIFIER); // varName
            symbolTable.define(name, type, SymbolTable.Kind.ARG);

            while (currentToken.equals(",")) {
                process(); // ,
                type = process(Arrays.asList(JackTokenizer.TokenType.KEYWORD, JackTokenizer.TokenType.IDENTIFIER)); // type
                name = process(JackTokenizer.TokenType.IDENTIFIER); // varName
                symbolTable.define(name, type, SymbolTable.Kind.ARG);
            }
        }
    }

    // This method implements the rule subroutineBody
    // '{' varDec* statements '}'
    public void compileSubroutineBody() throws IOException {
        process("{");
        while (isVarDecRule()) {
            compileVarDec();
        }

        String functionName = className + "." + subroutineName;
        int nLocals = symbolTable.varCount(SymbolTable.Kind.VAR);
        vmWriter.writeFunction(functionName, nLocals);

        if (subroutineType.equals("constructor")) {
            // Creates a memory block for representing the new object.
            vmWriter.writePush(VMWriter.Segment.CONST, symbolTable.varCount(SymbolTable.Kind.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            // Set this to the base address of this block.
            vmWriter.writePop(VMWriter.Segment.POINTER, 0);

        } else if (subroutineType.equals("method")) {
            vmWriter.writePush(VMWriter.Segment.ARG, 0);
            vmWriter.writePop(VMWriter.Segment.POINTER, 0); // THIS = argument 0
        }

        compileStatements();
        process("}");
    }

    public boolean isVarDecRule() {
        return currentToken.equals("var");
    }

    // This method implements the rule varDec
    // 'var' type varName (',' varName) * ';'
    public void compileVarDec() throws IOException {
        process("var"); // var
        String type = process(Arrays.asList(JackTokenizer.TokenType.KEYWORD, JackTokenizer.TokenType.IDENTIFIER)); // type
        String name = process(JackTokenizer.TokenType.IDENTIFIER); // varName
        symbolTable.define(name, type, SymbolTable.Kind.VAR);

        while (currentToken.equals(",")) {
            process(); // ,
            name = process(JackTokenizer.TokenType.IDENTIFIER); // varName
            symbolTable.define(name, type, SymbolTable.Kind.VAR);
        }
        process(";");
    }

    // This method implements the rule statements
    // statement*
    public void compileStatements() throws IOException {
        while (statements.contains(currentToken)) {
            switch (currentToken) {
                case "let": {
                    compileLet();
                    break;
                }
                case "if": {
                    compileIf();
                    break;
                }
                case "while": {
                    compileWhile();
                    break;
                }
                case "do": {
                    compileDo();
                    break;
                }
                case "return": {
                    compileReturn();
                    break;
                }
            }
        }
    }

    // This method implements the rule letStatement
    // 'let' varName ('[' expression ']' )? '=' expression ';'
    public void compileLet() throws IOException {
        boolean arrayAssignment = false;
        process("let");
        String varName = process(JackTokenizer.TokenType.IDENTIFIER); // varName

        if (currentToken.equals("[")) {
            arrayAssignment = true;
            process(); // [
            // Push base address
            VMWriter.Segment segment = VMWriter.Segment.fromKind(symbolTable.kindOf(varName));
            vmWriter.writePush(segment, symbolTable.indexOf(varName));
            // Offset
            compileExpression();
            process("]");
            vmWriter.writeArithmetic(VMWriter.Command.ADD);
        }

        process("=");
        compileExpression();
        process(";");

        if (arrayAssignment) {
            // Pops expression value to temp
            vmWriter.writePop(VMWriter.Segment.TEMP, 0);
            // Pops base address+offset into That segment
            vmWriter.writePop(VMWriter.Segment.POINTER, 1);
            // Push the expression value into THAT segment
            vmWriter.writePush(VMWriter.Segment.TEMP, 0);
            vmWriter.writePop(VMWriter.Segment.THAT, 0);

        } else {

            VMWriter.Segment segment = VMWriter.Segment.fromKind(symbolTable.kindOf(varName));
            vmWriter.writePop(segment, symbolTable.indexOf(varName));
        }
    }

    // This method implements the rule ifStatement
    // 'if' '(' expression ')' '{' statements '}' ( 'else' '{' statements '}' )?
    public void compileIf() throws IOException {
        String elseLabel = "L" + labelsCounter++;
        String endLabel = "L" + labelsCounter++;

        process("if");
        process("(");
        compileExpression();
        process(")");
        process("{");

        vmWriter.writeArithmetic(VMWriter.Command.NOT);
        vmWriter.writeIf(elseLabel);

        compileStatements();
        process("}");
        vmWriter.writeGoto(endLabel);
        vmWriter.writeLabel(elseLabel);

        if (currentToken.equals("else")) {
            process(); // else
            process("{");
            compileStatements();
            process("}");
        }

        vmWriter.writeLabel(endLabel);
    }

    // This method implements the rule whileStatement.
    // 'while' '(' expression ')' '{' statements '}'
    public void compileWhile() throws IOException {

        String firstLabel = "L" + labelsCounter++;
        String secondLabel = "L" + labelsCounter++;

        process("while");
        process("(");
        vmWriter.writeLabel(firstLabel);
        compileExpression();
        process(")");
        process("{");
        vmWriter.writeArithmetic(VMWriter.Command.NOT);
        vmWriter.writeIf(secondLabel);
        compileStatements();
        process("}");
        vmWriter.writeGoto(firstLabel);
        vmWriter.writeLabel(secondLabel);
    }

    // This method implements the rule doStatement
    // 'do' subroutineCall ';'
    public void compileDo() throws IOException {
        process("do");
        compileSubroutineCall();
        process(";");
        // The caller of a void method must dump the return value
        vmWriter.writePop(VMWriter.Segment.TEMP, 0);
    }

    // This method implements the rule subroutineCall
    // subroutineName '(' expressionList ')' | ( className | varName) '.' subroutineName '(' expressionList ')'
    private void compileSubroutineCall() throws IOException {
        String functionName;
        int nArgs = 0;
        String firstPart = process(JackTokenizer.TokenType.IDENTIFIER); // subroutineName | (className| varName)

        if (currentToken.equals("(")) {
            vmWriter.writePush(VMWriter.Segment.POINTER, 0);
            process(); // (
            nArgs = compileExpressionList() + 1;
            process(")");
            functionName = className + "." + firstPart;


        } else if (currentToken.equals(".")) {
            process(); // .
            String subroutineName = process(JackTokenizer.TokenType.IDENTIFIER); // subroutineName
            process("(");

            if (symbolTable.indexOf(firstPart) == -1) { // for static methods
                functionName = firstPart + "." + subroutineName;
            } else { // for instance methods
                nArgs = 1;
                VMWriter.Segment segment = VMWriter.Segment.fromKind(symbolTable.kindOf(firstPart));
                vmWriter.writePush(segment, symbolTable.indexOf(firstPart));
                functionName = symbolTable.typeOf(firstPart) + "." + subroutineName;
            }

            nArgs += compileExpressionList();
            process(")");

        } else {
            throw new RuntimeException("Syntax Error");
        }

        vmWriter.writeCall(functionName, nArgs);
    }

    // This method implements the rule returnStatement
    // 'return' expression? ';'
    public void compileReturn() throws IOException {
        process("return");
        if (!currentToken.equals(";")) {
            compileExpression();
        } else {
            // Even void methods must return a value.
            vmWriter.writePush(VMWriter.Segment.CONST, 0);
        }
        process(";");

        vmWriter.writeReturn();
    }

    // This method implements the rule expression
    // term (op term) *
    public void compileExpression() throws IOException {
        compileTerm(); // term

        while (binaryOps.contains(currentToken)) {
            String binaryOp = process(JackTokenizer.TokenType.SYMBOL); // op
            compileTerm(); // term

            switch (binaryOp) {
                case "*":
                    vmWriter.writeCall("Math.multiply", 2);
                    break;
                case "/":
                    vmWriter.writeCall("Math.divide", 2);
                    break;
                case "<":
                case ">":
                case "=":
                case "&":
                case "|":
                case "-":
                case "+":
                    vmWriter.writeArithmetic(VMWriter.Command.fromString(binaryOp));
            }
        }
    }

    // // This method implements the rule term
    // integerConstant | stringConstant | keywordConstant | varName | varName '[' expression ']' |
    // subroutineCall | '(' expression ')' | (unaryOp term)
    public void compileTerm() throws IOException {

        switch (currentTokenType) {

            case INT_CONST:
                String strNum = process(); // integerConstant
                vmWriter.writePush(VMWriter.Segment.CONST, Integer.parseInt((strNum)));
                break;

            case STRING_CONST:
                String str = process(); // stringConstant
                vmWriter.writePush(VMWriter.Segment.CONST, str.length());
                vmWriter.writeCall("String.new", 1);

                for (int i = 0; i < str.length(); i++) {
                    vmWriter.writePush(VMWriter.Segment.CONST, (int) str.charAt(i));
                    vmWriter.writeCall("String.appendChar", 2);
                }
                break;

            case KEYWORD:
                if (keywordConstants.contains(currentToken)) {
                    String keywordConstant = process(); // keywordConstant
                    switch (keywordConstant) {
                        case "this":
                            vmWriter.writePush(VMWriter.Segment.POINTER, 0);
                            break;
                        case "null":
                        case "false":
                            vmWriter.writePush(VMWriter.Segment.CONST, 0);
                            break;
                        case "true":
                            vmWriter.writePush(VMWriter.Segment.CONST, 0);
                            vmWriter.writeArithmetic(VMWriter.Command.NOT);
                            break;
                    }
                } else {
                    throw new RuntimeException("Syntax Error");
                }
                break;

            case IDENTIFIER: {
                String identifier = process(); // varName | varName '[' expression ']' | subroutineName

                if (currentToken.equals("[")) {
                    VMWriter.Segment segment = VMWriter.Segment.fromKind(symbolTable.kindOf(identifier));
                    vmWriter.writePush(segment, symbolTable.indexOf(identifier));
                    process(); // [
                    compileExpression();
                    process("]");

                    vmWriter.writeArithmetic(VMWriter.Command.ADD);
                    vmWriter.writePop(VMWriter.Segment.POINTER, 1);
                    vmWriter.writePush(VMWriter.Segment.THAT, 0);

                } else if (currentToken.equals("(") || currentToken.equals(".")) {

                    tokenizer.decrementTokenIndex();
                    tokenizer.decrementTokenIndex();
                    tokenizer.advance();
                    currentToken = tokenizer.getCurrentToken();
                    currentTokenType = tokenizer.tokenType();

                    compileSubroutineCall();

                } else {
                    VMWriter.Segment segment = VMWriter.Segment.fromKind(symbolTable.kindOf(identifier));
                    vmWriter.writePush(segment, symbolTable.indexOf(identifier));
                }
                break;
            }
            case SYMBOL: {
                if (currentToken.equals("(")) {
                    process(); // (
                    compileExpression();
                    process(")");
                } else if (unaryOps.contains(currentToken)) {
                    String unaryOp = process(); // unaryOp
                    compileTerm(); // term
                    vmWriter.writeArithmetic(unaryOp.equals("-") ? VMWriter.Command.NEG : VMWriter.Command.NOT);
                } else {
                    throw new RuntimeException("Syntax Error");
                }
                break;
            }
        }
    }

    // This method implements the rule expressionList
    // (expression (',' expression) * )?
    public int compileExpressionList() throws IOException {
        int nArgs = 0;

        if (isExpressionRule()) {
            compileExpression();
            nArgs = 1;

            while (currentToken.equals(",")) {
                process(); // ,
                compileExpression();
                nArgs++;
            }
        }

        return nArgs;
    }

    private boolean isExpressionRule() {
        return currentTokenType == JackTokenizer.TokenType.INT_CONST || currentTokenType == JackTokenizer.TokenType.STRING_CONST ||
                keywordConstants.contains(currentToken) || currentTokenType == JackTokenizer.TokenType.IDENTIFIER ||
                currentToken.equals("(") || unaryOps.contains(currentToken);
    }

    // A helper method that checks and returns the current token and advances to get the next token
    private String process(String token, List<JackTokenizer.TokenType> tokenTypes) throws IOException {

        if (currentToken.isEmpty()) {
            tokenizer.advance();
            currentToken = tokenizer.getCurrentToken();
            currentTokenType = tokenizer.tokenType();
        }

        if (!(token != null && token.isEmpty()) && !currentToken.equals(token) &&
                !(tokenTypes != null && tokenTypes.contains(currentTokenType))) {
            throw new RuntimeException("Syntax error");
        }

        String prevToken = currentToken;

        if (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }

        currentToken = tokenizer.getCurrentToken();
        currentTokenType = tokenizer.tokenType();

        return prevToken;
    }

    private String process(JackTokenizer.TokenType tokenType) throws IOException {
        return process(null, Collections.singletonList(tokenType));
    }

    private String process(List<JackTokenizer.TokenType> tokenTypes) throws IOException {
        return process(null, tokenTypes);
    }

    private String process(String token) throws IOException {
        return process(token, null);
    }

    private String process() throws IOException {
        return process("");
    }
}