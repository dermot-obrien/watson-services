package org.dobrien.watson.services.cropping;
import java.io.*;
import java.util.*;

import org.dobrien.watson.services.Context;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PDFSplitter {

    // where do we read the pdf documents from ?
    private String pathToSourceDirectoery;

    // where do we write the split documents to ?
    private String pathToCollectionDirectory;

    private int splitLimit;

    public PDFSplitter () {
        Configurations context = Configurations.get();
        this.pathToSourceDirectoery = context.getDocumentSourcePath();
        this.pathToCollectionDirectory = context.getDocumentCollectionPath();
        this.splitLimit = context.getPdfSplitLimit();
    }

    /**
     * read all pdf files from the config managers directory and split them in half, writting the resulting split docs to the out put directory:
     * @throws IOException
     */
    public void readAndSplit () throws IOException {


        File readFrom = new File(pathToSourceDirectoery);

        for (File currentPDFFile: readFrom.listFiles()) {

            String fileName = currentPDFFile.getName();
            String [] fileData = fileName.split("\\.");
            String ext = fileData[fileData.length - 1];
            if (!ext.equals("pdf"))
                continue;

            PDDocument document = PDDocument.load(currentPDFFile);

            if (document.isEncrypted()) // TO FIX A CRASH
                document.setAllSecurityToBeRemoved(true);

            double fileSize = currentPDFFile.length()*0.001;
            cropPDFDocument(document,currentPDFFile.getName(),1, fileSize);
        }

        System.out.println("Finished splitting and writing!");
    }

    /**
     * This will replace splitPDFDocument later
     *
     * take in a file and split it into different files such that each file is below the specified size
     */
    public void cropPDFDocument (PDDocument document, String fileName, int splitNum, double pdfFileSize) {

        Splitter pdfSplitter = new Splitter();

        try {
            int cropLimit = this.splitLimit;

            // the number of pages can never be less than one, or we get an exception
            if (cropLimit < 1) {
                cropLimit = 1;
            }

            try {
                pdfSplitter.setSplitAtPage(cropLimit);
            }catch (Exception e) {e.printStackTrace(); System.out.println(fileName);}

            List<PDDocument> docs = pdfSplitter.split(document);

            for (PDDocument splitDoc : docs) {
                splitDoc.save(this.pathToCollectionDirectory + "/" + fileName.replaceAll(".pdf","") + "-" + splitNum + ".pdf");
                splitDoc.close();
                splitNum++;
            }

        }

        catch (IOException e) {
            System.out.println("file reading or parsing error");
            e.printStackTrace();
        }
    }


    /**
     * Debug Code
     * @param doc
     */
    public void printDocContent (PDDocument doc) {
        // FOR DEBUG:
        try{
            PDFTextStripper strip = new PDFTextStripper();
            System.out.println("===================");
            System.out.println(strip.getText(doc));
            System.out.println("===================");
        }catch(Exception e) {e.printStackTrace();}
    }

}
