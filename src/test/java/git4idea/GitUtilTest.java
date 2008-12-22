package git4idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import junit.framework.TestCase;
import static org.mockito.Mockito.*;


public class GitUtilTest extends TestCase {
    private Project project;
    private FilePath filePath;
    private VirtualFile gitDir;
    private VirtualFile projectBaseDir;

    protected void setUp() throws Exception {
        super.setUp();
        project = mock(Project.class);
        filePath = mock(FilePath.class);
        gitDir = new LightVirtualFile(".git");
        projectBaseDir = mock(VirtualFile.class);
    }

    public void test_getVcsRoot_ProjectIsVcsRoot() {
        when(project.getBaseDir()).thenReturn(projectBaseDir);
        when(projectBaseDir.getChildren()).thenReturn(new VirtualFile[]{gitDir});

        VirtualFile vcsRoot = GitUtil.getVcsRoot(project);

        assertNotNull(vcsRoot);
        assertSame(projectBaseDir, vcsRoot);

        verify(project).getBaseDir();
        verify(projectBaseDir).getChildren();
    }

    public void test_getVcsRoot_ProjectIsNestedUnderAnotherProject() {
        VirtualFile parentDir = mock(VirtualFile.class);

        when(project.getBaseDir()).thenReturn(projectBaseDir);
        when(projectBaseDir.getChildren()).thenReturn(new VirtualFile[0]);
        when(projectBaseDir.getParent()).thenReturn(parentDir);
        when(parentDir.getChildren()).thenReturn(new VirtualFile[]{gitDir});

        VirtualFile vcsRoot = GitUtil.getVcsRoot(project);

        assertNotNull(vcsRoot);
        assertSame(parentDir, vcsRoot);

        verify(project).getBaseDir();
        verify(projectBaseDir).getChildren();
        verify(projectBaseDir).getParent();
        verify(parentDir).getChildren();
    }

    public void test_getVcsRoot_ProjectNotVcsRootAndParentDirectoryIsNull() {
        when(project.getBaseDir()).thenReturn(projectBaseDir);
        when(project.getName()).thenReturn("project-name");
        when(projectBaseDir.getChildren()).thenReturn(new VirtualFile[0]);
        when(projectBaseDir.getParent()).thenReturn(null);

        try {
            GitUtil.getVcsRoot(project);
            fail();
        } catch (IllegalArgumentException err) {
            assertEquals("Unable to locate the git repository for project-name", err.getMessage());
        }

        verify(project).getBaseDir();
        verify(project).getName();
        verify(projectBaseDir).getChildren();
        verify(projectBaseDir).getParent();
    }

    public void test_getVcsRoot_ProjectIsNestedMultipleTimes() {
        VirtualFile parentDir = mock(VirtualFile.class);
        VirtualFile parentsParentDir = mock(VirtualFile.class);

        when(project.getBaseDir()).thenReturn(projectBaseDir);
        when(projectBaseDir.getChildren()).thenReturn(new VirtualFile[0]);
        when(projectBaseDir.getParent()).thenReturn(parentDir);
        when(parentDir.getChildren()).thenReturn(new VirtualFile[0]);
        when(parentDir.getParent()).thenReturn(parentsParentDir);
        when(parentsParentDir.getChildren()).thenReturn(new VirtualFile[]{gitDir});

        VirtualFile vcsRoot = GitUtil.getVcsRoot(project);

        assertNotNull(vcsRoot);
        assertSame(parentsParentDir, vcsRoot);

        verify(project).getBaseDir();
        verify(projectBaseDir).getChildren();
        verify(projectBaseDir).getParent();
        verify(parentDir).getChildren();
        verify(parentDir).getParent();
        verify(parentsParentDir).getChildren();
    }

    public void test_getVcsRoot_ProjectIsNestedMultipleTimes_NullParent() {
        VirtualFile parentDir = mock(VirtualFile.class);

        when(project.getBaseDir()).thenReturn(projectBaseDir);
        when(project.getName()).thenReturn("project-name");
        when(projectBaseDir.getChildren()).thenReturn(new VirtualFile[0]);
        when(projectBaseDir.getParent()).thenReturn(parentDir);
        when(parentDir.getChildren()).thenReturn(new VirtualFile[0]);
        when(parentDir.getParent()).thenReturn(null);

        try {
            GitUtil.getVcsRoot(project);
            fail();
        } catch (IllegalArgumentException err) {
            assertEquals("Unable to locate the git repository for project-name", err.getMessage());
        }

        verify(project).getBaseDir();
        verify(projectBaseDir).getChildren();
        verify(projectBaseDir).getParent();
        verify(parentDir).getChildren();
        verify(parentDir).getParent();
    }


}
