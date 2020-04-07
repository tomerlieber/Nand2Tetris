import java.lang.reflect.Field;
import java.util.Hashtable;

public class SymbolTable {

    private Hashtable<String, SymbolMetadata> classSymbols;
    private Hashtable<String, SymbolMetadata> subroutineSymbols;
    private Hashtable<Kind, Integer> indexes;

    public enum Kind {
        STATIC("static"),
        FIELD("field"),
        ARG("argument"),
        VAR("local"),
        None("");

        public final String label;

        Kind(String label) {
            this.label = label;
        }

        public static Kind fromString(String str) {
            switch (str) {
                case "static":
                    return STATIC;
                case "field":
                    return FIELD;
                case "argument":
                    return ARG;
                case "local":
                    return VAR;
                default:
                    return None;
            }
        }
    }

    // Creates a new symbol table
    public SymbolTable() {
        classSymbols = new Hashtable<>();
        subroutineSymbols = new Hashtable<>();
        indexes = new Hashtable<>();

        indexes.put(Kind.FIELD, 0);
        indexes.put(Kind.STATIC, 0);
        indexes.put(Kind.ARG, 0);
        indexes.put(Kind.VAR, 0);
    }

    // Start a new subroutine scope (i.e. resets the subroutine's symbol table).
    public void startSubroutine() {
        subroutineSymbols.clear();
        indexes.put(Kind.ARG,0);
        indexes.put(Kind.VAR,0);
    }

    // Define a new identifier of the given name, type, and kind, and assigns it a running index.
    // STATIC and FIELD identifiers have a class scope, while ARG and VAR identifiers have a subroutine scope.
    public void define(String name, String type, Kind kind) {

        int index = indexes.get(kind);
        SymbolMetadata symbolMetadata = new SymbolMetadata(type,kind,index);

        switch (kind) {
            case ARG: case VAR:
                subroutineSymbols.put(name, symbolMetadata);
                break;
            case STATIC: case FIELD:
                classSymbols.put(name, symbolMetadata);
                break;
        }

        indexes.put(kind, index + 1);
    }

    // Returns the number of variables of the given kind already defined in the current scope.
    public int varCount(Kind kind) {
        return indexes.get(kind);
    }

    // Returns the kind of the named identifier in the current scope.
    // If the identifier is unknown in the current scope, returns NONE.
    public Kind kindOf(String name) {
        SymbolMetadata symbolMetadata = lookUp(name);
        return symbolMetadata != null ? symbolMetadata.getKind() : Kind.None;
    }

    // Returns the type of the named identifier in the current scope.
    public String typeOf(String name) {
        SymbolMetadata symbolMetadata = lookUp(name);
        return symbolMetadata != null ? symbolMetadata.getType() : null;
    }

    // Returns the index assigned to the named identifier.
    public int indexOf(String name) {
        SymbolMetadata symbolMetadata = lookUp(name);
        return symbolMetadata != null ? symbolMetadata.getIndex() : -1;
    }


    // Looks up the variable in the subroutine-level symbol table.
    // If not found, it looks it up in the class-level symbol table.
    private SymbolMetadata lookUp(String name){

        if (subroutineSymbols.containsKey(name)){
            return subroutineSymbols.get(name);
        }else if (classSymbols.containsKey(name)){
            return classSymbols.get(name);
        }else {
            return null;
        }
    }

    public class SymbolMetadata {

        private String type;
        private Kind kind;
        private int index;

        public SymbolMetadata(String type, Kind kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }

        public String getType() {
            return type;
        }

        public Kind getKind() {
            return kind;
        }

        public int getIndex() {
            return index;
        }
    }

}
