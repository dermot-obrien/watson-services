package org.dobrien.watson.services.preprocessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dobrien.watson.services.document.DocumentSet;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.simple.JSONObject;

import org.dobrien.watson.services.Context;
import org.dobrien.watson.services.model.Collection;
import org.dobrien.watson.services.model.Document;

public class CreateJSONFromPDF {
	
	private static Logger logger = Logger.getLogger(CreateJSONFromPDF.class.getName());
	
	public void create(String pdfFilename,String jsonFilename) {
		try {
			// Load PDF.
			File pdfFile = new File(pdfFilename);
	        PDDocument document = PDDocument.load(pdfFile);
	        
	        // Get metadata.
	        PDDocumentInformation info = document.getDocumentInformation();
	        PDDocumentCatalog cat = document.getDocumentCatalog();
	        PDMetadata metadata = cat.getMetadata();
		
		    // Instantiate PDFTextStripper class
		    PDFTextStripper pdfStripper = new PDFTextStripper();
		    String text = pdfStripper.getText(document);
		
		    // Retrieving text from PDF document
			JSONObject initiativeJSON = new JSONObject();
			
			String title = info.getTitle();
			if (title != null && title.trim().length() > 0) initiativeJSON.put("title",title);

			String subject = info.getSubject();
			if (subject != null && subject.trim().length() > 0) initiativeJSON.put("subject",subject);

			String author = info.getAuthor();
			if (author != null && author.trim().length() > 0) initiativeJSON.put("author",author);

			String keywords = info.getKeywords();
			if (keywords != null && keywords.trim().length() > 0) {
				keywords = keywords.trim();
				if (keywords.charAt(0) == '"') {
					keywords = keywords.substring(1);
				}
				if (keywords.charAt(keywords.length()-1) == '"') {
					keywords = keywords.substring(0,keywords.length()-1);
				}
				if (keywords.endsWith(",")) {
					keywords = keywords.substring(0,keywords.length()-1);
				}
				keywords = keywords.trim();
				initiativeJSON.put("keywords",keywords);
			}
			
			// Add metadata.
			JSONObject extractedMetadata = new JSONObject();
			String filename = pdfFile.getName();
			if (filename.toLowerCase().endsWith(".pdf")) {
				filename = filename.substring(0, filename.length()-4);
			}
			extractedMetadata.put("filename", filename);
			initiativeJSON.put("extracted_metadata",extractedMetadata);
			
			// Add text.
			text = text.replaceAll("\\r\\n", "\n");
			text = text.replaceAll("\\n", "");
			text = text.replaceAll("\\.\\.\\.\\.", "");
			text = text.replaceAll("\\.\\.\\.", "");
			text = text.replaceAll("\\.\\.", "");
			
			text = text.replaceAll("\u2019","'");
			text = text.replaceAll("\u2018","'");
			text = text.replaceAll("\u201c","\"");
			text = text.replaceAll("\u201d","\"");
			text = text.replaceAll("\u2013","-");
			text = text.replaceAll("\u2014","-");
			text = text.replaceAll("\u2212","-");
			text = text.replaceAll("\u2022","*");
			
			initiativeJSON.put("text", text);
	
	        // Write JSON.
			FileWriterWithEncoding writer = new FileWriterWithEncoding(jsonFilename,"UTF-8");
	        writer.write(initiativeJSON.toJSONString());
			writer.close();
		
		    // Closing the document
		    document.close();	
		}
		catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to convert.", e);
		}
	}

	/**
	 * Convert all PDF documents in a specified folder to json.
	 *
	 * @param documentSet The document set.
	 */
	public void convertDocumentsinDocumentSet(DocumentSet documentSet, String outputFolderPath) {
		convertDocumentsinFolder(documentSet.getPDFDocuments(), outputFolderPath, Integer.MAX_VALUE);
	}


	/**
	 * Convert all PDF documents in a specified folder to json. 
	 * 
	 * @param folderPath The folder path.
	 */
	public void convertDocumentsinFolder(String folderPath, String outputFildrPath) {
		convertDocumentsinFolder(folderPath, outputFildrPath, Integer.MAX_VALUE);
	}

	/**
	 * Convert all PDF documents in a specified folder to json. 
	 * 
	 * @param pdfFolderPath The folder path.
	 */
	public void convertDocumentsinFolder(String pdfFolderPath, String jsonFolderPath,int maxCount) {
		File folder = new File(pdfFolderPath);
		File[] pdfFiles = folder.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".pdf");
			}
		});
		convertDocumentsinFolder(Arrays.asList(pdfFiles), jsonFolderPath,maxCount);
	}

	/**
	 * Convert all PDF documents in a specified folder to json.
	 *
	 * @param pdfFiles The folder path.
	 */
	public void convertDocumentsinFolder(List<File> pdfFiles, String jsonFolderPath, int maxCount) {
		try {
			int addedCount = 0;
			for (int i = 0; pdfFiles != null && i < pdfFiles.size() && addedCount < maxCount; i++) {
				String pdfFilename = pdfFiles.get(i).getAbsolutePath();
				if (!jsonFolderPath.endsWith("/") && !jsonFolderPath.endsWith("\\")) {
					jsonFolderPath = jsonFolderPath + File.separatorChar;
				}
				String jsonFilename = jsonFolderPath+pdfFiles.get(i).getName();
				if (jsonFilename.toLowerCase().endsWith(".pdf")) jsonFilename = jsonFilename.substring(0,jsonFilename.length()-4);
				jsonFilename = jsonFilename +".json";
				create(pdfFilename,jsonFilename);
				addedCount++;
			}
		}
		catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to convert document set.", e);
		}
	}
}