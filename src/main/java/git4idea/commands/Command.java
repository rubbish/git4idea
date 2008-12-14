package git4idea.commands;

import com.intellij.openapi.vcs.VcsException;


public interface Command<T> {
    T execute() throws VcsException;
}
