import java.util.Hashtable;

/**
 * Manages the symbol table
 */
public class SymbolTable {

    private Hashtable<String, Integer> table;

    /**
     * Creates a new empty symbol table.
     */
    public SymbolTable() {
        table = new Hashtable<>();
    }

    /**
     * Adds the pair (symbol, address) to the table.
     * @param symbol
     * @param address
     */
    public void addEntry(String symbol, int address) {
        table.put(symbol, address);
    }

    /**
     * Does the symbol table contain the given symbol?
     * @param symbol
     * @return
     */
    public boolean contains(String symbol) {
        return table.containsKey(symbol);
    }

    /**
     * Returns the address associated with the symbol.
     * @param symbol
     * @return
     */
    public int getAddress(String symbol) {
        return table.get(symbol);
    }

    public void addPreDefinedSymbols() {
        // Adds the pre-defined symbols to the symbol table.
        this.addEntry("R0", 0);
        this.addEntry("R1", 1);
        this.addEntry("R2", 2);
        this.addEntry("R3", 3);
        this.addEntry("R4", 4);
        this.addEntry("R5", 5);
        this.addEntry("R6", 6);
        this.addEntry("R7", 7);
        this.addEntry("R8", 8);
        this.addEntry("R9", 9);
        this.addEntry("R10", 10);
        this.addEntry("R11", 11);
        this.addEntry("R12", 12);
        this.addEntry("R13", 13);
        this.addEntry("R14", 14);
        this.addEntry("R15", 15);
        this.addEntry("SCREEN", 16384);
        this.addEntry("KBD", 24576);
        this.addEntry("SP", 0);
        this.addEntry("LCL", 1);
        this.addEntry("ARG", 2);
        this.addEntry("THIS", 3);
        this.addEntry("THAT", 4);
    }
}
