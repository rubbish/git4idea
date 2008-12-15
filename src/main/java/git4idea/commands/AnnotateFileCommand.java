package git4idea.commands;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import git4idea.GitVcs;
import git4idea.providers.GitFileAnnotation;
import git4idea.vfs.GitRevisionNumber;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AnnotateFileCommand extends AbstractCommand<GitFileAnnotation> {
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private static final Pattern COMMIT_PATTERN = Pattern.compile("([0-9A-Za-z]{40})\\s+\\((.+?)\\s+([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2} -[0-9]{4})\\s+([0-9]+)\\)(.*)");
    private Project project;
    private String filePath;

    protected AnnotateFileCommand(CommandExecutor executor, GitVcs versionControlSystem, File workingDirectory, Project project, String filePath) {
        super(executor, versionControlSystem, workingDirectory);
        this.project = project;
        this.filePath = filePath;
    }

    protected void addAdditionalArguments(List args) {
        args.add("blame");
        args.add("-c");
        args.add("-C");
        args.add("-l");
        args.add("--");
        args.add(filePath);
    }

    protected GitFileAnnotation handleCommandOutput(String output) throws VcsException {
        GitFileAnnotation annotation = new GitFileAnnotation(project);
        try {
            for (String line : output.split("\n")) {
                Matcher matcher = COMMIT_PATTERN.matcher(line);
                if (matcher.find()) {
                    String revision = matcher.group(1);
                    String committer = matcher.group(2).trim();
                    Date date = DATE_FORMATTER.parse(matcher.group(3));
                    long lineNumber = Long.valueOf(matcher.group(4));
                    String lineContent = matcher.group(5);
                    annotation.appendLineInfo(date, new GitRevisionNumber(revision, date), committer, lineContent, lineNumber);
                }
            }
        } catch (ParseException err) {
            throw new VcsException("Problem parsing the date", err);
        }
        return annotation;
    }
}
