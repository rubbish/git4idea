package git4idea.vfs;

import static git4idea.vfs.GitVirtualFile.Status.*;
import static git4idea.vfs.GitVirtualFile.Status.fromString;
import junit.framework.TestCase;


public class StatusTest extends TestCase {
    public void test_fromString() {
        assertEquals(ADDED, fromString("A"));
        assertEquals(MODIFIED, fromString("M"));
        assertEquals(MODIFIED, fromString("H"));
        assertEquals(COPY, fromString("C"));
        assertEquals(RENAME, fromString("R"));
        assertEquals(DELETED, fromString("D"));
        assertEquals(UNMERGED, fromString("U"));
        assertEquals(UNVERSIONED, fromString("X"));
        assertEquals(UNMODIFIED, fromString(null));

    }
}
