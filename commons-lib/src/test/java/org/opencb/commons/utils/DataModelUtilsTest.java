package org.opencb.commons.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class DataModelUtilsTest {


    @Test
    public void unformattedDataModelJsonString() throws Exception {
        String dataFieldDocJSON = DataModelsUtils.dataModelToJsonString(Class.forName("org.opencb.commons.utils.TestBeanClass"), false);
        System.out.println("JSON:\n" + dataFieldDocJSON);
        Assert.assertFalse(dataFieldDocJSON.contains("\\n"));
        Assert.assertTrue(isJSONValid(dataFieldDocJSON));
    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
}
