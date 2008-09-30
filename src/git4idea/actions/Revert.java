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
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.commands.GitCommand;
import git4idea.config.GitVcsSettings;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Git "revert" action
 */
public class Revert extends BasicAction {
    private static final String REVERT_TITLE = "Revert file";
    private static final String REVERT_MESSAGE = "Revert cached Git changes on file(s)\n{0}";

    @Override
    public void perform(@NotNull Project project, GitVcs vcs, @NotNull List<VcsException> exceptions,
                        @NotNull VirtualFile[] affectedFiles) throws VcsException {
        saveAll();

        List<VirtualFile> files = new ArrayList<VirtualFile>();
        files.addAll(Arrays.asList(affectedFiles));
        Collection<VirtualFile> filesToRevert;
        VcsShowConfirmationOption option = vcs.getDeleteConfirmation();
        AbstractVcsHelper helper = AbstractVcsHelper.getInstance(project);
        filesToRevert = helper.selectFilesToProcess(files, REVERT_TITLE, null, REVERT_TITLE, REVERT_MESSAGE, option);

        if (filesToRevert == null || filesToRevert.size() == 0)
            return;

        final Map<VirtualFile, List<VirtualFile>> roots = GitUtil.sortFilesByVcsRoot(project, affectedFiles);
        for (VirtualFile root : roots.keySet()) {
            GitCommand command = new GitCommand(project, GitVcsSettings.getInstance(project), root);
            List<VirtualFile> rfiles = roots.get(root);
            command.revert(rfiles);
        }

        VcsDirtyScopeManager mgr = VcsDirtyScopeManager.getInstance(project);
        for (VirtualFile file : affectedFiles) {
            mgr.fileDirty(file);
            file.refresh(true, true);
        }
    }

    @Override
    @NotNull
    protected String getActionName(@NotNull AbstractVcs abstractvcs) {
        return "Revert";
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull GitVcs vcs, @NotNull VirtualFile... vFiles) {
        for (VirtualFile file : vFiles) {
            if (!vcs.getFileAdapter().isFileProcessable(file)) return false;
            FileStatus status = FileStatusManager.getInstance(project).getStatus(file);
            if (status == FileStatus.UNKNOWN || status == FileStatus.NOT_CHANGED)
                return false;
        }
        return true;
    }
}