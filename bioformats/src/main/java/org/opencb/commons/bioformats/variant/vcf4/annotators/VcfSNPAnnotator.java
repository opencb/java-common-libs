package org.opencb.commons.bioformats.variant.vcf4.annotators;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 9/13/13
 * Time: 11:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfSNPAnnotator implements VcfAnnotator {

    private Client wsRestClient;
    private WebTarget webResource;

    public VcfSNPAnnotator() {
        wsRestClient = ClientBuilder.newClient();
        webResource = wsRestClient.target("http://ws-beta.bioinfo.cipf.es/cellbase/rest/v3/hsapiens/genomic/position");
    }

    @Override
    public void annot(List<VcfRecord> batch) {

        StringBuilder positions = new StringBuilder();
        for (VcfRecord record : batch) {
            positions.append(record.getChromosome()).append(":").append(record.getPosition()).append(",");
        }

        Form form = new Form();
        form.param("position", positions.substring(0, positions.length() - 1));

        Response response = webResource.path("snp").queryParam("of", "json").queryParam("include", "id").request().post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

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
                    while (itResults.hasNext()) {
                        batch.get(cont).addSnp(itResults.next().get("id").asText());
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
    public void annot(VcfRecord elem) {
    }


}
