package git4idea.commands;

import com.intellij.openapi.vcs.VcsException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;


public class CommandExecutor {

    public String execute(File workingDirectory, List<String> commands) throws VcsException {
        return execute(workingDirectory, commands.toArray(new String[0]));
    }

    /**
     * Shell out a command and capture the output
     *
     * @param workingDirectory - the directory to execute the command from
     * @param commands         - the commands to be executed
     * @return the output from executing the given command
     * @throws VcsException when a problem occurs while executing the given command
     */
    public String execute(File workingDirectory, String... commands) throws VcsException {
        StringBuilder builder = new StringBuilder();
        BufferedInputStream input = null;
        Process process = null;
        try {
            process = createProcess(workingDirectory, commands);
            input = new BufferedInputStream(process.getInputStream());
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            int length = 0;
            byte[] buffer = new byte[16 * 1024];
            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
                output.flush();
            }
            builder.append(new String(output.toByteArray()));
            if (process.waitFor() != 0) {
                throw new VcsException(builder.toString());
            }
        } catch (InterruptedException e) {
            throw new VcsException(builder.toString(), e);
        } catch (IOException e) {
            throw new VcsException(e.getMessage(), e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {

                }
            }
            if (process != null) {
                process.destroy();
            }
        }
        return builder.toString();
    }

    protected Process createProcess(File workingDirectory, String[] commands) throws VcsException {
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.redirectErrorStream(true);
        pb.directory(workingDirectory);
        try {
            return pb.start();
        } catch (IOException e) {
            throw new VcsException(e);
        }
    }
}
