package org.dobrien.watson.services.training;

import org.dobrien.watson.services.training.QueryUploader;
import org.dobrien.watson.services.training.UploadAllQueriesAction;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

public class TrainingUploadingTests {
    // TODO write some tests

    private UploadAllQueriesAction allQueriesAction = new UploadAllQueriesAction();

    private QueryUploader uploader = new QueryUploader();


    /**
     * Reserved chars may end up being the only query to upload, we should test that the code doesn't break, but sends them up:
     */

    public void testImproperQueryString (String invalid) {
        String query = invalid;
        uploader.setTrainingQuery(query);
         String response = uploader.uploadATrainingQuery();
        JSONObject jsonReponse = new JSONObject(response);

        assert(response != "ERROR");
        boolean wasSentUpButAlreadyExists = jsonReponse.has("error") && jsonReponse.getString("error").contains("ALREADY_EXISTS");
        boolean wasSentUp = jsonReponse.has("query_id");
        assert(wasSentUpButAlreadyExists || wasSentUp);
    }

    @Test
    public void testNullQuery_01() {
        uploader.setTrainingQuery(null);
        String response = uploader.uploadATrainingQuery();
        assert(response == "ERROR");
    }

    @Test
    public void testNullQuery_02() {
        //whoops forgot to set question
        String response = uploader.uploadATrainingQuery();
        assert(response == "ERROR");
    }

    @Test
    public void testReservedCharQuery_01() {
        testImproperQueryString("'");
    }

    @Test
    public void testReservedCharQuery_02() {
        testImproperQueryString("\"hello\"");
    }

    @Test
    public void testReservedCharQuery_03() {
        testImproperQueryString("\"");
    }

    @Test
    public void testReservedCharQuery_04() {
        testImproperQueryString("test!!!");
    }

    @Test
    public void testReservedCharQuery_05() {
        testImproperQueryString("test!!!??");
    }

    @Test
    public void testReservedCharQuery_06() {
        testImproperQueryString("\"test\"!!!??");
    }


    @Test
    public void testReservedCharQuery_07() {
        testImproperQueryString("\"test\"!!\"!\"??");
    }

}
