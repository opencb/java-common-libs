package org.opencb.commons.bioformats.variant.annotators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import org.opencb.biodata.models.variant.Variant;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class VariantControlMongoAnnotator implements VariantAnnotator {


    @Override
    public void annot(List<Variant> batch) {

        Client clientNew = ClientBuilder.newClient();
        WebTarget webTarget = clientNew.target("http://ws-beta.bioinfo.cipf.es/controlsws/rest/");

        StringBuilder chunkVariants = new StringBuilder();
        for (Variant record : batch) {
            chunkVariants.append(record.getChromosome()).append(":");
            chunkVariants.append(record.getStart()).append(":");
            chunkVariants.append(record.getReference()).append(":");
            chunkVariants.append(record.getAlternate()).append(",");
        }

        Form form = new Form();
        form.param("positions", chunkVariants.substring(0, chunkVariants.length() - 1));

        Response response = webTarget.path("variants").request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        ObjectMapper mapperNew = new ObjectMapper();

        JsonNode actualObj;

        String resp = null;

        try {
            resp = response.readEntity(String.class);

            actualObj = mapperNew.readTree(resp);

            Iterator<JsonNode> it = actualObj.get("result").iterator();

            int i = 0;
            while (it.hasNext()) {
                JsonNode aa = it.next();
                System.out.println(aa);
                if (aa.has("studies")) {


                    Iterator<JsonNode> itStudies = aa.get("studies").iterator();

                    Variant v = batch.get(i);
                    v.addAttribute(aa.get("studies").get("studyId").asText() + "_maf", "" + aa.get("studies").get("stats").get("maf").asDouble());
                    v.addAttribute(aa.get("studies").get("studyId").asText() + "_amaf", "" + aa.get("studies").get("stats").get("alleleMaf").asText());

                    List<String> gts = new ArrayList<>();
                    Iterator<Map.Entry<String, JsonNode>> gtIt = aa.get("studies").get("stats").get("genotypeCount").fields();
                    while (gtIt.hasNext()) {
                        Map.Entry<String, JsonNode> elem = gtIt.next();
                        String aux = elem.getKey() + ":" + elem.getValue().asInt();
                        gts.add(aux);
                    }

                    v.addAttribute(aa.get("studies").get("studyId").asText() + "_gt", Joiner.on(",").join(gts));

                }

                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void annot(Variant elem) {

    }
}
