package git4idea.actions;
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
 * Authors: Mark Scott
 *
 * This code was originally derived from the MKS & Mercurial IDEA VCS plugins
 */

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.commands.GitCommand;
import git4idea.providers.GitMergeProvider;
import git4idea.vfs.GitVirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.io.File;

/**
 * Git merge tool for resolving conflicts
 * <p/>
 * You'll need to add a stanza something like the following in your .git/config file for the affected repository. The
 * mergetool must point to a Git supported merge tool.  Note that on MS-Windwos when using the Cygwin version of git,
 * you need to specifiy a valid Cygwin path to the actual merge tool since it will be called from within a Cygwin
 * environment.
 * <p/>
 * [mergetool "kdiff3"]
 * path = /cygdrive/c/Program Files/KDiff3/kdiff3.exe
 * [merge]
 * tool = kdiff3
 */
public class MergeTool extends BasicAction {
    @Override
    public void perform(@NotNull Project project, GitVcs vcs, @NotNull List<VcsException> exceptions,
                        @NotNull VirtualFile[] affectedFiles) throws VcsException {
        saveAll();

        for (VirtualFile file : affectedFiles) {
            GitCommand cmd = new GitCommand(project, vcs.getSettings(), GitUtil.getVcsRoot(project, file));
            final GitVirtualFile gvFile = new GitVirtualFile(project, file.getPath());
            if (cmd.gitStatus(gvFile) != GitVirtualFile.Status.UNMERGED) {
                File f = new File(file.getPath());
                throw new VcsException("File does not need merging!\n" + f.getAbsolutePath());
            }
        }

        AbstractVcsHelper.getInstance(project).showMergeDialog(Arrays.asList(affectedFiles),
                new GitMergeProvider(project, vcs));
    }

    @Override
    @NotNull
    protected String getActionName(@NotNull AbstractVcs abstractvcs) {
        return "MergeTool";
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull GitVcs vcs, @NotNull VirtualFile... vFiles) {
        return true;
    }
}