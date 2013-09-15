package org.opencb.variant.lib.io.variant.annotators;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.opencb.variant.lib.core.formats.VcfRecord;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
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
    private WebResource webResource;

    public VcfSNPAnnotator() {
        wsRestClient = Client.create();
        webResource = wsRestClient.resource("http://ws.bioinfo.cipf.es/cellbase/rest/latest/hsa/genomic/position/");

    }

    @Override
    public void annot(List<VcfRecord> batch) {

        List<SnpJson> snpList;
        StringBuilder positions = new StringBuilder();
        for (int i = 0; i < batch.size(); i++) {
            positions.append(batch.get(i).getChromosome()).append(":").append(batch.get(i).getPosition()).append(",");

        }
        System.out.println(positions.toString());
        String response = webResource.path(positions.toString().substring(0, positions.toString().length() - 1)).path("snp").queryParam("of", "json").get(String.class);

        ObjectMapper mapper = new ObjectMapper();
        try {
            snpList = mapper.readValue(response, new TypeReference<List<SnpJson>>() {
            });
            System.out.println(snpList);
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println(response);


//        ObjectMapper mapper = new ObjectMapper();


    }

    @Override
    public void annot(VcfRecord elem) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


     class SnpJson {
        @JsonProperty
        private int snpId;
        @JsonProperty
        private String name;
        @JsonProperty
        private String chromosome;
        @JsonProperty
        private int start;
        @JsonProperty
        private int end;
        @JsonProperty
        private String strand;
        @JsonProperty
        private int mapWeight;
        @JsonProperty
        private String alleleString;
        @JsonProperty
        private String ancestralAllele;
        @JsonProperty
        private String source;
        @JsonProperty
        private String displaySoConsequence;
        @JsonProperty
        private String soConsequenceType;
        @JsonProperty
        private String displayConsequence;
        @JsonProperty
        private String sequence;

        private SnpJson(int snpId, String name, String chromosome, int start, int end, String strand, int mapWeight, String alleleString, String ancestralAllele, String source, String displaySoConsequence, String soConsequenceType, String displayConsequence, String sequence) {
            this.snpId = snpId;
            this.name = name;
            this.chromosome = chromosome;
            this.start = start;
            this.end = end;
            this.strand = strand;
            this.mapWeight = mapWeight;
            this.alleleString = alleleString;
            this.ancestralAllele = ancestralAllele;
            this.source = source;
            this.displaySoConsequence = displaySoConsequence;
            this.soConsequenceType = soConsequenceType;
            this.displayConsequence = displayConsequence;
            this.sequence = sequence;
        }
    }
}
