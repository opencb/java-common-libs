package org.opencb.commons.bioformats.variant.vcf4.annotators;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.opencb.commons.bioformats.variant.vcf4.VariantEffect;
import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: javi
 * Date: 26/09/13
 * Time: 17:57
 * To change this template use File | Settings | File Templates.
 */
public class VcfConsequenceTypeAnnotator implements VcfAnnotator {

    private Client wsRestClient;
    private WebTarget webResource;


    public VcfConsequenceTypeAnnotator() {
        wsRestClient = ClientBuilder.newClient();
        webResource = wsRestClient.target("http://ws.bioinfo.cipf.es/cellbase/rest/latest/hsa/genomic/variant/");

    }

    @Override
    public void annot(List<VcfRecord> batch) {

        ObjectMapper mapper = new ObjectMapper();
        List<VariantEffect> batchEffect = new ArrayList<>();

        StringBuilder chunkVcfRecords = new StringBuilder();


        for (VcfRecord record : batch) {
            chunkVcfRecords.append(record.getChromosome()).append(":");
            chunkVcfRecords.append(record.getPosition()).append(":");
            chunkVcfRecords.append(record.getReference()).append(":");
            chunkVcfRecords.append(record.getAlternate()).append(",");

        }


        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        formDataMultiPart.field("variants", chunkVcfRecords.substring(0, chunkVcfRecords.length() - 1));

        System.out.println("WEB");
//        String response = webResource.path("consequence_type").queryParam("of", "json").type(MediaType.MULTIPART_FORM_DATA).post(String.class, formDataMultiPart);
        Response response = webResource.path("consequence_type").queryParam("of", "json").request(MediaType.MULTIPART_FORM_DATA).post(Entity.text(formDataMultiPart.toString()));

        try {
            batchEffect = mapper.readValue(response.toString(), new TypeReference<List<VariantEffect>>() {
            });
        } catch (IOException e) {
            System.err.println(chunkVcfRecords.toString());
            e.printStackTrace();
        }

        for (VcfRecord variant : batch) {

            annotVariantEffect(variant, batchEffect);
        }

    }

    private void annotVariantEffect(VcfRecord variant, List<VariantEffect> batchEffect) {

        Set<String> ct = new HashSet<>();
        for (VariantEffect effect : batchEffect) {

            if (variant.getChromosome().equals(effect.getChromosome()) &&
                    variant.getPosition() == effect.getPosition() &&
                    variant.getReference().equals(effect.getReferenceAllele()) &&
                    variant.getAlternate().equals(effect.getAlternativeAllele())) {

                ct.add(effect.getConsequenceTypeObo());
            }

        }

        String ct_all = Joiner.on(",").join(ct);

        if (ct.size() > 0) {
            variant.addInfoField("ConsType=" + ct_all);
        }

    }

    @Override
    public void annot(VcfRecord elem) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
