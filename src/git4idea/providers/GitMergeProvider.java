package git4idea.providers;
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
 */
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.merge.MergeData;
import com.intellij.openapi.vcs.merge.MergeProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsRunnable;
import com.intellij.vcsUtil.VcsUtil;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.commands.GitCommand;
import git4idea.config.GitVcsSettings;
import git4idea.i18n.GitBundle;
import git4idea.vfs.GitContentRevision;
import git4idea.vfs.GitRevisionNumber;
import git4idea.vfs.GitVirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Merge-changes provider for Git, used by IDEA internal 3-way merge tool
 */
public class GitMergeProvider implements MergeProvider {
    private static final String[] binExts = new String[]{
            ".exe", ".dll", ".so", ".jar", ".war", ".iso", ".class", ".bin", ".zip", ".tgz", ".tar", ".gif", ".png",
            ".jpg", ".tiff", ".psd"};
    private Project project;
    private GitVcsSettings settings;

    public GitMergeProvider(Project proj, GitVcs vcs) {
        this.project = proj;
        this.settings = vcs.getSettings();
    }

    @NotNull
    public MergeData loadRevisions(VirtualFile file) throws VcsException {
        final MergeData mergeData = new MergeData();
        if(file == null) return mergeData;
        final GitVirtualFile gvFile = new GitVirtualFile(project, file.getPath());

        VcsRunnable runnable = new VcsRunnable() {
            @SuppressWarnings({"ConstantConditions"})
            public void run() throws VcsException {
                GitContentRevision original = new GitContentRevision(gvFile, new GitRevisionNumber(":1"), project);
                GitContentRevision current = new GitContentRevision(gvFile, new GitRevisionNumber(":2"), project);
                GitContentRevision last = new GitContentRevision(gvFile, new GitRevisionNumber(":3"), project);
                mergeData.ORIGINAL = original.getContent().getBytes();
                mergeData.CURRENT = current.getContent().getBytes();
                mergeData.LAST = last.getContent().getBytes();
                mergeData.LAST_REVISION_NUMBER = new GitRevisionNumber("Theirs");
            }
        };
        VcsUtil.runVcsProcessWithProgress(runnable, GitBundle.message("multiple.file.merge.loading.progress.title"), false, project);
        return mergeData;
    }

    @SuppressWarnings({"EmptyCatchBlock"})
    public void conflictResolvedForFile(VirtualFile file) {
        if (file == null) return;
        GitCommand cmd = new GitCommand(project, settings, GitUtil.getVcsRoot(project, file));
        try {
            cmd.add(new VirtualFile[]{file});
        } catch (VcsException e) {
        }
    }

    public boolean isBinary(VirtualFile file) {
        if (file == null) return false;
        String fpath = file.getPath().toLowerCase();
        for (String ext : binExts) {
            if (fpath.endsWith(ext))
                return true;
        }
        return false;
    }
}