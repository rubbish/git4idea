package git4idea.commands;

import com.intellij.openapi.vcs.VcsException;
import junit.framework.TestCase;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class CommandExecutorTest extends TestCase {
    private Process process;
    private ShuntCommandExecutor executor;

    protected void setUp() throws Exception {
        super.setUp();
        process = mock(Process.class);
        executor = new ShuntCommandExecutor();
        executor.process = process;
    }

    public void FAILING_test_execute_ReadingOutputThrowsIOException() throws InterruptedException, VcsException {
        IOException cause = new IOException();

        when(process.getInputStream()).thenReturn(new ExplodingInputStream(cause));

        try {
            executor.execute(new File("."), "ls");
            fail();
        } catch (VcsException err) {
            assertEquals(cause.getMessage(), err.getMessage());
            assertEquals(cause, err.getCause());
        }

        verify(process).getInputStream();
    }

    public void test_execute_WaitForThrowsInterruptedException() throws InterruptedException, VcsException {
        String expectedOutput = "Output";
        InterruptedException cause = new InterruptedException("You have been interrupted!");

        when(process.waitFor()).thenThrow(cause);
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream(expectedOutput.getBytes()));

        try {
            executor.execute(new File("."), "ls");
            fail();
        } catch (VcsException err) {
            assertEquals(expectedOutput, err.getMessage());
            assertEquals(cause, err.getCause());
        }

        verify(process).waitFor();
        verify(process).getInputStream();
    }

    public void test_execute_ReturnCodeZero() throws InterruptedException, VcsException {
        String expectedOutput = "Output";

        when(process.waitFor()).thenReturn(0);
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream(expectedOutput.getBytes()));

        String output = executor.execute(new File("."), "ls");

        assertNotNull(output);
        assertEquals(expectedOutput, output);

        verify(process).waitFor();
        verify(process).getInputStream();
    }

    public void test_execute_ReturnCodeNotZero() throws InterruptedException {
        String expectedOutput = "Output";

        when(process.waitFor()).thenReturn(-1);
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream(expectedOutput.getBytes()));

        try {
            executor.execute(new File("."), "ls");
            fail();
        } catch (VcsException e) {
            assertEquals(expectedOutput, e.getMessage());
        }

        verify(process).waitFor();
        verify(process).getInputStream();
    }


    private static class ShuntCommandExecutor extends CommandExecutor {
        private Process process;

        @Override
        protected Process createProcess(File workingDirectory, String command) {
            return process;
        }
    }

    private static class ExplodingInputStream extends InputStream {
        private IOException error;

        private ExplodingInputStream(IOException error) {
            this.error = error;
        }

        public int read() throws IOException {
            throw error;
        }
    }
}
