package org.dobrien.watson.services.conductors;

import org.dobrien.watson.services.analysis.RetrieveEnrichments;

public class ExtractEntityCountsFromEnrichments {

    public static void main(String[] args) {
        String inputFilename = RetrieveConductorTestsEnrichments.EnrichmentsFilename;
        org.dobrien.watson.services.analysis.ExtractEntityCountsFromEnrichments extractor = new org.dobrien.watson.services.analysis.ExtractEntityCountsFromEnrichments();
        String outputFolder = "data/reports/";
        extractor.extract(inputFilename,outputFolder);
    }
}
