package filesystem;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.FsEntryNotFoundException;
import de.schlichtherle.truezip.fs.FsSyncException;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.fs.nio.file.FileDriver;
import de.schlichtherle.truezip.nio.file.TPath;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import filesystem.dataStructures.Directory;
import filesystem.dataStructures.File;
import filesystem.dataStructures.Node;
import filesystem.dataStructures.NodesWrapper;
import filesystem.dataStructures.jobs.InputFileJob;
import filesystem.dataStructures.jobs.OutputFileJob;
import filesystem.exceptions.AlreadyExistsException;
import filesystem.exceptions.FileNotFoundException;
import filesystem.exceptions.FileSystemException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import logging.Logger;
import logging.LoggingCategory;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class ArchiveFileSystem implements FileSystem {

	String pathToFileSystem;
	TPath archive;

	public static String getFileSystemExtension() {
		return "fsm";
	}

	public ArchiveFileSystem(String pathToFileSystem) {


		TConfig config = TConfig.get();
		config.setArchiveDetector(new TArchiveDetector(
				getFileSystemExtension(), // file system file extension
				//new FileDriver()));
				new JarDriver(IOPoolLocator.SINGLETON)));

		TConfig.push();

		archive = new TPath(pathToFileSystem);

		TFile archiveFile = archive.toFile();

		if (!archiveFile.exists() && !archiveFile.mkdirs()) // if archive doesnt exist and cannot create empty one
		{
			System.err.println("mkdir failed");
		}


		if (!archiveFile.isArchive() || !archiveFile.isDirectory()) {
			System.err.println("file: " + pathToFileSystem + " is not compatible archive ");
		}
		try {
			this.pathToFileSystem = archiveFile.getCanonicalPath();
		} catch (IOException ex) {
			Logger.log(Logger.ERROR, LoggingCategory.FILE_SYSTEM, "Fatal error, cannot resolve canonical path to filesystem archive");
		}
	}

	@Override
	public boolean rm_r(String path) throws FileNotFoundException {
		TFile file = getRelativeTFile(path);

		if (file == null) {
			return false;
		}

		if (!file.exists()) {
			throw new FileNotFoundException();
		}

		try {
			file.rm_r();
		} catch (IOException ex) {
			return false;
		}

		return true;
	}

	@Override
	public boolean isFile(String path) {
		TFile file = getRelativeTFile(path);

		return file.isFile();
	}

	@Override
	public boolean isDir(String path) {


		TFile file = getRelativeTFile(path);

		if (file == null) {
			return false;
		}

		if (!file.exists()) {
			return false;
		}

		return file.isDirectory();
	}

	@Override
	public boolean exists(String path) {

		TFile file = getRelativeTFile(path);

		if (file == null) {
			return false;
		}

		return file.exists();
	}

	@Override
	public void umount() {
		try {
			TVFS.umount(archive.toFile());
		} catch (FsSyncException ex) {
			System.err.println("FsSyncException occured when umounting filesystem");
			Logger.log(Logger.WARNING, LoggingCategory.FILE_SYSTEM, "FsSyncException occured when umounting filesystem");
		}
	}

	@Override
	public NodesWrapper listDir(String path) throws FileNotFoundException {

		TFile dir = getRelativeTFile(path);

		if (dir.isFile()) {
			List<Node> singleFile = new LinkedList<>();
			File file = new File();
			file.setName(dir.getName());
			singleFile.add(file);
			return new NodesWrapper(singleFile);
		}

		if (!dir.isDirectory()) // path is not directory
		{
			throw new FileNotFoundException();
		}


		TFile[] files = dir.listFiles();
		LinkedList<Node> ret = new LinkedList<>();


		for (int i = 0; i < files.length; i++) {
			TFile tFile = files[i];

			if (tFile.isDirectory()) {
				Directory add = new Directory();
				add.setName(tFile.getName());
				ret.add(add);
			} else {
				File add = new File();
				add.setName(tFile.getName());
				ret.add(add);
			}
		}

		return new NodesWrapper(ret);


	}

	@Override
	public int runInputFileJob(String path, InputFileJob job) throws FileNotFoundException {

		while (Thread.interrupted()) {  // clear threat interrupted status
		}

		InputStream input = null;

		try {

			TFile file = getRelativeTFile(path);

			if (!file.exists()) {
				return -1;
			}

			TPath pat = new TPath(file);

			input = Files.newInputStream(pat);
			job.workOnFile(input);
			return 0;
		} catch (FsEntryNotFoundException ex) {
			throw new FileNotFoundException();
		} catch (Exception ex) {
			Logger.log(Logger.WARNING, LoggingCategory.FILE_SYSTEM, "Exception occured when running inputFileJob: " + ex.toString());
		} finally {

			try {
				input.close();
			} catch (Exception ex) {
			}
		}

		return -1;

	}

	@Override
	public int runOutputFileJob(String path, OutputFileJob job) {

		while (Thread.interrupted()) {  // clear threat interrupted status
		}

		OutputStream output = null;

		try {
			TFile file = getRelativeTFile(path);

//			if (!file.exists()) {
//				return -1;
//			}

			TPath pat = new TPath(file);

			output = Files.newOutputStream(pat);
			job.workOnFile(output);
			return 0;
		} catch (Exception ex) {
			Logger.log(Logger.WARNING, LoggingCategory.FILE_SYSTEM, "Exception occured when running outputFileJob: " + ex.toString());
		} finally {

			try {
				output.close();
			} catch (Exception ex) {
			}
		}

		return -1;
	}

	/**
	 * create new file
	 *
	 * @param path string like /home/user/xyz creates xyz file
	 * @return
	 * @throws FileNotFoundException
	 */
	@Override
	public boolean createNewFile(String path) throws FileNotFoundException {

		if (path.endsWith("/")) {
			return false;
		}
		try {

			TFile file = getRelativeTFile(path);

			if (file.getParentFile() == null || !file.getParentFile().isDirectory()) {
				throw new FileNotFoundException();
			}

			return file.createNewFile();
		} catch (IOException ex) {
			return false;
		}

	}

	/**
	 * create TFile object from path like /home/user/./../
	 *
	 * @param path
	 * @return
	 */
	private TFile getRelativeTFile(String path) {
		return new TFile(pathToFileSystem + normalize(path));
	}

	/**
	 * normalize path like /home/user/./../ to /home/
	 *
	 * @param path
	 * @return
	 */
	@Override
	public String normalize(String path) {

		TPath tpath = new TPath(this.pathToFileSystem);

		String normalized = tpath.getFileSystem().getPath(path).normalize().toString();


		StringBuilder sb = new StringBuilder(normalized);

		while (sb.toString().startsWith(".") || sb.toString().startsWith("/")) {

			while (sb.toString().startsWith(".")) {
				sb.delete(0, 1);
			}

			while (sb.toString().startsWith("/")) {
				sb.delete(0, 1);
			}

		}

		return "/" + sb.toString();
	}

	@Override
	public boolean createNewDir(String path) throws FileNotFoundException {

		if (path.endsWith("/")) {
			return false;
		}

		TFile file = getRelativeTFile(path);

		if (file.getParentFile() == null || !file.getParentFile().isDirectory()) {
			throw new FileNotFoundException();
		}

		return file.mkdirs();
	}

	@Override
	public boolean cp_r(String source, String target) throws FileSystemException {
		TFile sourceFile = getRelativeTFile(source);

		if (sourceFile == null) {
			return false;
		}

		if (!sourceFile.exists()) {
			throw new FileNotFoundException();
		}

		TFile targetFile = getRelativeTFile(target);
		
		if(targetFile==null)
			return false;
		
		if (targetFile.exists() && sourceFile.isDirectory()) {
			throw new AlreadyExistsException();
		}
		
		if (targetFile.exists() && sourceFile.isFile()) {
			
			if(!targetFile.isDirectory())  
				throw new AlreadyExistsException();
			
			targetFile = getRelativeTFile(target+ "/" + sourceFile.getName());
			System.out.println(targetFile.getAbsolutePath());
		}
		
		try {
			sourceFile.cp_r(targetFile);
		} catch (IOException ex) {
			
			throw new FileNotFoundException();
		}

		return true;
	
	}

	@Override
	public boolean mv(String source, String target) throws FileSystemException {
		TFile sourceFile = getRelativeTFile(source);

		if (sourceFile == null) {
			return false;
		}

		if (!sourceFile.exists()) {
			throw new FileNotFoundException();
		}

		TFile targetFile = getRelativeTFile(target);
		
		if(targetFile==null)
			return false;
		
		if (targetFile.exists() && sourceFile.isDirectory()) {
			throw new AlreadyExistsException();
		}
		
		if (targetFile.exists() && sourceFile.isFile()) {
			
			if(!targetFile.isDirectory())  
				throw new AlreadyExistsException();
			
			targetFile = getRelativeTFile(target+ "/" + sourceFile.getName());
			System.out.println(targetFile.getAbsolutePath());
		}
		
		
		try {

			
			sourceFile.mv(targetFile);
		} catch (IOException ex) {
			
			throw new FileNotFoundException();
		}

		return true;
	}
}
