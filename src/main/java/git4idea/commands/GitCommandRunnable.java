package git4idea.commands;
/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 *
 * Copyright 2008 MQSoftware
 * Author: Mark Scott
 *
 * This code was originally derived from the MKS & Mercurial IDEA VCS plugins
 */

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.config.GitVcsSettings;
import git4idea.GitVcs;
import org.jetbrains.annotations.NotNull;

/**
 * Run a Git command as a Runnable
 */
@SuppressWarnings({"JavaDoc"})
public class GitCommandRunnable implements Runnable {
    private String cmd = null;
    private Project project = null;
    private GitVcsSettings settings = null;
    private String[] opts = null;
    private String[] args = null;
    private VirtualFile vcsRoot = null;
    private VcsException vcsEx = null;
    private boolean silent = false;
    private String output = null;

    public GitCommandRunnable(@NotNull final Project project, @NotNull GitVcsSettings settings, @NotNull VirtualFile vcsRoot) {
        this.project = project;
        this.settings = settings;
        this.vcsRoot = vcsRoot;
    }

    public GitCommandRunnable(@NotNull final Project project, @NotNull GitVcsSettings settings, @NotNull VirtualFile vcsRoot,
                              String cmd, String[] opts, String[] args) {
        this.project = project;
        this.settings = settings;
        this.vcsRoot = vcsRoot;
        this.cmd = cmd;
        this.opts = opts;
        this.args = args;
    }

    @SuppressWarnings({"EmptyCatchBlock"})
    @Override
    public void run() {
        if (cmd == null) throw new IllegalStateException("No command set!");
        vcsEx = null;

        ProgressManager manager = ProgressManager.getInstance();
        ProgressIndicator indicator = manager.getProgressIndicator();
        indicator.setText("Git " + cmd + "...");
        indicator.setIndeterminate(true);

        try {
            GitCommand c = new GitCommand(project, settings, vcsRoot);
            output = c.execute(cmd, opts, args, silent);
            if(!silent) {
                GitVcs.getInstance(project).showMessage(output);
            }
        } catch (VcsException e) {
            vcsEx = e;
        }
    }

    /**
     * Returns the exception thrown by the command runnable
     *
     * @return The exception, else null if the command was sucessful
     */
    public VcsException getException() {
        return vcsEx;
    }

    /**
     * Set the runnable's Git command.
     */
    public void setCommand(String cmd) {
        this.cmd = cmd;
    }

    /**
     * Set the runnable's Git command options.
     */
    public void setOptions(String[] opts) {
        this.opts = opts;
    }

    /**
     * Set the runnable's Git command arguments.
     */
    public void setArgs(String[] args) {
        this.args = args;
    }

    /**
     * Set to true if git command output is to NOT be sent the version control console. (Default is false)
     */
    public void setSilent(boolean isSilent) {
        silent = isSilent;
    }

    /**
     * Returns the output (stdout & stderr) from the command.
     *
     * @return the command output
     */
    public String getOutput() {
        return output;
    }

}