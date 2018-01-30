package com.example.snippet.ogema.io;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**Examples for common I/O operations for which standard  methods are not known*/
public class DirUtilExamples {
	public static void makeSureDirExists(Path dir) {
		if(Files.notExists(dir)) {
			try {
				Files.createDirectories(dir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
	
	public static void createFile(String fileNameWithPath) {
		File file = new File(fileNameWithPath);
		file.getParentFile().mkdirs();
		//TODO: create the file based on your data
	}
	
	public static String appendPath(String orgDir, String addPath) {
		return (orgDir.endsWith("/")||orgDir.endsWith("\\"))?(orgDir+addPath):(orgDir+File.separator+addPath);
	}
	
	public static List<File> getAllFilesInDirectory(String dirName) {
		File folder = new File(dirName);
		File[] listOfFiles = folder.listFiles();
		List<File> result = new ArrayList<>();
		
	    for (int i = 0; i < listOfFiles.length; i++) {
	    	if (listOfFiles[i].isFile()) {
	    		result.add(listOfFiles[i]);
	    	}
	    }
	    return result;
	}
	
	public static void processAllFilesInDirectory(String dirName) {
		Paths.get(dirName).toFile().list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
				if(!new File(current, name).isDirectory()) {
					//TODO: process your file
				}
			    return new File(current, name).isDirectory();
			  }
		});
		
	}
}
