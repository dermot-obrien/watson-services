package org.dobrien.watson.services.conductors;

import org.dobrien.watson.services.document.DocumentSet;
import org.dobrien.watson.services.preprocessing.CreateJSONFromPDF;

import java.util.logging.Logger;

public class ConvertConductorTestReports {

    private static final Logger logger = Logger.getLogger(ConvertConductorTestReports.class.getName());

    static String folder = "C:\\Users\\obriende\\OneDrive - Transpower\\Work\\05 Delivery\\Cognitive Search\\Document Sets\\Conductor sample test reports - for ML";
    static String outputFolder = "C:\\Users\\obriende\\OneDrive - Transpower\\Work\\05 Delivery\\Cognitive Search\\Document Sets\\Conductor Test Reports";

    public static void main(String[] args) {
        DocumentSet documentSet = new DocumentSet(folder);
        new CreateJSONFromPDF().convertDocumentsinDocumentSet(documentSet, outputFolder);
    }
}
