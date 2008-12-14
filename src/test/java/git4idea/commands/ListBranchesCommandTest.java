package git4idea.commands;

import com.intellij.openapi.vcs.VcsException;
import git4idea.GitVcs;
import git4idea.actions.GitBranch;
import git4idea.config.GitVcsSettings;
import junit.framework.TestCase;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.List;


public class ListBranchesCommandTest extends TestCase {
    private CommandExecutor executor;
    private GitVcs vcs;
    private GitVcsSettings settings;
    private File workingDirectory;
    private ListBranchesCommand command;

    public void setUp() {
        executor = mock(CommandExecutor.class);
        vcs = mock(GitVcs.class);
        settings = mock(GitVcsSettings.class);

        workingDirectory = new File("working");
        command = new ListBranchesCommand(executor, vcs, workingDirectory, false);
    }

    public void test_execute_Local_OnlyMaster() throws VcsException {
        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");

        List<String> commands = Arrays.asList("git", "branch");

        when(executor.execute(workingDirectory, commands)).thenReturn("* master");

        List<GitBranch> branches = command.execute();

        assertNotNull(branches);
        assertEquals(1, branches.size());

        assertBranch(branches.get(0), "master", true, false);

        Mockito.verify(executor).execute(workingDirectory, commands);
    }

    public void test_execute_Local_MultipleBranches_Linux() throws VcsException {
        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");

        List<String> commands = Arrays.asList("git", "branch");

        when(executor.execute(workingDirectory, commands)).thenReturn("* master\n  branch1");

        List<GitBranch> branches = command.execute();

        assertNotNull(branches);
        assertEquals(2, branches.size());

        assertBranch(branches.get(0), "master", true, false);
        assertBranch(branches.get(1), "branch1", false, false);

        Mockito.verify(executor).execute(workingDirectory, commands);
    }

    public void test_execute_Remote_MultipleBranches_Linux() throws VcsException {
        command = new ListBranchesCommand(executor, vcs, workingDirectory, true);

        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");

        List<String> commands = Arrays.asList("git", "branch", "-r");

        when(executor.execute(workingDirectory, commands)).thenReturn("  origin/HEAD\n  origin/master");

        List<GitBranch> branches = command.execute();

        assertNotNull(branches);
        assertEquals(2, branches.size());

        assertBranch(branches.get(0), "origin/HEAD", false, true);
        assertBranch(branches.get(1), "origin/master", false, true);

        Mockito.verify(executor).execute(workingDirectory, commands);
    }
    
    public void test_execute_Remote_MultipleBranches_Windows() throws VcsException {
        command = new ListBranchesCommand(executor, vcs, workingDirectory, true);

        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");

        List<String> commands = Arrays.asList("git", "branch", "-r");

        when(executor.execute(workingDirectory, commands)).thenReturn("  origin/HEAD\r\n  origin/master");

        List<GitBranch> branches = command.execute();

        assertNotNull(branches);
        assertEquals(2, branches.size());

        assertBranch(branches.get(0), "origin/HEAD", false, true);
        assertBranch(branches.get(1), "origin/master", false, true);

        Mockito.verify(executor).execute(workingDirectory, commands);
    }

    public void test_execute_Local_MultipleBranches_Windows() throws VcsException {
        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");

        List<String> commands = Arrays.asList("git", "branch");

        when(executor.execute(workingDirectory, commands)).thenReturn("* master\r\n  branch1");

        List<GitBranch> branches = command.execute();

        assertNotNull(branches);
        assertEquals(2, branches.size());

        assertBranch(branches.get(0), "master", true, false);
        assertBranch(branches.get(1), "branch1", false, false);

        Mockito.verify(executor).execute(workingDirectory, commands);
    }

    public void test_execute_Local_MultipleBranches_Mac() throws VcsException {
        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");

        List<String> commands = Arrays.asList("git", "branch");

        when(executor.execute(workingDirectory, commands)).thenReturn("* master\r  branch1");

        List<GitBranch> branches = command.execute();

        assertNotNull(branches);
        assertEquals(2, branches.size());

        assertBranch(branches.get(0), "master", true, false);
        assertBranch(branches.get(1), "branch1", false, false);

        Mockito.verify(executor).execute(workingDirectory, commands);
    }

    private void assertBranch(GitBranch branch, String expectedBranchName, boolean expectedActive, boolean expectedRemote) {
        assertEquals(expectedBranchName, branch.getName());
        assertEquals(expectedActive, branch.isActive());
        assertEquals(expectedRemote, branch.isRemote());
    }
}
