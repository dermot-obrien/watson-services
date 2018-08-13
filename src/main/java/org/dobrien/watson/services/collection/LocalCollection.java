package org.dobrien.watson.services.collection;

import java.io.File;
import java.io.FilenameFilter;

public class LocalCollection {
	
	public static File[] getPDFFiles(String folderPath) {
		File folder = new File(folderPath);
		File[] pdfFiles = folder.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".pdf");
			}
		});
		return pdfFiles;
	}

}
