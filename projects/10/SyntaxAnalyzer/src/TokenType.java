public enum TokenType {
    KEYWORD("keyword"),
    SYMBOL("symbol"),
    IDENTIFIER("identifier"),
    INT_CONST("integerConstant"),
    STRING_CONST("stringConstant");

    public final String label;

    TokenType(String label) {
        this.label = label;
    }
}