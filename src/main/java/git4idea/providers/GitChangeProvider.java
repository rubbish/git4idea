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
 * Copyright 2007 Decentrix Inc
 * Copyright 2007 Aspiro AS
 * Copyright 2008 MQSoftware
 * Authors: gevession, Erlend Simonsen & Mark Scott
 *
 * This code was originally derived from the MKS & Mercurial IDEA VCS plugins
 */

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import git4idea.changes.ChangeMonitor;
import git4idea.commands.GitCommand;
import git4idea.config.GitVcsSettings;
import git4idea.vfs.GitContentRevision;
import git4idea.vfs.GitRevisionNumber;
import git4idea.vfs.GitVirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.List;

/**
 * Git repository change provide
 */
public class GitChangeProvider implements ChangeProvider {
    private Project project;
    private GitVcsSettings settings;

    public GitChangeProvider(@NotNull Project project, @NotNull GitVcsSettings settings) {
        this.project = project;
        this.settings = settings;
    }

    public void getChanges(VcsDirtyScope dirtyScope, ChangelistBuilder builder, ProgressIndicator progress, ChangeListManagerGate changeListManagerGate) throws VcsException {
        Collection<VirtualFile> roots = dirtyScope.getAffectedContentRoots();
        ChangeMonitor mon = ChangeMonitor.getInstance(project);
        FileTypeManager ftm = FileTypeManager.getInstance();
        for (VirtualFile root : roots) {
            GitCommand command = new GitCommand(project, settings, root);

            // process Git cached/indexed files
            Set<GitVirtualFile> files = command.gitCachedFiles();
            for (GitVirtualFile file : files) {
                 if (ftm.isFileIgnored(file.getPath())) {     // IDEA (not Git) is configured to ignore this file
                        builder.processIgnoredFile(file);
                     continue;
                 }
                Change c = getChange(file);
                if (c != null)
                    builder.processChange(c);
            }
            // process Git uncached modified files
            Set<String> unCachedFilenames = mon.getUncachedFiles(root);
            if (unCachedFilenames != null && unCachedFilenames.size() > 0) {
                for (String filename : unCachedFilenames) {
                    if (filename == null || ftm.isFileIgnored(filename)) continue;
                    GitVirtualFile file = new GitVirtualFile(project, filename, GitVirtualFile.Status.MODIFIED);
                    Change c = getChange(file);
                    if (c != null)
                        builder.processChange(c);
                }
            }
            // process Git unversioned files
            Set<String> otherFilenames = mon.getOtherFiles(root);
            if (otherFilenames != null && otherFilenames.size() > 0) {
                for (String filename : otherFilenames) {
                    if (filename == null) continue;
                    if (ftm.isFileIgnored(filename)) // IDEA (not Git) is configured to ignore this file
                        builder.processIgnoredFile(
                                new GitVirtualFile(project, filename, GitVirtualFile.Status.IGNORED));
                    else
                        builder.processUnversionedFile(
                                new GitVirtualFile(project, filename, GitVirtualFile.Status.UNVERSIONED));
                }
            }
            // process Git configured ignored files
//            Set<String> ignoredFilenames = mon.getIgnoredFiles(root);
//            if (ignoredFilenames != null && ignoredFilenames.size() > 0) {
//                for (String filename : ignoredFilenames) {
//                    if (filename == null) continue;
//                        builder.processIgnoredFile(
//                                new GitVirtualFile(project, filename, GitVirtualFile.Status.IGNORED));
//                }
//            }
        }
    }


    public boolean isModifiedDocumentTrackingRequired() {
        return false;
    }

    public void doCleanup(List<VirtualFile> virtualFiles) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private Change getChange(GitVirtualFile file) {
        if (file == null) return null;
        ContentRevision beforeRev = new GitContentRevision(file, new GitRevisionNumber(
                GitRevisionNumber.TIP, new Date(file.getModificationStamp())), project);
        ContentRevision afterRev = CurrentContentRevision.create(VcsUtil.getFilePath(file.getPath()));

        Change c = null;
        switch (file.getStatus()) {
            case UNMERGED: {
                c = new Change(beforeRev, afterRev, FileStatus.MERGED_WITH_CONFLICTS);
                break;
            }
            case ADDED: {
                c = new Change(null, afterRev, FileStatus.ADDED);
                break;
            }
            case DELETED: {
                c = new Change(beforeRev, null, FileStatus.DELETED);
                break;
            }
            case COPY:
            case RENAME:
            case MODIFIED: {
                c = new Change(beforeRev, afterRev, FileStatus.MODIFIED);
                break;
            }
            case UNMODIFIED: {
                break;
            }
            case IGNORED: {
                c = new Change(null, afterRev, FileStatus.IGNORED);
                break;
            }
            case UNVERSIONED:
            default: {
                c = new Change(null, afterRev, FileStatus.UNKNOWN);
            }
        }
        return c;
    }
}