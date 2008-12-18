package git4idea.vfs;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

// TODO - needs test coverage
public class VirtualFileUtilImpl implements VirtualFileUtil {
    public String getRelativeFilePath(VirtualFile file, @NotNull final VirtualFile baseDir) {
        if (file == null) return null;
        return getRelativeFilePath(file.getPath(), baseDir);
    }

    public String getRelativeFilePath(String file, @NotNull final VirtualFile baseDir) {
        if (file == null) return null;
        String rfile = file.replace("\\", "/");
        final String basePath = baseDir.getPath();
        if (!rfile.startsWith(basePath)) return rfile;
        else if (rfile.equals(basePath)) return ".";
        return rfile.substring(baseDir.getPath().length() + 1);
    }
}
