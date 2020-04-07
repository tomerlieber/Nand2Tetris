import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CompilationEngine {

    private JackTokenizer tokenizer;
    private String currentToken;
    private TokenType currentTokenType;
    private BufferedWriter bw;
    private String ident = "";
    private List<String> statements = Arrays.asList("let", "if", "while", "do", "return");
    private List<String> op = Arrays.asList("+", "-", "*", "/", "&", "|", "<", ">", "=");
    private List<String> unaryOp = Arrays.asList("-", "~");
    private List<String> keywordConstant = Arrays.asList("true", "false", "null", "this");

    // Create a new compilation engine with the given input and output. The next routine called must be compileClass.
    public CompilationEngine(String input, String output) throws IOException {
        tokenizer = new JackTokenizer(input);
        tokenizer.advance();
        currentToken = tokenizer.getCurrentToken();
        currentTokenType = tokenizer.tokenType();
        bw = new BufferedWriter(new FileWriter(output));
    }

    // This method implements the rule class.
    // 'class' className '{' classVarDec* subroutineDec* '}'
    // It should be called if the current token is 'while'.
    public void compileClass() throws IOException {
        writeRuleStart("class");
        process("class");
        process(TokenType.IDENTIFIER); // className
        process("{");
        while (isClassVarDecRule()) {
            compileClassVarDec();
        }
        while (isSubroutineDecRule()) {
            compileSubroutineDec();
        }
        process("}");
        writeRuleEnd("class");
        bw.close();
    }

    public boolean isClassVarDecRule() {
        return currentToken.equals("static") || currentToken.equals("field");
    }

    // This methods implements the rule classVarDec.
    // ('static' | 'field' ) type varName (',' varName) * ';'
    // It should be called if the current token is 'static' or 'field'
    public void compileClassVarDec() throws IOException {
        writeRuleStart("classVarDec");
        process(); // 'static' | 'field'
        process(); // type
        process(); // varName
        while (currentToken.equals(",")) {
            process(); // ,
            process(); // varName
        }
        process(";");
        writeRuleEnd("classVarDec");
    }


    public boolean isSubroutineDecRule() {
        return currentToken.equals("constructor") || currentToken.equals("function") || currentToken.equals("method");
    }

    // This method implements the rule subroutineDec.
    // ('constructor' | 'function' | 'method') ('void' | type) subroutineName '(' parameterList ')' subroutineBody
    public void compileSubroutineDec() throws IOException {
        writeRuleStart("subroutineDec");
        process(TokenType.KEYWORD); // 'constructor' | 'function' | 'method'
        process(Arrays.asList(TokenType.KEYWORD, TokenType.IDENTIFIER)); // 'void' | type
        process(TokenType.IDENTIFIER); // subroutineName
        process("(");
        compileParameterList();
        process(")");
        compileSubroutineBody();
        writeRuleEnd("subroutineDec");
    }

    // This method implements the rule parameterList
    // ( (type varName) (',' type varName)*)?
    public void compileParameterList() throws IOException {
        writeRuleStart("parameterList");
        if (!currentToken.equals(")")) {
            process(Arrays.asList(TokenType.KEYWORD, TokenType.IDENTIFIER)); // type
            process(TokenType.IDENTIFIER); // varName

            while (currentToken.equals(",")) {
                process(); // ,
                process(Arrays.asList(TokenType.KEYWORD, TokenType.IDENTIFIER)); // type
                process(TokenType.IDENTIFIER); // varName
            }
        }
        writeRuleEnd("parameterList");
    }

    // This method implements the rule subroutineBody
    // '{' varDec* statements '}'
    public void compileSubroutineBody() throws IOException {
        writeRuleStart("subroutineBody");
        process("{");
        while (isVarDecRule()) {
            compileVarDec();
        }
        compileStatements();
        process("}");
        writeRuleEnd("subroutineBody");
    }

    public boolean isVarDecRule() {
        return currentToken.equals("var");
    }

    // This method implements the rule varDec
    // 'var' type varName (',' varName) * ';'
    public void compileVarDec() throws IOException {
        writeRuleStart("varDec");
        process("var"); // var
        process(Arrays.asList(TokenType.KEYWORD, TokenType.IDENTIFIER)); // type
        process(TokenType.IDENTIFIER); // varName

        while (currentToken.equals(",")) {
            process(); // ,
            process(TokenType.IDENTIFIER); // varName
        }
        process(";");
        writeRuleEnd("varDec");
    }

    // This method implements the rule statements
    // statement*
    public void compileStatements() throws IOException {
        writeRuleStart("statements");
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
        writeRuleEnd("statements");
    }

    // This method implements the rule letStatement
    // 'let' varName ('[' expression ']' )? '=' expression ';'
    public void compileLet() throws IOException {
        writeRuleStart("letStatement");
        process("let");
        process(TokenType.IDENTIFIER); // varName
        if (currentToken.equals("[")) {
            process(); // [
            compileExpression();
            process("]");
        }
        process("=");
        compileExpression();
        process(";");
        writeRuleEnd("letStatement");
    }

    // This method implements the rule ifStatement
    // 'if' '(' expression ')' '{' statements '}' ( 'else' '{' statements '}' )?
    public void compileIf() throws IOException {
        writeRuleStart("ifStatement");
        process("if");
        process("(");
        compileExpression();
        process(")");
        process("{");
        compileStatements();
        process("}");
        if (currentToken.equals("else")) {
            process(); // else
            process("{");
            compileStatements();
            process("}");
        }
        writeRuleEnd("ifStatement");
    }

    // This method implements the rule whileStatement.
    // 'while' '(' expression ')' '{' statements '}'
    public void compileWhile() throws IOException {
        writeRuleStart("whileStatement");
        process("while");
        process("(");
        compileExpression();
        process(")");
        process("{");
        compileStatements();
        process("}");
        writeRuleEnd("whileStatement");
    }

    // This method implements the rule doStatement
    // 'do' subroutineCall ';'
    public void compileDo() throws IOException {
        writeRuleStart("doStatement");
        process("do");
        compileSubroutineCall();
        process(";");
        writeRuleEnd("doStatement");
    }

    // This method implements the rule subroutineCall
    // subroutineName '(' expressionList ')' | ( className | varName) '.' subroutineName '(' expressionList ')'
    private void compileSubroutineCall() throws IOException {
        process(TokenType.IDENTIFIER); // subroutineName | (className| varName)
        if (currentToken.equals("(")) {
            process(); // (
            compileExpressionList();
            process(")");
        } else if (currentToken.equals(".")) {
            process(); // .
            process(TokenType.IDENTIFIER); // subroutineName
            process("(");
            compileExpressionList();
            process(")");
        } else {
            throw new RuntimeException("Syntax Error");
        }
    }

    // This method implements the rule returnStatement
    // 'return' expression? ';'
    public void compileReturn() throws IOException {
        writeRuleStart("returnStatement");
        process("return");
        if (!currentToken.equals(";")) {
            compileExpression();
        }
        process(";");
        writeRuleEnd("returnStatement");
    }

    // This method implements the rule expression
    // term (op term) *
    public void compileExpression() throws IOException {
        writeRuleStart("expression");
        compileTerm();
        while (op.contains(currentToken)) {
            process(TokenType.SYMBOL);
            compileTerm();
        }
        writeRuleEnd("expression");
    }

    // // This method implements the rule term
    // integerConstant | stringConstant | keywordConstant | varName | varName '[' expression ']' |
    // subroutineCall | '(' expression ')' | (unaryOp term)
    public void compileTerm() throws IOException {
        writeRuleStart("term");
        switch (currentTokenType) {
            case INT_CONST:
                process(); // integerConstant
                break;
            case STRING_CONST:
                process(); // stringConstant
                break;
            case KEYWORD:
                if (keywordConstant.contains(currentToken)) {
                    process(); // keywordConstant
                } else {
                    throw new RuntimeException("Syntax Error");
                }
                break;
            case IDENTIFIER: {
                process(); // varName | subroutineName | (className | varName)
                if (currentToken.equals("[")) {
                    process(); // [
                    compileExpression();
                    process("]");
                } else if (currentToken.equals("(")) {
                    process(); // (
                    compileExpressionList();
                    process(")");
                } else if (currentToken.equals(".")) {
                    process(); // .
                    process(TokenType.IDENTIFIER);
                    process("(");
                    compileExpressionList();
                    process(")");
                }
                break;
            }
            case SYMBOL: {
                if (currentToken.equals("(")) {
                    process(); // (
                    compileExpression();
                    process(")");
                } else if (unaryOp.contains(currentToken)) {
                    process(); // unaryOp
                    compileTerm();
                } else {
                    throw new RuntimeException("Syntax Error");
                }
                break;
            }
        }
        writeRuleEnd("term");
    }

    // This method implements the rule expressionList
    // (expression (',' expression) * )?
    public void compileExpressionList() throws IOException {
        writeRuleStart("expressionList");
        if (isExpressionRule()) {
            compileExpression();
            while (currentToken.equals(",")) {
                process(); // ,
                compileExpression();
            }
        }
        writeRuleEnd("expressionList");
    }

    private boolean isExpressionRule() {
        return currentTokenType == TokenType.INT_CONST || currentTokenType == TokenType.STRING_CONST ||
                keywordConstant.contains(currentToken) || currentTokenType == TokenType.IDENTIFIER ||
                currentToken.equals("(") || unaryOp.contains(currentToken);
    }

    private void writeToken() throws IOException {

        // Replace symbols that are also used for XML markup.
        if (currentToken.equals("<")) {
            currentToken = "&lt;";
        } else if (currentToken.equals(">")) {
            currentToken = "&gt;";
        } else if (currentToken.equals("&")) {
            currentToken = "&amp;";
        } else if (currentToken.contains("\"")) {
            currentToken = currentToken.replaceAll("\"", "&quot;");
        }

        bw.write(String.format("%1$s<%2$s> %3$s </%2$s>%4$s", ident, currentTokenType.label, currentToken,
                System.lineSeparator()));
    }

    private void writeRuleStart(String rule) throws IOException {
        bw.write(String.format("%s<%s>%s", ident, rule, System.lineSeparator()));
        ident += "\t";
    }

    private void writeRuleEnd(String rule) throws IOException {
        if (ident.length() > 0) {
            ident = ident.substring(0, ident.length() - 1);
        }
        bw.write(String.format("%s</%s>%s", ident, rule, System.lineSeparator()));
    }

    // A helper method that handles the current token, and advances to get the next token.
    private void process(String token, List<TokenType> tokenTypes) throws IOException {
        if ((token != null && token.isEmpty()) || currentToken.equals(token) ||
                (tokenTypes != null && tokenTypes.contains(currentTokenType))) {
            writeToken();
        } else {
            throw new RuntimeException("Syntax error");
        }

        if (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }
        currentToken = tokenizer.getCurrentToken();
        currentTokenType = tokenizer.tokenType();
    }

    private void process(TokenType tokenType) throws IOException {
        process(null, Collections.singletonList(tokenType));
    }

    private void process(List<TokenType> tokenTypes) throws IOException {
        process(null, tokenTypes);
    }

    private void process(String token) throws IOException {
        process(token, null);
    }

    private void process() throws IOException {
        process("");
    }
}