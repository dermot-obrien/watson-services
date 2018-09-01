package org.dobrien.watson.services.document;

import org.dobrien.watson.services.model.Collection;
import org.dobrien.watson.services.model.Document;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentSet {

    private static final Logger logger = Logger.getLogger(DocumentSet.class.getName());

    private String[] rootFolderPaths;

    public DocumentSet(String... rootFolderPaths) {
        this.rootFolderPaths = rootFolderPaths;
    }

    /**
     * Get paths of all PDF documents in the root folder and subfolders.
     *
     */
    public List<File> getPDFDocuments() {
        List<File> paths = new ArrayList<File>();
        for (int i = 0; i < rootFolderPaths.length; i++) {
            getPDFDocuments(paths,new File(rootFolderPaths[i]));
        }
        return paths;
    }

    /**
     * Add all PDF documents in a specified folder to the collection.
     *
     * @param paths The folder paths.
     */
    private void getPDFDocuments(List<File> paths, File folder) {
        try {
            File[] pdfFiles = folder.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".pdf");
                }
            });
            paths.addAll(Arrays.asList(pdfFiles));

            for (File file : folder.listFiles()) {
                if (file.isDirectory()) {
                    getPDFDocuments(paths,file);
                }
            }
        }
        catch(Exception e) {
            logger.log(Level.SEVERE, "Unable to get PDF documents.", e);
        }
    }

}
