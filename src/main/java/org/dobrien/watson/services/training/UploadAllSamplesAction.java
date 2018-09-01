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
public class UploadAllSamplesAction {

    private SampleUploader uploader = new SampleUploader();

    private static final Logger logger = Logger.getLogger(DocumentService.class.getName());


    /**
     * upload all samples and write response to csv file
     * @return true if there were no errors uploading the docs
     */
    public boolean readAndUploadData () {

        String samplesCSVFilePath = Configurations.get().getPathToTrainingSamples();
        String sampleOutPutCSVPath = Configurations.get().getPathToTrainingSamplesUIDFile();

        try {
            // if we upload samples that already exist, then the output file should not be erased
            PrintWriter pw = new PrintWriter(new FileWriter(sampleOutPutCSVPath,true));

            byte[] fileBytes = Files.readAllBytes(Paths.get(samplesCSVFilePath));
            String allSampleData = new String(fileBytes);

            String [] samples = allSampleData.split("\n");
            for (String sample: samples) {

                String [] sampleData = sample.split(",");
                //
                String docUID = sampleData[0];
                String relevance = sampleData[1];
                String crosRef = sampleData[2];
                String queryID = sampleData[3].replaceAll("\r","");


                this.uploader.setDocID(docUID);
                this.uploader.setRelevance(relevance);
                this.uploader.setQueryID(queryID);
                this.uploader.setCrossRef(crosRef);

                String response = this.uploader.uploadATrainingSample();

                // process the response:
                JSONObject jsonReponse = new JSONObject(response);
                // handle the case where a sample already exists, print something useful etc
                if (jsonReponse.has("error")) {
                    if (jsonReponse.getString("error").contains("ALREADY_EXISTS")) {
                        logger.log(Level.INFO,"Found a sample which already exists, moving on");
                        continue;
                    }

                    if (jsonReponse.getString("error").contains("NOT_FOUND")) {
                        logger.log(Level.INFO,jsonReponse +" moving on");
                        continue;
                    }
                }


                logger.log(Level.INFO,"Uploaded Training Sample: " + docUID);
                pw.println(docUID + "," + crosRef + "," + relevance);
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
