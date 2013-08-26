package org.opencb.commons.bioformats.commons.core.variant.vcf4.filter;

import org.bioinfo.formats.core.variant.vcf4.VcfRecord;

public class VcfRegionFilter implements VcfGenericFilter {

	private String chromosome;
	private int start;
	private int end;
	
	public VcfRegionFilter(String chromosome, int start, int end) {
		this.chromosome = chromosome;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public boolean filter(VcfRecord vcfRecord) {
		return (vcfRecord.getChromosome().equalsIgnoreCase(chromosome) && vcfRecord.getPosition() > start && vcfRecord.getPosition() < end);
//		if(vcfRecord.getChromosome().equals(chromosome) && vcfRecord.getPosition() >= start && vcfRecord.getPosition() <= end) {
//			return true;
//		}else {
//			return false;
//		}
	}

	
	/**
	 * @return the chromosome
	 */
	public String getChromosome() {
		return chromosome;
	}

	/**
	 * @param chromosome the chromosome to set
	 */
	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}


	/**
	 * @return the start
	 */
	public int getStart() {
		return start;
	}


	/**
	 * @param start the start to set
	 */
	public void setStart(int start) {
		this.start = start;
	}


	/**
	 * @return the end
	 */
	public int getEnd() {
		return end;
	}


	/**
	 * @param end the end to set
	 */
	public void setEnd(int end) {
		this.end = end;
	}

	
}
