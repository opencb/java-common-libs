package org.opencb.commons.bioformats.variant.annotators;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.biodata.models.variant.Variant;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class VariantSNPAnnotator implements VariantAnnotator {

    private WebTarget webResource;

    public VariantSNPAnnotator() {
        webResource = ClientBuilder.newClient().target("http://ws-beta.bioinfo.cipf.es/cellbase/rest/v3/hsapiens/genomic/position");
    }

    @Override
    public void annot(List<Variant> batch) {
        StringBuilder positions = new StringBuilder();
        for (Variant record : batch) {
            positions.append(record.getChromosome()).append(":").append(record.getStart()).append(",");
        }

        Form form = new Form();
        form.param("position", positions.substring(0, positions.length() - 1));

        Response response = webResource.path("snp").queryParam("of", "json").queryParam("include", "id").request().post(
                Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj;

        String resp = null;
        try {
            resp = response.readEntity(String.class);
            actualObj = mapper.readTree(resp);
            Iterator<JsonNode> it = actualObj.get("response").iterator();

            int cont = 0;
            while (it.hasNext()) {
                JsonNode snp = it.next();
                if (snp.get("numResults").asInt() > 0) {
                    Iterator<JsonNode> itResults = snp.get("result").iterator();
                    
                    // TODO Accept multiple identifiers via xrefs
//                    while (itResults.hasNext()) {
                    if (itResults.hasNext()) {
                        String rs = itResults.next().get("id").asText();
                        if (rs.startsWith("rs")) {
//                            batch.get(cont).addId(rs);
                            batch.get(cont).setId(rs);
                        }
                    }
                }
                cont++;
            }

        } catch (JsonParseException e) {
            System.err.println(resp);
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void annot(Variant elem) {
        annot(Arrays.asList(elem));
    }

}
