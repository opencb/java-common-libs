package org.opencb.commons.bioformats.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alejandro Aleman Ramos
 * @author Cristina Yenyxe Gonzalez Garcia
 */
public class Region {

    private String chromosome;
    private long start;
    private long end;

    public Region(String chromosome, long start, long end) {
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
    }

    public Region(String region) {
        Pattern pattern = Pattern.compile("(\\w+):(\\d+)-(\\d+)");
        Matcher matcher = pattern.matcher(region);
        if (matcher.find()) {
            this.chromosome = matcher.group(1);
            this.start = Integer.valueOf(matcher.group(2));
            this.end = Integer.valueOf(matcher.group(3));
        }
    }

    public static Region parseRegion(String regionString) {
        Region region = null;
        if (regionString != null && !regionString.equals("")) {
            if (regionString.indexOf(':') != -1) {
                String[] fields = regionString.split("[:-]", -1);
                if (fields.length == 3) {
                    region = new Region(fields[0], Integer.parseInt(fields[1]), Integer.parseInt(fields[2]));
                }
            } else {
                region = new Region(regionString, 0, Integer.MAX_VALUE);
            }
        }
        return region;
    }

    public static List<Region> parseRegions(String regionsString) {
        List<Region> regions = null;
        if (regionsString != null && !regionsString.equals("")) {
            String[] regionItems = regionsString.split(",");
            regions = new ArrayList<>(regionItems.length);
            String[] fields;
            for (String regionString : regionItems) {
                if (regionString.indexOf(':') != -1) {
                    fields = regionString.split("[:-]", -1);
                    if (fields.length == 3) {
                        regions.add(new Region(fields[0], Integer.parseInt(fields[1]), Integer.parseInt(fields[2])));
                    } else {
                        regions.add(null);
                    }
                } else {
                    regions.add(new Region(regionString, 0, Integer.MAX_VALUE));
                }
            }
        }
        return regions;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Region region = (Region) o;

        if (end != region.end) {
            return false;
        }
        if (start != region.start) {
            return false;
        }
        if (!chromosome.equals(region.chromosome)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = chromosome.hashCode();
        result = 31 * result + (int) (start ^ (start >>> 32));
        result = 31 * result + (int) (end ^ (end >>> 32));
        return result;
    }

    public boolean contains(String chr, long pos) {
        if (this.chromosome.equals(chr) && this.start <= pos && this.end >= pos) {
            return true;
        } else {
            return false;
        }
    }
}
