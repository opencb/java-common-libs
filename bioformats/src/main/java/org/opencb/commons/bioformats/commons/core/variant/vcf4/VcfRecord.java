package org.opencb.commons.bioformats.commons.core.variant.vcf4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bioinfo.commons.utils.ListUtils;
import org.opencb.commons.bioformats.commons.core.vcfstats.Genotype;

public class VcfRecord {

	private String chromosome;
	private int position;
	private String id;
	private String reference;
	private String alternate;
	private String quality;
	private String filter;
	private String info;
	private String format;
    private List<String> samples;
	
	/**
	 * @param chromosome
	 * @param position
	 * @param id
	 * @param reference
	 * @param alternate
	 * @param quality
	 * @param filter
	 * @param info
	 */
	public VcfRecord(String chromosome, Integer position, String id, String reference, String alternate, String quality, String filter, String info) {
		this.chromosome = chromosome;
		this.position = position;
		this.id = id;
		this.reference = reference;
		this.alternate = alternate;
		this.quality = quality;
		this.filter = filter;
		this.info = info;
	}
	
	/**
	 * @param chromosome
	 * @param position
	 * @param id
	 * @param reference
	 * @param alternate
	 * @param quality
	 * @param filter
	 * @param info
	 */
	public VcfRecord(String chromosome, Integer position, String id, String reference, String alternate, String quality, String filter, String info, String format) {
		this(chromosome, position, id, reference, alternate, quality, filter, info);
		this.format =  format;
	}

	/**
	 * 
	 * @param chromosome
	 * @param position
	 * @param id
	 * @param reference
	 * @param alternate
	 * @param quality
	 * @param filter
	 * @param info
	 * @param format
	 * @param sampleList
     *
	 */
	public VcfRecord(String chromosome, Integer position, String id, String reference, String alternate, String quality, String filter, String info, String format, String ... sampleList) {
		this(chromosome, position, id, reference, alternate, quality, filter, info, format);

		samples = new ArrayList<String>();
		for(String sample: sampleList) {
			samples.add(sample);
		}
	}
	
	public VcfRecord(String[] fields) {
//		this(chromosome, position, id, reference, alternate, quality, filter, info, format);
		this(fields[0], Integer.parseInt(fields[1]), fields[2], fields[3], fields[4], fields[5], fields[6], fields[7], fields[8]);

		samples = new ArrayList<String>(fields.length-9);
		for(int i=9; i<fields.length; i++) {
			samples.add(fields[i]);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(chromosome).append("\t");
		builder.append(position).append("\t");
		builder.append(id).append("\t");
		builder.append(reference).append("\t");
		builder.append(alternate).append("\t");
		builder.append(quality).append("\t");
		builder.append(filter).append("\t");
		builder.append(info);
		if(format != null) {
			builder.append("\t").append(format);
		}
		if(samples != null) {
			builder.append("\t").append(ListUtils.toString(samples, "\t"));
		}
		return builder.toString();
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
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}
	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
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
	 * @return the reference
	 */
	public String getReference() {
		return reference;
	}
	/**
	 * @param reference the reference to set
	 */
	public void setReference(String reference) {
		this.reference = reference;
	}

	/**
	 * @return the alternate
	 */
	public String getAlternate() {
		return alternate;
	}
	/**
	 * @param alternate the alternate to set
	 */
	public void setAlternate(String alternate) {
		this.alternate = alternate;
	}

	/**
	 * @return the quality
	 */
	public String getQuality() {
		return quality;
	}
	/**
	 * @param quality the quality to set
	 */
	public void setQuality(String quality) {
		this.quality = quality;
	}

	/**
	 * @return the filter
	 */
	public String getFilter() {
		return filter;
	}
	/**
	 * @param filter the filter to set
	 */
	public void setFilter(String filter) {
		this.filter = filter;
	}

	/**
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}
	/**
	 * @param info the info to set
	 */
	public void setInfo(String info) {
		this.info = info;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}


    public List<String> getSamples() {
        return samples;
    }

    public void setSamples(List<String> samples) {
        this.samples = samples;
    }

    public String[]  getAltAlleles(){
        return this.getAlternate().split(",");
    }

    public String getValueFormatSample(String key, String sample) {

        String[] array_format = this.format.split(":");
        int field_pos = Arrays.asList(array_format).indexOf(key);
        if( field_pos >= 0){
            return sample.split(":")[field_pos];
        }   else{
            return null;
        }
    }

    public Genotype getSampleGenotype(String sample){
        Genotype g = null;
        String gt_val = getValueFormatSample("GT", sample);
        if(gt_val != null){
            g = new Genotype(gt_val);
        }
        return g;

    }

}
