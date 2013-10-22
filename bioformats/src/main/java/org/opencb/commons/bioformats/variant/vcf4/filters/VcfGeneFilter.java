package org.opencb.commons.bioformats.variant.vcf4.filters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.commons.bioformats.feature.Region;
import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 10/9/13
 * Time: 5:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfGeneFilter extends VcfFilter {
    private List<Region> regionList;


    public VcfGeneFilter(String filename) {
        regionList = new ArrayList<>(1000);
        populateRegionList(filename);
    }

    public VcfGeneFilter(String filename, int priority) {
        super(priority);
        regionList = new ArrayList<>(1000);
        populateRegionList(filename);
    }

    private void populateRegionList(String filename) {

        BufferedReader br = null;
        List<String> genes = new ArrayList<>(1000);
        Client wsRestClient = ClientBuilder.newClient();
        WebTarget webResource = wsRestClient.target("http://ws.bioinfo.cipf.es/cellbase/rest/latest/hsa/feature/gene/");


        String line;
        try {
            br = new BufferedReader(new FileReader(filename));
            while ((line = br.readLine()) != null) {
                if (!line.equals("")) {
                    genes.add(line);

                }

            }

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        ObjectMapper mapper = new ObjectMapper();

        Iterator<String> it = genes.iterator();
        while (it.hasNext()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 100 && it.hasNext(); i++) {
                sb.append(it.next()).append(",");
            }
            Response response = webResource.path(sb.toString()).path("info").queryParam("of", "json").request("text/plain").get();

            JsonNode actualObj = null;
            try {
                actualObj = mapper.readTree(response.toString());
                Iterator<JsonNode> itAux = actualObj.iterator();
                Iterator<JsonNode> aux;

                while (itAux.hasNext()) {
                    JsonNode node = itAux.next();
                    if (node.isArray()) {

                        aux = node.iterator();
                        while (aux.hasNext()) {
                            JsonNode auxNode = aux.next();

                            String chr = auxNode.get("chromosome").asText();
                            long start = auxNode.get("start").asLong();
                            long end = auxNode.get("end").asLong();

                            Region r = new Region(chr, start, end);
                            regionList.add(r);

                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    @Override
    public boolean apply(VcfRecord vcfRecord) {

        for (Region r : regionList) {
            if (r.contains(vcfRecord.getChromosome(), vcfRecord.getPosition())) {
                return true;
            }
        }

        return false;
    }
}
