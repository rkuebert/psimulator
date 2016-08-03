/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filesystem;

import de.schlichtherle.truezip.file.TFile;
import filesystem.dataStructures.jobs.OutputFileJob;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class FileSystemTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    public FileSystemTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void hello() {

        
        DateFormat format = DateFormat.getInstance();
        String mark = format.format(new Date());

        String fileSystemPath = "test"+System.getProperty("file.separator") + "test"+mark.replaceAll("\\s", "")+ "."+ ArchiveFileSystem.getFileSystemExtension();
        String testPath = "/home/user/history";
        String testedData = "abaca dabaca $;+-/* __ xyz \n dalsi radek";
        FileSystem filesystem = new ArchiveFileSystem(fileSystemPath);

        filesystem.runOutputFileJob(testPath, new OutputFileJob() {

            @Override
            public int workOnFile(OutputStream output) throws Exception {

                PrintWriter writer = new PrintWriter(output);
                writer.println("fuuujjjii...");
                writer.flush();

                return 0;
            }
        });

        filesystem.umount();

        assert new TFile(fileSystemPath).exists();
        assert filesystem.exists(testPath);

    }
}
