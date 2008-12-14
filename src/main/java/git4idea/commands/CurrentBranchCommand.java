package git4idea.commands;

import git4idea.GitVcs;

import java.util.List;
import java.io.File;


public class CurrentBranchCommand extends AbstractCommand<String> {
    protected CurrentBranchCommand(CommandExecutor executor, GitVcs versionControlSystem, File workingDirectory) {
        super(executor, versionControlSystem, workingDirectory);
    }

    protected void addAdditionalArguments(List<String> args) {
        args.add("branch");
    }

    protected String handleCommandOutput(String output) {
        String[] lines = output.split("[\\n|\\r]");
        for (String line : lines) {
            if (line.startsWith("*")) {
                return line.substring(2);
            }
        }
        return "master";
    }
}
