import java.io.File;
import java.io.FilenameFilter;

public class JackFileFilter implements FilenameFilter {

    @Override
    public boolean accept(File directory, String fileName) {
        return fileName.endsWith(".jack");
    }
}