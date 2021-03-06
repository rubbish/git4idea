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
 * Copyright 2007 Decentrix Inc
 * Copyright 2007 Aspiro AS
 * Copyright 2008 MQSoftware
 * Authors: gevession, Erlend Simonsen & Mark Scott
 *
 * This code was originally derived from the MKS & Mercurial IDEA VCS plugins
 */

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.config.GitVcsSettings;
import git4idea.vfs.GitVirtualFileAdapter;
import git4idea.commands.GitCommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Git "delete" action
 */
public class Delete extends BasicAction {
    private static final String DELETE_TITLE = "Delete file";
    private static final String DELETE_MESSAGE = "Delete file(s) from Git?\n{0}";

    @Override
    public void perform(@NotNull Project project, GitVcs vcs, @NotNull List<VcsException> exceptions,
                        @NotNull VirtualFile[] affectedFiles) throws VcsException {
        saveAll();

        if (!ProjectLevelVcsManager.getInstance(project).checkAllFilesAreUnder(GitVcs.getInstance(project), affectedFiles))
            return;

        List<VirtualFile> files = new ArrayList<VirtualFile>();
        files.addAll(Arrays.asList(affectedFiles));
        Collection<VirtualFile> filesToDelete;
        VcsShowConfirmationOption option = vcs.getAddConfirmation();
        AbstractVcsHelper helper = AbstractVcsHelper.getInstance(project);
        filesToDelete = helper.selectFilesToProcess(files, DELETE_TITLE, null, DELETE_TITLE, DELETE_MESSAGE, option);

        if (filesToDelete == null || filesToDelete.size() == 0)
            return;

        final Map<VirtualFile, List<VirtualFile>> roots = GitUtil.sortFilesByVcsRoot(project, filesToDelete);

        for (VirtualFile root : roots.keySet()) {
            GitCommand command = new GitCommand(project, vcs.getSettings(), root);
            List<VirtualFile> list = roots.get(root);
            VirtualFile[] vfiles = list.toArray(new VirtualFile[list.size()]);
            command.delete(vfiles);
        }

        VcsDirtyScopeManager mgr = VcsDirtyScopeManager.getInstance(project);
        for (VirtualFile file : affectedFiles) {
            mgr.fileDirty(file);
            file.refresh(true, true);
        }

    }

    public static void deleteFiles(@NotNull Project project, @NotNull VirtualFile[] files) throws VcsException {
        final Map<VirtualFile, List<VirtualFile>> roots = GitUtil.sortFilesByVcsRoot(project, files);
        for (VirtualFile root : roots.keySet()) {
            GitCommand command = new GitCommand(project, GitVcsSettings.getInstance(project), root);
            List<VirtualFile> list = roots.get(root);
            VirtualFile[] vfiles = list.toArray(new VirtualFile[list.size()]);
            command.delete(vfiles);
        }

        VcsDirtyScopeManager mgr = VcsDirtyScopeManager.getInstance(project);
        for (VirtualFile file : files) {
            mgr.fileDirty(file);
            file.refresh(true, true);
        }
    }

    @Override
    @NotNull
    protected String getActionName(@NotNull AbstractVcs abstractvcs) {
        return "Delete";
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull GitVcs vcs, @NotNull VirtualFile... vFiles) {
        GitVirtualFileAdapter fa = vcs.getFileAdapter();
        for (VirtualFile file : vFiles)
            return fa.isFileProcessable(file);

        return true;
    }
}