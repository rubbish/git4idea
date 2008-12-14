package git4idea.commands;

import git4idea.GitVcs;
import git4idea.actions.GitBranch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ListBranchesCommand extends AbstractCommand<List<GitBranch>> {
    private boolean remoteBranches;

    public ListBranchesCommand(CommandExecutor executor, GitVcs vcs, File workingDirectory, boolean remoteBranches) {
        super(executor, vcs, workingDirectory);
        this.remoteBranches = remoteBranches;
    }

    protected void addAdditionalArguments(List<String> args) {
        args.add("branch");
        if (remoteBranches) {
            args.add("-r");
        }
    }

    protected List<GitBranch> handleCommandOutput(String output) {
        ArrayList<GitBranch> branches = new ArrayList<GitBranch>();
        for (String line : output.split("[\\n|\\r]")) {
            if (line.trim().startsWith("*")) {
                branches.add(new GitBranch(line.trim().substring(2), true, remoteBranches));
            } else if (line.trim().length() > 0) {
                branches.add(new GitBranch(line.trim(), false, remoteBranches));
            }
        }
        return branches;
    }
}
