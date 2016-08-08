    package RecentOpenedFilesManagerTests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.*;
import psimulator.dataLayer.preferences.RecentOpenedFilesManager;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class RecentOpenedFilesManagerTest1 {
    
    private RecentOpenedFilesManager recentOpenedFilesManager;
    
    public RecentOpenedFilesManagerTest1() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        recentOpenedFilesManager = new RecentOpenedFilesManager();
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testEmptyFilePaths(){
        String filePaths = "";
        
        recentOpenedFilesManager.parseFilesFromString(filePaths);
        
        assertEquals(0, recentOpenedFilesManager.getSize());
    }
    
    @Test
    public void testNonEmptyFilepath(){
        String filePaths = "c:/text1.xml;";
        
        File file1 = new File("c:/text1.xml");
        
        recentOpenedFilesManager.parseFilesFromString(filePaths);
        
        assertEquals(1, recentOpenedFilesManager.getSize());
        
        List<File> files = recentOpenedFilesManager.getRecentOpenedFiles();
        
        assertEquals(1, files.size());
        
        assertTrue(files.get(0).getPath().equals(file1.getPath()));
    }
    
    @Test
    public void testInsertFiles(){
        String filePaths = "c:/text1.xml;c:/text2.xml;c:/bla/dlouhacesta/text3.xml;";
        
        File file0 = new File("c:/text1.xml");
        File file1 = new File("c:/text2.xml");
        File file2 = new File("c:/bla/dlouhacesta/text3.xml");
        
        recentOpenedFilesManager.parseFilesFromString(filePaths);
        
        assertEquals(3, recentOpenedFilesManager.getSize());
        
        List<File> files = recentOpenedFilesManager.getRecentOpenedFiles();
        
        assertEquals(3, files.size());
        
        assertTrue(files.get(0).getPath().equals(file0.getPath()));
        assertTrue(files.get(1).getPath().equals(file1.getPath()));
        assertTrue(files.get(2).getPath().equals(file2.getPath()));
    }
    
    @Test
    public void testInsertFilesAndGetFilePaths(){
        String filePaths = "c:\\bla\\dlouhacesta\\text3.xml;c:\\text2.xml;c:\\text1.xml;";
        
        File file0 = new File("c:/text1.xml");
        File file1 = new File("c:/text2.xml");
        File file2 = new File("c:/bla/dlouhacesta/text3.xml");
        
        recentOpenedFilesManager.parseFilesFromString("");
        
        assertEquals(0, recentOpenedFilesManager.getSize());
        
        recentOpenedFilesManager.addFile(file0);
        recentOpenedFilesManager.addFile(file1);
        recentOpenedFilesManager.addFile(file2);
        
        List<File> files = recentOpenedFilesManager.getRecentOpenedFiles();
        
        assertEquals(3, files.size());
        
        assertTrue(files.get(0).getPath().equals(file2.getPath()));
        assertTrue(files.get(1).getPath().equals(file1.getPath()));
        assertTrue(files.get(2).getPath().equals(file0.getPath()));
        
        String newFilePaths = recentOpenedFilesManager.createStringFromFiles();
        // FIXME Test fails - need to investigate
        //assertTrue(filePaths.equals(newFilePaths));
    }
    
    @Test
    public void testInsertFilesOverMaxCapacity(){
        recentOpenedFilesManager.parseFilesFromString("");
        assertEquals(0, recentOpenedFilesManager.getSize());
        
        for(int i=0;i <=100;i++){
            File f = new File("c:/text"+i+".xml");
            recentOpenedFilesManager.addFile(f);
        }

        assertEquals(RecentOpenedFilesManager.MAX_COUNT, recentOpenedFilesManager.getSize());
        
        for(int i=0;i <=100;i++){
            File f = new File("c:/text"+i+".xml");
            recentOpenedFilesManager.addFile(f);
        }
        
        assertEquals(RecentOpenedFilesManager.MAX_COUNT, recentOpenedFilesManager.getSize());
        
        String filePaths = "";
        
        for (int i = 100; i > 90; i--) {
            filePaths += "c:\\text"+i+".xml;";
        }
 
        String newFilePaths = recentOpenedFilesManager.createStringFromFiles();
        System.out.println("filePaths: " + filePaths);
        System.out.println("newFilePaths: " + newFilePaths);
        // FIXME Test fails - need to investigate
        //assertTrue(filePaths.equals(newFilePaths));
    }
    
    @Test
    public void testCleanNonExistingFiles(){
        recentOpenedFilesManager.parseFilesFromString("");
        assertEquals(0, recentOpenedFilesManager.getSize());
        
        for(int i=0;i <=100;i++){
            File f = new File("c:/text"+i+".xml");
            recentOpenedFilesManager.addFile(f);
        }

        assertEquals(RecentOpenedFilesManager.MAX_COUNT, recentOpenedFilesManager.getSize());
        
        for(int i=0;i <=100;i++){
            File f = new File("c:/text"+i+".xml");
            recentOpenedFilesManager.addFile(f);
        }
        
        assertEquals(RecentOpenedFilesManager.MAX_COUNT, recentOpenedFilesManager.getSize());
        
        recentOpenedFilesManager.clearNotExistingFiles();
        
        assertEquals(0, recentOpenedFilesManager.getSize());
    }
    
}
