package org.opencb.commons.bioformats.commons.core.variant.vcf4.filter;

import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;

import java.util.Arrays;

public class VcfInfoFilter implements VcfGenericFilter {

	private String filter;

	public VcfInfoFilter(String filter) {
		this.filter = filter;
	}

	@Override
	public boolean filter(VcfRecord vcfRecord) {
		String[] infoOptions = vcfRecord.getInfo().split(";");
		String[] keyValue;
		String[] filterOptions = filter.split("[=<>]");
		Arrays.toString(filterOptions);
		
		for(String infoOption: infoOptions) {
			keyValue = infoOption.split("[=<>]");
			if(keyValue[0].equals(filterOptions[0])) {
				if(keyValue.length == 1) {
					return keyValue[0].equals(filterOptions[0]);
				}else {
					if(keyValue.length == 2) {
						if(filter.indexOf('=') != -1) {
							return Double.parseDouble(filterOptions[1]) == Double.parseDouble(keyValue[1]);
						}else {
							if(filter.indexOf('>') != -1) {
								return Double.parseDouble(filterOptions[1]) > Double.parseDouble(keyValue[1]);
							}else {	// keyValue[1].equals("<")
//								System.out.println(Arrays.toString(keyValue)+" "+Arrays.toString(filterOptions));
								try {
									return Double.parseDouble(filterOptions[1]) < Double.parseDouble(keyValue[1]);
								}catch(Exception e) {
									return false;
								}
							}
						}
					}else {	// keyValue.length == 3

					}	
				}	
			}
		}
		return false;
	}

}
