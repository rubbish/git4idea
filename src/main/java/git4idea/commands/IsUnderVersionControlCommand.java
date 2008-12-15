package git4idea.commands;

import com.intellij.openapi.vcs.VcsException;
import git4idea.GitVcs;

import java.io.File;
import java.util.List;


public class IsUnderVersionControlCommand extends AbstractCommand<Boolean> {
    private String filePath;

    protected IsUnderVersionControlCommand(CommandExecutor executor, GitVcs versionControlSystem, File workingDirectory, String filePath) {
        super(executor, versionControlSystem, workingDirectory);
        this.filePath = filePath;
    }

    protected void addAdditionalArguments(List<String> args) {
        args.add("ls-files");
    }

    protected Boolean handleCommandOutput(String output) throws VcsException {
        return output.contains(filePath);
    }
}
