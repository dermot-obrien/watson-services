package org.dobrien.watson.services.training;

import org.dobrien.watson.services.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class is responsible for containing a training sample, and sending it's training sample
 * to the API
 */
public class SampleUploader {

    private String docID;
    private String crossRef;
    private String relevance;
    private String queryID;

        /**
         * take in a training sample and prepare a curl command, in the form of a string, to send a training sample to the api
         * with the specified relevancy
         *
         * @return
         * @throws IOException
         */
        public String readJSONCommand () throws IOException {

        byte[] fileBytes = Files.readAllBytes(Paths.get("resources/trainingSampleJSON.txt"));
        String allData = new String(fileBytes);

        String userName     = Configurations.get().getUsername();
        String password     = Configurations.get().getPassword();
        String colID        = Configurations.get().getMainCollection().getCollectionId();
        String envID        = Configurations.get().getEnvironmentId();
        String version      = Configurations.get().getVersion();

        String result = allData
                // The following are sourced from the .csv file being processed
                .replace("{document_id}",docID)
                .replace("{cross_reference}",crossRef)
                .replace("{relevance}",relevance)
                .replace("{query_id}",queryID)

                // The following are sourced from the context.json file
                .replace("{username}",userName)
                .replace("{password}",password)
                .replace("{document_id}",docID)
                .replace("{cross_reference}",crossRef)
                .replace("{relevance}",relevance)
                .replace("{environment_id}",envID)
                .replace("{collection_id}",colID)
                .replace("{query_id}",queryID)
                .replace("{version}",version);

        return result;

    }


    /**
     * upload a sample to the API and return the response
     * @return
     */
    public String uploadATrainingSample () {

        String response = "ERROR";

        if ((docID == null)||(relevance == null) || (queryID == null))
            return response;

        try {
            // prep a curl command with the actual question:
            String command = readJSONCommand();

            Runtime r = Runtime.getRuntime();
            Process p = r.exec(command);

            // read the response here:
            InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            response = br.readLine();

        } catch (Exception e) {e.printStackTrace();}

        return response;
    }

    // SETTERS:

    public void setDocID (String docID) {
        this.docID = docID;
    }

    public void setRelevance (String relevance) {
        this.relevance = relevance;
    }

    public void setQueryID (String queryID) {
        this.queryID = queryID;
    }

    public void setCrossRef (String crossRef) {
        this.crossRef = crossRef;
    }
}
