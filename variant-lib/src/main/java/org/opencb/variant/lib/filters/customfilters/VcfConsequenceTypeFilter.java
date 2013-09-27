package org.opencb.variant.lib.filters.customfilters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.opencb.variant.lib.core.formats.VariantEffect;
import org.opencb.variant.lib.core.formats.VcfRecord;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: javi
 * Date: 26/09/13
 * Time: 18:33
 * To change this template use File | Settings | File Templates.
 */
public class VcfConsequenceTypeFilter extends VcfFilter {

    private String consequenceType;

    private Client wsRestClient;
    private WebResource webResource;


    public VcfConsequenceTypeFilter(String consequenceType) {
        this.consequenceType = consequenceType;

        wsRestClient =  Client.create();
        webResource = wsRestClient.resource("http://ws.bioinfo.cipf.es/cellbase/rest/latest/hsa/genomic/variant/");

    }

    public VcfConsequenceTypeFilter( String consequenceType, int priority) {
        super(priority);
        this.consequenceType = consequenceType;
    }

    @Override
    public boolean apply(VcfRecord vcfRecord) {

        ObjectMapper mapper = new ObjectMapper();
        boolean res = false;

        List<VariantEffect> batchEffect = new ArrayList<>();
        String chrPos = vcfRecord.getChromosome() + ":" + vcfRecord.getPosition() + ":" + vcfRecord.getReference() + ":" + vcfRecord.getAlternate() ;

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        formDataMultiPart.field("variants", chrPos);

        String response = webResource.path("consequence_type").queryParam("of", "json").type(MediaType.MULTIPART_FORM_DATA).post(String.class, formDataMultiPart);

        try {
            batchEffect = mapper.readValue(response, new TypeReference<List<VariantEffect>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }

        Iterator<VariantEffect> it = batchEffect.iterator();

        VariantEffect effect;
        while(it.hasNext() && !res){
            effect = it.next();

            if(effect.getConsequenceTypeObo().equalsIgnoreCase(this.consequenceType)){
                return true;
            }

        }

        return res;
    }
}
