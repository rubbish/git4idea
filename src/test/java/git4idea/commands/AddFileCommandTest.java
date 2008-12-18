package git4idea.commands;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import git4idea.config.GitVcsSettings;
import git4idea.vfs.GitVirtualFile;
import git4idea.vfs.VirtualFileUtil;
import junit.framework.TestCase;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;


public class AddFileCommandTest extends TestCase {
    private CommandExecutor executor;
    private GitVcs vcs;
    private GitVcsSettings settings;
    private File workingDirectory;
    private AddFileCommand command;
    private VirtualFileUtil fileUtil;
    private VirtualFile root;

    public void setUp() {
        executor = mock(CommandExecutor.class);
        vcs = mock(GitVcs.class);
        settings = mock(GitVcsSettings.class);
        fileUtil = mock(VirtualFileUtil.class);

        workingDirectory = new File("working");
        root = createVirtualFile("root");
    }


    public void test_execute_AddSingleFile() throws VcsException {
        VirtualFile file = createVirtualFile("folder/file1");

        command = new AddFileCommand(executor, vcs, fileUtil, workingDirectory, root, file);

        List<String> args = Arrays.asList("git", "add", "folder/file1");

        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");
        when(executor.execute(workingDirectory, args)).thenReturn("");
        when(fileUtil.getRelativeFilePath(file, root)).thenReturn("folder/file1");

        command.execute();

        verify(executor).execute(workingDirectory, args);
        verify(fileUtil).getRelativeFilePath(file, root);
    }

    public void test_execute_AddMultipleFiles() throws VcsException {
        VirtualFile file1 = createVirtualFile("folder/file1");
        VirtualFile file2 = createVirtualFile("file2");
        command = new AddFileCommand(executor, vcs, fileUtil, workingDirectory, root, file1, file2);

        List<String> args = Arrays.asList("git", "add", "folder/file1", "file2");

        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");
        when(executor.execute(workingDirectory, args)).thenReturn("");
        when(fileUtil.getRelativeFilePath(file1, root)).thenReturn("folder/file1");
        when(fileUtil.getRelativeFilePath(file2, root)).thenReturn("file2");

        command.execute();

        verify(executor).execute(workingDirectory, args);
        verify(fileUtil).getRelativeFilePath(file1, root);
        verify(fileUtil).getRelativeFilePath(file2, root);
    }

    private VirtualFile createVirtualFile(String name) {
        return new GitVirtualFile(null, name);
    }

}
