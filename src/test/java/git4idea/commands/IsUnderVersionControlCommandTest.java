package git4idea.commands;

import com.intellij.openapi.vcs.VcsException;
import git4idea.GitVcs;
import git4idea.config.GitVcsSettings;
import junit.framework.TestCase;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;


public class IsUnderVersionControlCommandTest extends TestCase {
    private CommandExecutor executor;
    private GitVcs vcs;
    private GitVcsSettings settings;
    private File workingDirectory;
    private IsUnderVersionControlCommand command;
    private String filePath;

    public void setUp() {
        executor = mock(CommandExecutor.class);
        vcs = mock(GitVcs.class);
        settings = mock(GitVcsSettings.class);

        filePath = "filePath";
        workingDirectory = new File("working");
        command = new IsUnderVersionControlCommand(executor, vcs, workingDirectory, filePath);
    }

    public void test_execute_FileUnderVersionControl() throws VcsException {
        List<String> commands = Arrays.asList(
                "git",
                "ls-files"
        );

        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");
        when(executor.execute(workingDirectory, commands)).thenReturn(filePath);

        assertTrue(command.execute());
        
        verify(executor).execute(workingDirectory, commands);
    }
    
    public void test_execute_FileNotUnderVersionControl() throws VcsException {
        List<String> commands = Arrays.asList(
                "git",
                "ls-files"
        );

        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");
        when(executor.execute(workingDirectory, commands)).thenReturn("unknownFile");

        assertFalse(command.execute());

        verify(executor).execute(workingDirectory, commands);
    }
}
