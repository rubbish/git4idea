package git4idea.commands;

import com.intellij.openapi.vcs.VcsException;
import git4idea.GitVcs;
import git4idea.config.GitVcsSettings;
import junit.framework.TestCase;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.io.File;


public class CurrentBranchCommandTest extends TestCase {
    private CommandExecutor executor;
    private GitVcs vcs;
    private GitVcsSettings settings;
    private CurrentBranchCommand command;
    private File workingDirectory;

    protected void setUp() throws Exception {
        super.setUp();
        executor = mock(CommandExecutor.class);
        vcs = mock(GitVcs.class);
        settings = mock(GitVcsSettings.class);

        workingDirectory = new File("working");
        command = new CurrentBranchCommand(executor, vcs, workingDirectory);
    }
    
    public void test_execute_MasterBranch() throws VcsException {
        List<String> commands = Arrays.asList("git", "branch");

        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");
        when(executor.execute(workingDirectory, commands)).thenReturn("* master");

        String branchName = command.execute();

        assertNotNull(branchName);
        assertEquals("master", branchName);

        Mockito.verify(executor).execute(workingDirectory, commands);
    }

    public void test_execute_NotMasterBranch_UnixLineEnding() throws VcsException {
        List<String> commands = Arrays.asList("git", "branch");

        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");
        when(executor.execute(workingDirectory, commands)).thenReturn("  master\n* other");

        String branchName = command.execute();

        assertNotNull(branchName);
        assertEquals("other", branchName);

        Mockito.verify(executor).execute(workingDirectory, commands);
    }
    
    public void test_execute_NotMasterBranch_WindowsLineEnding() throws VcsException {
        List<String> commands = Arrays.asList("git", "branch");

        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");
        when(executor.execute(workingDirectory, commands)).thenReturn("  master\r\n* other");

        String branchName = command.execute();

        assertNotNull(branchName);
        assertEquals("other", branchName);

        Mockito.verify(executor).execute(workingDirectory, commands);
    }
    
    public void test_execute_NotMasterBranch_MacLineEnding() throws VcsException {
        List<String> commands = Arrays.asList("git", "branch");

        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");
        when(executor.execute(workingDirectory, commands)).thenReturn("  master\r* other");

        String branchName = command.execute();

        assertNotNull(branchName);
        assertEquals("other", branchName);

        Mockito.verify(executor).execute(workingDirectory, commands);
    }


}
