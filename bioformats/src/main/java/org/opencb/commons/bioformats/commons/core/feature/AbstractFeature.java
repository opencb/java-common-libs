package org.opencb.commons.bioformats.commons.core.feature;

@Deprecated
public abstract class AbstractFeature {
	
	private String chromosome;
	private int start;
	private int end;
	private String strand;
	
	public AbstractFeature() {
		
	}

	/**
	 * @param chromosome
	 * @param start
	 * @param end
	 * @param strand
	 */
	public AbstractFeature(String chromosome, int start, int end, String strand) {
		super();
		this.chromosome = chromosome;
		this.start = start;
		this.end = end;
		this.strand = strand;
	}

	
}
