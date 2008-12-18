package git4idea.commands;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import git4idea.vfs.VirtualFileUtil;

import java.io.File;
import java.util.List;


public class AddFileCommand extends AbstractCommand<Void> {
    private VirtualFileUtil fileUtil;
    private VirtualFile vcsRootDir;
    private VirtualFile[] files;

    protected AddFileCommand(CommandExecutor executor, GitVcs versionControlSystem, VirtualFileUtil fileUtil, File workingDirectory, VirtualFile vcsRootDir, VirtualFile... files) {
        super(executor, versionControlSystem, workingDirectory);
        this.fileUtil = fileUtil;
        this.vcsRootDir = vcsRootDir;
        this.files = files;
    }

    protected void addAdditionalArguments(List<String> args) {
        args.add("add");
        for (VirtualFile file : files) {
            args.add(fileUtil.getRelativeFilePath(file, vcsRootDir));
        }
    }

    protected Void handleCommandOutput(String output) throws VcsException {
        return null;
    }

}
