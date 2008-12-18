package git4idea.vfs;

import com.intellij.openapi.vfs.VirtualFile;


public interface VirtualFileUtil {
    String getRelativeFilePath(VirtualFile file, VirtualFile baseDir);

    String getRelativeFilePath(String file, VirtualFile baseDir);
}
