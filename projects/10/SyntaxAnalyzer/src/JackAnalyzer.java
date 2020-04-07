import java.io.File;
import java.io.IOException;

public class JackAnalyzer {

    public static void main(String[] args) {

        try {
            File path = new File(args[0]);

            String[] jackFiles = path.isFile() ? new String[]{path.getAbsolutePath()} : path.list(new JackFileFilter());

            if(jackFiles == null) {
                return;
            }

            if(path.isDirectory()) {
                for (int i  = 0; i < jackFiles.length; i++) {
                    jackFiles[i] = path.getAbsolutePath() + File.separator + jackFiles[i];
                }
            }

            for (String jackFile : jackFiles) {
                String xmlFile = jackFile.replace(".jack", ".xml");
                (new CompilationEngine(jackFile, xmlFile)).compileClass();
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
