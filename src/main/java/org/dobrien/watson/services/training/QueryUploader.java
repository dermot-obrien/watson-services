package org.dobrien.watson.services.training;

import org.dobrien.watson.services.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class is responsible for containing a training query, and sending it's training query (usually a question)
 * to the API
 */
public class QueryUploader {

    private String trainingQuery;

    /**
     * take in a question and prepare a curl command, in the form of a string, to send a training query to the api
     * with the specified question
     *
     * @param question
     * @return
     * @throws IOException
     */
    public String readJSONCommand (String question) throws IOException {

        byte[] fileBytes = Files.readAllBytes(Paths.get("resources/trainingQueryJSON.txt"));
        String allData = new String(fileBytes);

        String userName = Configurations.get().getUsername();
        String password = Configurations.get().getPassword();
        String colID = Configurations.get().getMainCollection().getCollectionId();
        String envID = Configurations.get().getEnvironmentId();
        String version = Configurations.get().getVersion();

        String result = allData
                // The following are sourced from the .csv file being processed
                .replace("{question}", question)

                // The following are sourced from the context.json file
                .replace("{username}",userName)
                .replace("{password}",password)
//                .replace("{filter}","")
                .replace("{environment_id}",envID)
                .replace("{collection_id}",colID)
                .replace("{version}",version);
        return result;
    }


    /**
     * upload a query to the API and return the response
     * @return
     */
    public String uploadATrainingQuery () {

        String response = "ERROR";

        if (trainingQuery == null)
            return response;

        try {
            // prep a curl command with the actual question:
            String command = readJSONCommand(this.trainingQuery);
            Runtime r = Runtime.getRuntime();
            Process p = r.exec(command);

            // read the response here:
            InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            response = br.readLine();

        } catch (Exception e) {e.printStackTrace();}

        return response;
    }

    public void setTrainingQuery (String trainingQuery) {
        if (trainingQuery == null)
            return;
        String allSpecialCharsRemoved = trainingQuery.replaceAll("(?=[]\\[+&|!(){}^\"~*?:\\\\-])", "").replaceAll("\"","");
        if (allSpecialCharsRemoved.isEmpty())
            this.trainingQuery = trainingQuery.replaceAll("(?=[]\\[+&|!(){}^\"~*?:\\\\-])", "\\\\\\\\\\\\");
        else
            this.trainingQuery = trainingQuery;
    }
}
