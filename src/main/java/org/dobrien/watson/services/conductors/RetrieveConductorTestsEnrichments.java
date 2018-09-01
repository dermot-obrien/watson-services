package org.dobrien.watson.services.conductors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.dobrien.watson.services.Context;
import org.dobrien.watson.services.analysis.RetrieveEnrichments;
import org.dobrien.watson.services.document.DocumentService;
import org.dobrien.watson.services.model.Collection;

import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RetrieveConductorTestsEnrichments {

    private static Logger logger = Logger.getLogger(RetrieveConductorTestsEnrichments.class.getName());

    static final String EnrichmentsFilename = "data/reports/ConductorTestsEnrichments.json";

    public void retrieve(Configurations context, String outputFilename) {
        retrieve(context, outputFilename, Integer.MAX_VALUE);
    }

    public void retrieve(Configurations context, String jsonFilename, int documentCount) {
        try {
            DocumentService documentsService = new DocumentService(context);
            Collection collection = context.getCollection();
            List<String> returnFields = Arrays.asList("enriched_text,metadata,extracted_metadata,id");
            int startIndex = 0;
            int pageSize = Math.min(500,documentCount);

            // Retrieve.
            JsonObject aggregatedResults = new JsonObject();
            JsonArray resultArray = new JsonArray();
            aggregatedResults.add("results", resultArray);

            JsonObject result = documentsService.getDocumentDetailsJSON(collection,returnFields,startIndex,pageSize);
            int remaining = result.get("matching_results").getAsInt();
            while (remaining > 0) {
                JsonArray results = result.get("results").getAsJsonArray();
                resultArray.addAll(results);
                int retrieved = results.size();
                remaining -= retrieved;
                startIndex += results.size();
                if (remaining > 0) result = documentsService.getDocumentDetailsJSON(collection,returnFields,startIndex,pageSize);
            }

            // Write.
            FileWriter writer = new FileWriter(jsonFilename);
            writer.write(aggregatedResults.toString());
            writer.close();
        }
        catch(Exception e) {
            logger.log(Level.SEVERE,"Unable to retrieve.",e);
        }
    }

    public static void main(String[] args) {
        Configurations context = Configurations.get("conductor-tests");
        String outputFilename = EnrichmentsFilename;
        new RetrieveEnrichments().retrieve(context, outputFilename);
    }

}
