import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class JackTokenizer {

    private int currentTokenIndex = 0;
    private List<String> tokens = new ArrayList<>();
    private String currentToken;
    List<String> keyword = Arrays.asList("class", "constructor", "function", "method", "field", "static", "var",
            "int", "char", "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else", "while",
            "return");
    List<String> symbols = Arrays.asList("{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/", "&", "|",
            "<", ">", "=", "~");
    List<String> whitespace = Arrays.asList(" ", "\r", "\n", "\t");

    /**
     * Open the the input .jack file and gets ready to tokenize it.
     */
    public JackTokenizer(String input) throws IOException {

        // Read file content as String.
        byte[] encoded = Files.readAllBytes(Paths.get(input));
        String content = new String(encoded, Charset.defaultCharset());

        // Remove // comment until the end of the line
        int beginIndex = content.indexOf("//");
        while (beginIndex != -1) {
            int endIndex = beginIndex + content.substring(beginIndex).indexOf(System.lineSeparator()); // end of file
            content = content.substring(0, beginIndex) + content.substring(endIndex + System.lineSeparator().length());
            beginIndex = content.indexOf("//");
        }

        // Remove /** API comment until closing */
        beginIndex = content.indexOf("/**");
        while (beginIndex != -1) {
            int endIndex = beginIndex + content.substring(beginIndex).indexOf("*/");
            content = content.substring(0, beginIndex) + content.substring(endIndex + "*/".length());
            beginIndex = content.indexOf("/**");
        }

        // Remove /* comment until closing */
        beginIndex = content.indexOf("/*");
        while (beginIndex != -1) {
            int endIndex = beginIndex + content.substring(beginIndex).indexOf("*/");
            content = content.substring(0, beginIndex) + content.substring(endIndex + "*/".length());
            beginIndex = content.indexOf("/*");
        }

        String delim = Arrays.toString(whitespace.toArray()) + Arrays.toString(symbols.toArray());

        // Create a tokenizer object that received as parameters the jack file as string without the comments,
        // the delimiter and a flag. If the flag is true, then the delimiter characters are also returned as tokens.
        StringTokenizer tokenizer = new StringTokenizer(content, delim, true);
        while (tokenizer.hasMoreTokens()) {

            String curr = tokenizer.nextToken();
            // Skip whitespace tokens.
            if (whitespace.contains(curr)) {
                continue;
            }

            // Handle the string constant token
            if (curr.startsWith("\"")) {
                String strConst = curr;
                while (!strConst.endsWith("\"")) {
                    strConst += tokenizer.nextToken();
                }
                curr = strConst;
            }

            tokens.add(curr);
        }
    }

    /**
     * Are there more tokens in the input?
     */
    public boolean hasMoreTokens() {
        return currentTokenIndex < tokens.size();
    }

    /**
     * Reads the next token from the input and make it the current token.
     * Should be called only if hasMoreTokens() is true. Initially there is no current command.
     */
    public void advance() {
        currentToken = tokens.get(currentTokenIndex++);
    }

    /**
     * Returns the type of the current token, as a constant.
     */
    public TokenType tokenType() {

        if (keyword.contains(currentToken)) {
            return TokenType.KEYWORD;
        } else if (symbols.contains(currentToken)) {
            return TokenType.SYMBOL;
        } else if (currentToken.startsWith("\"") && currentToken.endsWith("\"")) {
            return TokenType.STRING_CONST;
        } else {

            // Check if the current token is integer.
            Integer parsedInt = Utils.tryParseInt(currentToken);
            if (parsedInt != null && parsedInt >= 0 && parsedInt <= 32767) {
                return TokenType.INT_CONST;
            }

            // Check if the current token is identifier.
            char firstChar = currentToken.charAt(0);
            if (Character.isLetter(firstChar) || firstChar == '_') {
                for (char c : currentToken.toCharArray()) {
                    if (!(Character.isLetterOrDigit(c) || (c != '_'))) {
                        throw new RuntimeException("The token '" + currentToken + "' doesn't have a matching type.");
                    }
                }

                return TokenType.IDENTIFIER;
            }
        }

        throw new RuntimeException("The token '" + currentToken + "' doesn't have a matching type.");

    }

    // Return the keyword which is the current token, as a constant. Should be called only if the tokenType is KEYWORD.
    public Keyword keyword() {
        return Keyword.valueOf(currentToken.toUpperCase());
    }

    // Return the keyword which is the current token. Should be called only if the tokenType is SYMBOL.
    public char symbol() {
        return currentToken.charAt(0);
    }

    // Return the identifier which is the current token. Should be called only if the tokenType is IDENTIFIER.
    public String identifier() {
        return currentToken;
    }

    // Return the integer value of the current token. Should be called only if the tokenType is INT_CONST.
    public int intVal() {
        return Integer.parseInt(currentToken);
    }

    // Return the string value of the current token, without the two enclosing double quotes.
    // Should be called only if the tokenType is STRING_CONST.
    public String stringVal() {
        return currentToken.substring(1, currentToken.length() - 1);
    }

    public String getCurrentToken() {
        switch (tokenType()) {
            case KEYWORD:
                return keyword().toString().toLowerCase();
            case IDENTIFIER:
                return identifier();
            case STRING_CONST:
                return stringVal();
            case SYMBOL:
                return String.valueOf(symbol());
            case INT_CONST:
                return String.valueOf(intVal());
            default:
                throw new RuntimeException("Illegal token type.");
        }
    }
}