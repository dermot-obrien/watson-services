package org.dobrien.watson.services.preprocessing;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;

import org.dobrien.watson.services.Context;
import org.dobrien.watson.services.collection.LocalCollection;
import org.dobrien.watson.services.document.DocumentService;

public class ExtractStructure {

	private static final Logger logger = Logger.getLogger(ExtractStructure.class.getName());
	
	public void extractStructure(File file) {
		PDDocument document = null;
		try {
			document = PDDocument.load(file);
            PDDocumentOutline outline =  document.getDocumentCatalog().getDocumentOutline();
            if( outline != null ) {
                printBookmark(document, outline, "");
            }
            else {
            	logger.warning(file.getName()+" does not contain any table of contents." );
            }
		}
		catch(Exception e) {
			logger.log(Level.SEVERE, "Unable to process "+file.getName(), e);
		}
		finally {
			if (document != null) {
				try { document.close(); } 
				catch(Exception e) { 
					logger.log(Level.SEVERE, "Unable to close document.", e);
				}
			}
		}
	}

    /**
     * This will print the documents bookmarks to System.out.
     *
     * @param document The document.
     * @param bookmark The bookmark to print out.
     * @param indentation A pretty printing parameter
     *
     * @throws IOException If there is an error getting the page count.
     */
    public void printBookmark(PDDocument document, PDOutlineNode bookmark, String indentation) throws IOException
    {
        PDOutlineItem current = bookmark.getFirstChild();
        while( current != null )
        {
        	int pageNo = -1;
            if (current.getDestination() instanceof PDPageDestination)
            {
                PDPageDestination pd = (PDPageDestination) current.getDestination();
                pageNo = (pd.retrievePageNumber() + 1);
            }
            if (current.getAction() instanceof PDActionGoTo)
            {
                PDActionGoTo gta = (PDActionGoTo) current.getAction();
                if (gta.getDestination() instanceof PDPageDestination)
                {
                    PDPageDestination pd = (PDPageDestination) gta.getDestination();
                    pageNo = (pd.retrievePageNumber() + 1);
                }
            }
            System.out.println( indentation + current.getTitle() +" ("+pageNo+")");
            printBookmark( document, current, indentation + "    " );
            current = current.getNextSibling();
        }
    }
    
	public void extractStructure(String folderPath) {
		File[] files = LocalCollection.getPDFFiles(folderPath);
		for (int i = 0; i < files.length; i++) {
			extractStructure(files[i]);
		}
	}
	
	public static void main(String[] args) {
		String folder = Configurations.get().getDocumentCollectionPath();
		new ExtractStructure().extractStructure(folder);
	}

}
