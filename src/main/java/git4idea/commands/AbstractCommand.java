package git4idea.commands;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import git4idea.GitVcs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public abstract class AbstractCommand<T> implements Command<T> {
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("[MM/dd/yyyy HH:mm:ss]");
    private CommandExecutor executor;
    private GitVcs versionControlSystem;
    private File workingDirectory;

    protected AbstractCommand(CommandExecutor executor, GitVcs versionControlSystem, File workingDirectory) {
        this.executor = executor;
        this.versionControlSystem = versionControlSystem;
        this.workingDirectory = workingDirectory;
    }

    public final T execute() throws VcsException {
        List<String> commands = new ArrayList<String>();
        addAdditionalArguments(commands);
        log(commands);
        commands.add(0, versionControlSystem.getSettings().locationOfGit());
        return handleCommandOutput(executor.execute(workingDirectory, commands));
    }

    protected abstract void addAdditionalArguments(List<String> args);

    protected abstract T handleCommandOutput(String output) throws VcsException;

    private void log(List<String> args) {
        List<String> commands = new ArrayList<String>();
        commands.add("git");
        commands.addAll(args);
        log(StringUtil.join(commands, " "));
    }

    protected void log(String message) {
        versionControlSystem.showMessage(dateFormatter.format(new Date()) + " " + message);
    }
}
