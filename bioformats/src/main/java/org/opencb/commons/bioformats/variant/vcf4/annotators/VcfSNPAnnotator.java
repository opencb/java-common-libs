package org.opencb.commons.bioformats.variant.vcf4.annotators;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
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
        webResource = wsRestClient.target("http://ws.bioinfo.cipf.es/cellbase/rest/latest/hsa/genomic/position/");

    }

    @Override
    public void annot(List<VcfRecord> batch) {

        List<SnpJson> snpList;
        StringBuilder positions = new StringBuilder();
        for (VcfRecord record : batch) {
            positions.append(record.getChromosome()).append(":").append(record.getPosition()).append(",");

        }
        System.out.println(positions.toString());
        Response response = webResource.path(positions.toString().substring(0, positions.toString().length() - 1)).path("snp").queryParam("of", "json").request("text/plain").get();

        ObjectMapper mapper = new ObjectMapper();
        try {
            snpList = mapper.readValue(response.toString(), new TypeReference<List<SnpJson>>() {
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
        private int snpId;
        private String name;
        private String chromosome;
        private int start;
        private int end;
        private String strand;
        private int mapWeight;
        private String alleleString;
        private String ancestralAllele;
        private String source;
        private String displaySoConsequence;
        private String soConsequenceType;
        private String displayConsequence;
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
