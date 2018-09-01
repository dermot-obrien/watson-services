package org.dobrien.watson.services.training;

import org.dobrien.watson.services.Context;
import org.dobrien.watson.services.document.DocumentService;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for reading a list of training queries from a .csv file, it will use the QueryUploader
 * to send the queries to the API and obtain the response. It will then write the response data to another .csbv file
 */
public class UploadAllQueriesAction {

    private QueryUploader uploader = new QueryUploader();

    private static final Logger logger = Logger.getLogger(DocumentService.class.getName());


    /**
     * upload all queries and write response to csv file
     * @return true if there were no errors uploading the docs
     */
    public boolean readAndUploadData () {

        String queriesCSVFilePath = Configurations.get().getPathToTrainingQueries();
        String queryOutPutCSVPath = Configurations.get().getPathToTrainingQueriesUIDFile();

        try {
            // if we upload queries that already exist, then the output file should not be erased
            PrintWriter pw = new PrintWriter(new FileWriter(queryOutPutCSVPath,true));

            byte[] fileBytes = Files.readAllBytes(Paths.get(queriesCSVFilePath));
            String allQueryData = new String(fileBytes);

            String [] queries = allQueryData.split("\n");
            for (String query: queries) {
                String actualQuery = query.split(",")[1].replaceAll("\r",""); // Because the file will have a number, followed by the actual query
                this.uploader.setTrainingQuery(actualQuery);
                String response = this.uploader.uploadATrainingQuery();

                // process the response:
                JSONObject jsonReponse = new JSONObject(response);
                // handle the case where a query already exists, print something useful etc
                if (jsonReponse.has("error")) {
                    if (jsonReponse.getString("error").contains("ALREADY_EXISTS")) {
                        logger.log(Level.INFO,"Found a query which already exists, moving on");
                        continue;
                    }
                }

                logger.log(Level.INFO,"Uploaded Training Query: " + actualQuery);
                String queryID = jsonReponse.getString("query_id");
                pw.println(actualQuery + "," + queryID);
            }

            pw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


}
