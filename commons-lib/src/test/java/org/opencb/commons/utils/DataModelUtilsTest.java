package org.opencb.commons.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DataModelUtilsTest {


    @Test
    public void unformattedDataModelJsonString() throws Exception {
        String dataFieldDocJSON = DataModelsUtils.dataModelToJsonString(Class.forName("org.opencb.commons.utils.TestBeanClass"), false);
        System.out.println("JSON:\n" + dataFieldDocJSON);
        assertFalse(dataFieldDocJSON.contains("\\n"));
        assertTrue(isJSONValid(dataFieldDocJSON));
    }

    public boolean isJSONValid(String test) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.readValue(test, Map.class);
        } catch (JsonProcessingException e) {
            return false;

        }
        return true;
    }
}
