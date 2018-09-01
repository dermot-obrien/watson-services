package org.dobrien.watson.services.preprocessing;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Ignore;
import org.junit.Test;

import org.dobrien.watson.services.Context;

public class PreprocessingTest {

	private File getFirst() {
		File documentCollectionFolder = new File(Configurations.get().getDocumentCollectionPath());
		File[] pdfFiles = documentCollectionFolder.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".pdf");
			}
		});
		return pdfFiles[0];
	}
	
	@Test
	@Ignore
	public void printMetadata() throws IOException {
        PDDocument document = PDDocument.load( getFirst());
        PrintDocumentMetaData meta = new PrintDocumentMetaData();
        meta.printMetadata( document );
        document.close();
	}
	
	@Test
	@Ignore
	public void printCIMiec61968Bookmarks() throws IOException {
		String filename = "src/test/resources/iec61968-2{ed2.0}en.pdf";
        PDDocument document = PDDocument.load( new File(filename));
        PrintBookmarks meta = new PrintBookmarks();
        PDDocumentOutline outline =  document.getDocumentCatalog().getDocumentOutline();
        if( outline != null )
        {
            meta.printBookmark(document, outline, "");
        }
        else
        {
            System.out.println( "This document does not contain any bookmarks" );
        }
        document.close();
    }
	
	@Test
	@Ignore
	public void printBookmarks() throws IOException {
        PDDocument document = PDDocument.load( getFirst());
        PrintBookmarks meta = new PrintBookmarks();
        PDDocumentOutline outline =  document.getDocumentCatalog().getDocumentOutline();
        if( outline != null )
        {
            meta.printBookmark(document, outline, "");
        }
        else
        {
            System.out.println( "This document does not contain any bookmarks" );
        }
        document.close();
    }
	
	@Test
	@Ignore
	public void readText() throws IOException {
        PDDocument document = PDDocument.load( getFirst());
	
	    // Instantiate PDFTextStripper class
	    PDFTextStripper pdfStripper = new PDFTextStripper();
	
	    // Retrieving text from PDF document
	    String text = pdfStripper.getText(document);
	    System.out.println(text);
	
	    // Closing the document
	    document.close();	
	}
	
	@Test
	@Ignore
	public void extractCIMiec61968Text() throws IOException {
		String filename = "src/test/resources/iec61968-2{ed2.0}en.pdf";
        PDDocument document = PDDocument.load( new File(filename));
	
	    // Instantiate PDFTextStripper class
	    PDFTextStripper pdfStripper = new PDFTextStripper();
	
	    // Retrieving text from PDF document
	    String text = pdfStripper.getText(document);
	    System.out.println(text);
	
	    // Closing the document
	    document.close();	
	}
	
	@Test
	@Ignore
	public void readPages() throws IOException {
        PDDocument document = PDDocument.load( getFirst());
        PDPageTree pageTree = document.getPages();
        for (PDPage pdPage : pageTree) {
 		}
 	    document.close();	
	}	
}
