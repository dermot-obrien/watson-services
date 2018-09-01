package org.dobrien.watson.services.conductors;

import org.dobrien.watson.services.Context;
import org.dobrien.watson.services.document.DocumentService;

import java.util.logging.Logger;

public class UploadConductorTestReports {

    private static final Logger logger = Logger.getLogger(ConvertConductorTestReports.class.getName());

    static String jsonFolder = ConvertConductorTestReports.outputFolder;

    public static void main(String[] args) {
        Configurations context = Configurations.get("conductor-tests");
        DocumentService documentsService = new DocumentService(context);
        documentsService.addGenericJSONDocumentsFromFolder(context.getCollection(), jsonFolder);
    }

}
