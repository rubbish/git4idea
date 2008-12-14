package git4idea.commands;

import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import git4idea.GitVcs;
import git4idea.vfs.GitFileRevision;
import git4idea.vfs.GitRevisionNumber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class LogCommand extends AbstractCommand<List<VcsFileRevision>> {
    private FilePath filePath;

    public LogCommand(CommandExecutor executor, GitVcs versionControlSystem, FilePath filePath) {
        super(executor, versionControlSystem, null);
        this.filePath = filePath;
    }

    protected List<VcsFileRevision> handleCommandOutput(String output) {
        String[] parts = output.split("@@@");

        List<VcsFileRevision> revisions = new ArrayList<VcsFileRevision>();
        revisions.add(createRevision(parts));

        return revisions;
    }

    protected void addAdditionalArguments(List<String> args) {
        args.addAll(Arrays.asList(
                "log",
                "-C",
                "-l5",
                "--find-copies-harder",
                "-n50",
                "--pretty=format:%H@@@%an <%ae>@@@%ct@@@%s",
                "--"
        ));
    }

    private VcsFileRevision createRevision(String parts[]) {
        Date commitDate = new Date(Long.valueOf(parts[2]) * 1000);
        String revstr = parts[0];
        return new GitFileRevision(
                null,
                null,
                new GitRevisionNumber(revstr, commitDate),// git revision id
                parts[1],                // user realname & email
                parts[3],                // commit description
                null);                    // TODO: find branch name for the commit & pass it here
    }

}
