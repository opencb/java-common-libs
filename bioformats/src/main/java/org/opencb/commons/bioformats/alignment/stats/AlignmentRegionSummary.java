package org.opencb.commons.bioformats.alignment.stats;


import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 3/31/14
 * Time: 8:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlignmentRegionSummary {

    
    private long start;
    private long end;

    private int defaultLength;
    private int defaultFlag;
    private int defaultMapQ;
    private String defaultRNext;

    private Map<String, Integer> keyMap;
    private Map<Map.Entry<Integer, Object>, Integer> tagMap;

    public AlignmentRegionSummary() {
        keyMap = new HashMap<>();
        tagMap = new HashMap<>();
    }

    public AlignmentRegionSummary(int defaultLength, int defaultFlag, int defaultMapQ, String defaultRNext, Map<String, Integer> keyMap, Map<Map.Entry<Integer, Object>, Integer> tagMap) {
        this.defaultLength = defaultLength;
        this.defaultFlag = defaultFlag;
        this.defaultMapQ = defaultMapQ;
        this.defaultRNext = defaultRNext;
        this.keyMap = keyMap;
        this.tagMap = tagMap;
    }

    public int getDefaultLength() {
        return defaultLength;
    }

    public void setDefaultLength(int defaultLength) {
        this.defaultLength = defaultLength;
    }

    public int getDefaultFlag() {
        return defaultFlag;
    }

    public void setDefaultFlag(int defaultFlag) {
        this.defaultFlag = defaultFlag;
    }

    public int getDefaultMapQ() {
        return defaultMapQ;
    }

    public void setDefaultMapQ(int defaultMapQ) {
        this.defaultMapQ = defaultMapQ;
    }

    public String getDefaultRNext() {
        return defaultRNext;
    }

    public void setDefaultRNext(String defaultRNext) {
        this.defaultRNext = defaultRNext;
    }

    public Map<String, Integer> getKeyMap() {
        return keyMap;
    }

    public void setKeyMap(Map<String, Integer> keyMap) {
        this.keyMap = keyMap;
    }

    public Map<Map.Entry<Integer, Object>, Integer> getTagMap() {
        return tagMap;
    }

    public void setTagMap(Map<Map.Entry<Integer, Object>, Integer> tagMap) {
        this.tagMap = tagMap;
    }
}
