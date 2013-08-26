package org.opencb.commons.bioformats.commons.core.variant.vcf4.filter;

import org.bioinfo.formats.core.variant.vcf4.VcfRecord;

public class VcfSnpFilter implements VcfGenericFilter {

	@Override
	public boolean filter(VcfRecord vcfRecord) {
		return (!vcfRecord.getId().equalsIgnoreCase(".") && !vcfRecord.getId().trim().equalsIgnoreCase("")); 
	}

}
