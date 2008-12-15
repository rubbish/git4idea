package git4idea.commands;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import git4idea.GitVcs;
import git4idea.config.GitVcsSettings;
import git4idea.providers.GitFileAnnotation;
import git4idea.vfs.GitRevisionNumber;
import junit.framework.TestCase;
import static org.mockito.Mockito.*;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class AnnotateFileCommandTest extends TestCase {
    private CommandExecutor executor;
    private GitVcs vcs;
    private GitVcsSettings settings;
    private File workingDirectory;
    private AnnotateFileCommand command;

    public void setUp() {
        executor = mock(CommandExecutor.class);
        vcs = mock(GitVcs.class);
        settings = mock(GitVcsSettings.class);

        workingDirectory = new File("working");
        command = new AnnotateFileCommand(executor, vcs, workingDirectory, null, "filePath");
    }

    public void test_execute_Linux() throws VcsException {
        String output = "0b935a6299b732aa56c92e2d42188918f3ed130c        (Luke Amdor     2008-12-08 18:06:21 -0600       1)<?xml version=\"1.0\"?>\n" +
                "0b935a6299b732aa56c92e2d42188918f3ed130c        (U-Xena\\Mark Scott     2008-12-08 18:06:21 -0600       2) <project>";

        List<String> commands = Arrays.asList(
                "git",
                "blame",
                "-c",
                "-C",
                "-l",
                "--",
                "filePath"
        );

        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");
        when(executor.execute(workingDirectory, commands)).thenReturn(output);

        GitFileAnnotation fileAnnotation = command.execute();

        assertNotNull(fileAnnotation);

        assertLineInfo("0b935a6299b732aa56c92e2d42188918f3ed130c", "2008-12-08 18:06:21 -0600", "Luke Amdor", fileAnnotation.getLineInfo(0));
        assertLineInfo("0b935a6299b732aa56c92e2d42188918f3ed130c", "2008-12-08 18:06:21 -0600", "U-Xena\\Mark Scott", fileAnnotation.getLineInfo(1));
        assertEquals("<?xml version=\"1.0\"?>\n <project>\n", fileAnnotation.getAnnotatedContent());

        verify(executor).execute(workingDirectory, commands);
    }

    public void test_execute_Windows() throws VcsException {
        String output = "0b935a6299b732aa56c92e2d42188918f3ed130c        (Luke Amdor     2008-12-08 18:06:21 -0600       1)<?xml version=\"1.0\"?>\r\n" +
                "0b935a6299b732aa56c92e2d42188918f3ed130c        (Luke Amdor     2008-12-08 18:06:21 -0600       2) <project>";

        List<String> commands = Arrays.asList(
                "git",
                "blame",
                "-c",
                "-C",
                "-l",
                "--",
                "filePath"
        );

        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");
        when(executor.execute(workingDirectory, commands)).thenReturn(output);

        GitFileAnnotation fileAnnotation = command.execute();

        assertNotNull(fileAnnotation);

        assertLineInfo("0b935a6299b732aa56c92e2d42188918f3ed130c", "2008-12-08 18:06:21 -0600", "Luke Amdor", fileAnnotation.getLineInfo(0));
        assertLineInfo("0b935a6299b732aa56c92e2d42188918f3ed130c", "2008-12-08 18:06:21 -0600", "Luke Amdor", fileAnnotation.getLineInfo(1));
        assertEquals("<?xml version=\"1.0\"?>\n <project>\n", fileAnnotation.getAnnotatedContent());

        verify(executor).execute(workingDirectory, commands);
    }

    public void test_execute_UncommittedFile() throws VcsException {
        String output = "0000000000000000000000000000000000000000        (Not Committed Yet      2008-12-14 18:28:32 -0600       1)package git4idea.commands;\n" +
                "0000000000000000000000000000000000000000        (Not Committed Yet      2008-12-14 18:28:32 -0600       2)";

        List<String> commands = Arrays.asList(
                "git",
                "blame",
                "-c",
                "-C",
                "-l",
                "--",
                "filePath"
        );

        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");
        when(executor.execute(workingDirectory, commands)).thenReturn(output);

        GitFileAnnotation fileAnnotation = command.execute();

        assertNotNull(fileAnnotation);
        assertEquals(2, fileAnnotation.getLineInfos().size());
        assertLineInfo("0000000000000000000000000000000000000000", "2008-12-14 18:28:32 -0600", "Not Committed Yet", fileAnnotation.getLineInfo(0));
        assertLineInfo("0000000000000000000000000000000000000000", "2008-12-14 18:28:32 -0600", "Not Committed Yet", fileAnnotation.getLineInfo(1));
        assertEquals("package git4idea.commands;\n\n", fileAnnotation.getAnnotatedContent());

        verify(executor).execute(workingDirectory, commands);
    }

    public void test_execute_SourceCodeHasAPatternSimilarToTheLineNumber() throws VcsException {
        String output = "0000000000000000000000000000000000000000\t(Not Committed Yet\t2008-12-14 19:48:03 -0600\t1)        String output = \"0b935a6299b732aa56c92e2d42188918f3ed130c        (Luke Amdor     2008-12-08 18:06:21 -0600       1)<?xml version=\\\"1.0\\\"?>";

        List<String> commands = Arrays.asList(
                "git",
                "blame",
                "-c",
                "-C",
                "-l",
                "--",
                "filePath"
        );

        when(vcs.getSettings()).thenReturn(settings);
        when(settings.locationOfGit()).thenReturn("git");
        when(executor.execute(workingDirectory, commands)).thenReturn(output);

        GitFileAnnotation fileAnnotation = command.execute();

        assertNotNull(fileAnnotation);
        assertEquals(1, fileAnnotation.getLineInfos().size());
        assertLineInfo("0000000000000000000000000000000000000000", "2008-12-14 19:48:03 -0600", "Not Committed Yet", fileAnnotation.getLineInfo(0));
        assertEquals("        String output = \"0b935a6299b732aa56c92e2d42188918f3ed130c        (Luke Amdor     2008-12-08 18:06:21 -0600       1)<?xml version=\\\"1.0\\\"?>\n", fileAnnotation.getAnnotatedContent());

        verify(executor).execute(workingDirectory, commands);
    }

    private void assertLineInfo(String expectedRevision, String expectedDate, String expectedCommitter, GitFileAnnotation.LineInfo actualLineInfo) {
        assertNotNull(actualLineInfo);
        assertEquals(createDate(expectedDate), actualLineInfo.getDate());
        assertEquals(expectedCommitter, actualLineInfo.getAuthor());
        assertRevisionNumber(expectedRevision, expectedDate, actualLineInfo.getRevision());
    }

    private void assertRevisionNumber(String expectedRevision, String expectedDate, VcsRevisionNumber actualRevisionNumber) {
        assertTrue("Not an instance of GitRevisionNumber", GitRevisionNumber.class.isInstance(actualRevisionNumber));
        GitRevisionNumber revision = (GitRevisionNumber) actualRevisionNumber;
        assertEquals(expectedRevision, revision.getRev());
        assertEquals(createDate(expectedDate), revision.getTimestamp());
    }

    private Date createDate(String value) {
        SimpleDateFormat format = AnnotateFileCommand.DATE_FORMATTER;
        try {
            return format.parse(value);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
