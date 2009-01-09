package git4idea;
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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vcs.FilePath;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Git utility/helper methods
 */
public class GitUtil {

    @NotNull
    public static VirtualFile getVcsRoot(@NotNull final Project project) {
        VirtualFile gitRootDir = locateGitRepository(project.getBaseDir());
        if (gitRootDir == null) {
            throw new IllegalArgumentException("Unable to locate the git repository for " + project.getName());
        }
        return gitRootDir;
    }

    private static VirtualFile locateGitRepository(VirtualFile dir) {
        if (dir == null) {
            return null;
        }

        if (containsGitDir(dir.getChildren())) {
            return dir;
        } else {
            return locateGitRepository(dir.getParent());
        }
    }

    private static boolean containsGitDir(VirtualFile[] files) {
        for (VirtualFile file : files) {
            if (".git".equals(file.getName())) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public static VirtualFile getVcsRoot(@NotNull final Project project, @NotNull final VirtualFile virtualFile) {
        return getVcsRoot(project);
    }

    @NotNull
    public static VirtualFile getVcsRoot(@NotNull final Project project, @NotNull final FilePath filePath) {
        return getVcsRoot(project);
    }

    @NotNull
    public static Map<VirtualFile, List<VirtualFile>> sortFilesByVcsRoot(
            @NotNull Project project,
            @NotNull List<VirtualFile> virtualFiles) {
        Map<VirtualFile, List<VirtualFile>> result = new HashMap<VirtualFile, List<VirtualFile>>();

        for (VirtualFile file : virtualFiles) {
            final VirtualFile vcsRoot = getVcsRoot(project, file);
            assert vcsRoot != null;

            List<VirtualFile> files = result.get(vcsRoot);
            if (files == null) {
                files = new ArrayList<VirtualFile>();
                result.put(vcsRoot, files);
            }
            files.add(file);
        }

        return result;
    }

    @NotNull
    public static Map<VirtualFile, List<VirtualFile>> sortFilesByVcsRoot(
            @NotNull Project project,
            @NotNull Collection<VirtualFile> virtualFiles) {
        return sortFilesByVcsRoot(project, new LinkedList<VirtualFile>(virtualFiles));
    }

    @NotNull
    public static Map<VirtualFile, List<VirtualFile>> sortFilesByVcsRoot(Project project, VirtualFile[] affectedFiles) {
        return sortFilesByVcsRoot(project, Arrays.asList(affectedFiles));
    }

    @NotNull
    public static Set<VirtualFile> getVcsRootsForFiles(Project project, VirtualFile[] affectedFiles) {
        Set<VirtualFile> roots = new HashSet<VirtualFile>();
        for (VirtualFile file : affectedFiles) {
            if (file == null) continue;
            roots.add(getVcsRoot(project, file));
        }
        return roots;
    }
}