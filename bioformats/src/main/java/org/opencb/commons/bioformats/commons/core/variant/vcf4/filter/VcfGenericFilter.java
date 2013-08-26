package org.opencb.commons.bioformats.commons.core.variant.vcf4.filter;

import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;

public interface VcfGenericFilter {

	public boolean filter(VcfRecord vcfRecord);
	
}
