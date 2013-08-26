package org.opencb.commons.bioformats.commons.core.variant.vcf4;

public class VcfFilter {
	private String id;
	private String description;
	
	public VcfFilter(String filterLine) {
		// ##FILTER=<ID=q10,Description="Variants that not pass quality 10">
		String[] fields = filterLine.replaceAll("[\"<>]", "").split("=");
		// fields[2] ==> q10,Description
		this.id = fields[2].split(",")[0];
		// fields[3] ==> "Variants that not pass quality 10"
		this.description = fields[3];
	}
	
	public VcfFilter(String id, String description) {
		this.id = id;
		this.description = description;
	}
	
	@Override
	public String toString() {
		return "##FILTER=<ID="+id+",Description=\""+description+"\">";
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
