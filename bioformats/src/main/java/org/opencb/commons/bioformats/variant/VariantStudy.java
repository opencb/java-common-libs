package org.opencb.commons.bioformats.variant;

import java.util.List;
import org.opencb.commons.bioformats.pedigree.Pedigree;
import org.opencb.commons.bioformats.variant.vcf4.stats.VcfGlobalStat;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia
 * @todo Add members for date and meta (headers and so on)
 */
public class VariantStudy {

    private String name;
    private String alias;
    private String description;
    private List<String> authors;
    private List<String> samples;
    private Pedigree pedigree;
    private List<String> files;
    private VcfGlobalStat stats;
    

    public VariantStudy(String name, String alias, String description, List<String> authors, List<String> files) {
        this.name = name;
        this.alias = alias;
        this.description = description;
        this.authors = authors;
        this.files = files;
        // TODO initialize pedigree?
    }
    
   
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSamples() {
        return samples;
    }

    public void setSamples(List<String> samples) {
        this.samples = samples;
    }

    public Pedigree getPedigree() {
        return pedigree;
    }

    public void setPedigree(Pedigree pedigree) {
        this.pedigree = pedigree;
    }

    public VcfGlobalStat getStats() {
        return stats;
    }

    public void setStats(VcfGlobalStat stats) {
        this.stats = stats;
    }

}
