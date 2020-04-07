import java.io.File;
import java.io.FilenameFilter;

public class JackCompiler {
    public static void main(String[] args) {

        try {
            if (args == null || args.length != 1) {
                throw new IllegalArgumentException("Usage: JackCompiler input");
            }

            File path = new File(args[0]);

            String[] jackFiles = path.isFile() ? new String[]{path.getAbsolutePath()} : path.list(new JackFileFilter());

            if (jackFiles == null) {
                return;
            }

            if (path.isDirectory()) {
                for (int i = 0; i < jackFiles.length; i++) {
                    jackFiles[i] = path.getAbsolutePath() + File.separator + jackFiles[i];
                }
            }

            for (String jackFile : jackFiles) {
                String vmFile = jackFile.replace(".jack", ".vm");
                (new CompilationEngine(jackFile, vmFile)).compileClass();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static class JackFileFilter implements FilenameFilter {

        @Override
        public boolean accept(File directory, String fileName) {
            return fileName.endsWith(".jack");
        }
    }
}
