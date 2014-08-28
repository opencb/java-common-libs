package org.opencb.commons.bioformats.variant.annotators;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import org.opencb.commons.bioformats.variant.Variant;
import org.opencb.commons.bioformats.variant.effect.EffectCalculator;
import org.opencb.commons.bioformats.variant.utils.effect.VariantEffect;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

/**
 * @author Roberto Alonso Valero <ralonso@cipf.es>
 */
public class VariantGOAnnotator implements VariantAnnotator {


    private Client wsRestClient;
    private String wsServer;
    private WebTarget webResource;
    private Map<String,Set<String>> geneGOs;
    private int batchGeneSize;
    private String tag;

    public VariantGOAnnotator() {
        this("GOTerms");
    }

    public VariantGOAnnotator(String goTag) {
        wsRestClient = ClientBuilder.newClient();
        wsServer = "http://www.ebi.ac.uk/cellbase/webservices/rest/v3/hsapiens/";
        webResource = wsRestClient.target(wsServer);
        geneGOs = new HashMap<>();
        batchGeneSize = 900;
        tag = goTag;
    }

    @Override
    public void annot(List<Variant> batch) {

        EffectCalculator.setEffects(batch);
        /** get all gos by gene **/
        List<String> genes = new ArrayList<>();

        for (Variant record : batch) {
            for (VariantEffect ve : record.getEffect()) {
                String geneName = ve.getGeneName();
                if (geneName.equalsIgnoreCase("") || geneGOs.containsKey(geneName))
                    continue;
                geneGOs.put(geneName, new HashSet<String>());
                genes.add(geneName);
            }
        }

        for(int i = 0; i < genes.size(); i += batchGeneSize){
            int startIdx = i;
            int endIdx = i + batchGeneSize;

            if( endIdx > genes.size())
                endIdx = genes.size();
            System.out.println(startIdx + " - " + endIdx);
            String geneLine = Joiner.on(",").join(genes.subList(startIdx, endIdx));

            System.out.println(wsServer + "feature/id/" + geneLine + "/xref?dbname=GO");
            Response response = webResource.path("feature").path("id").path(geneLine).path("xref").queryParam("dbname", "GO").queryParam("of", "json").request().get();
            String resp = response.readEntity(String.class);
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode actualObj = mapper.readTree(resp);
                Iterator<JsonNode> it = actualObj.get("response").iterator();
                while (it.hasNext()) {
                    JsonNode go = it.next();
                    String geneName = go.get("id").asText();
                    Set<String> gos = new HashSet<>();
                    Iterator<JsonNode> itResults = go.get("result").iterator();

                    while (itResults.hasNext()) {
                        String goId = itResults.next().get("id").asText();
                        if (goId.startsWith("GO")) {

                            gos.add(goId);
                        }
                    }
                    geneGOs.put(geneName, gos);
                }
            }catch(JsonParseException e){
                System.err.println(resp);
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        for (Variant record : batch) {
            Set<String> goTerms = new HashSet<>();
            for (VariantEffect ve : record.getEffect()) {
                if(ve.getGeneName().equalsIgnoreCase(""))
                    continue;
                goTerms.addAll(geneGOs.get(ve.getGeneName()));
            }
            record.addAttribute(tag, Joiner.on(",").join(goTerms));
        }
    }

    @Override
    public void annot(Variant elem) {
    }


}
