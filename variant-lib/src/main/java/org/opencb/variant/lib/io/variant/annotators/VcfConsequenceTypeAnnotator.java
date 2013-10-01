package org.opencb.variant.lib.io.variant.annotators;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.apache.commons.lang.StringUtils;
import org.opencb.variant.lib.core.formats.VariantEffect;
import org.opencb.variant.lib.core.formats.VcfRecord;

import javax.ws.rs.core.MediaType;
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
    private WebResource webResource;



    public VcfConsequenceTypeAnnotator(){
        wsRestClient =  Client.create();
        webResource = wsRestClient.resource("http://ws.bioinfo.cipf.es/cellbase/rest/latest/hsa/genomic/variant/");

    }

    @Override
    public void annot(List<VcfRecord> batch) {

        ObjectMapper mapper = new ObjectMapper();
        List<VariantEffect> batchEffect = new ArrayList<>();

        StringBuilder chunkVcfRecords = new StringBuilder();


        for(VcfRecord record : batch){
            chunkVcfRecords.append(record.getChromosome()).append(":");
            chunkVcfRecords.append(record.getPosition()).append(":");
            chunkVcfRecords.append(record.getReference()).append(":");
            chunkVcfRecords.append(record.getAlternate()).append(",");

        }

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        formDataMultiPart.field("variants", chunkVcfRecords.substring(0, chunkVcfRecords.length()-1));

        String response = webResource.path("consequence_type").queryParam("of", "json").type(MediaType.MULTIPART_FORM_DATA).post(String.class, formDataMultiPart);

        try {
            batchEffect = mapper.readValue(response, new TypeReference<List<VariantEffect>>(){});
        } catch (IOException e) {
            System.err.println(chunkVcfRecords.toString());
            e.printStackTrace();
        }

        for(VcfRecord variant: batch){

            annotVariantEffect(variant, batchEffect);
        }

    }

    private void annotVariantEffect(VcfRecord variant, List<VariantEffect> batchEffect) {

        Set<String> ct = new HashSet<>();
        for(VariantEffect effect: batchEffect){

            if(variant.getChromosome().equals(effect.getChromosome()) &&
                    variant.getPosition() == effect.getPosition() &&
                    variant.getReference().equals(effect.getReferenceAllele())&&
                    variant.getAlternate().equals(effect.getAlternativeAllele())){

                ct.add(effect.getConsequenceTypeObo()) ;
            }

        }

        String ct_all = StringUtils.join(ct, ",");

        if(ct.size() > 0){
            variant.addInfoField("ConsType=" + ct_all);
        }

    }

    @Override
    public void annot(VcfRecord elem) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
