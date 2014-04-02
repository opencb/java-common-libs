package org.opencb.commons.bioformats.alignment.tasks;

import org.opencb.commons.bioformats.alignment.Alignment;
import org.opencb.commons.bioformats.alignment.AlignmentRegion;
import org.opencb.commons.run.Task;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jmmut
 * Date: 3/31/14
 * Time: 5:55 PM
 *
 * @brief This task obtains the most repeated values in the alignments belonging to some range
 */
public class AlignmentModeTask extends Task<AlignmentRegion> {
    private Map<String, Integer> nameMap;

    /**/private String chromosome;
    /**/private long start;
    /**/private long end;
    /**/private long unclippedStart;
    /**/private long unclippedEnd;

    /**/private int length;
    /**/private int mappingQuality;
    /**/private String qualities;   // TODO Find an alternative way to store qualities
    /**/private String mateReferenceName;
    /**/private int mateAlignmentStart;
    /**/private int inferredInsertSize;
    /**/private List<Alignment.AlignmentDifference> differences;
    /**/private Map<String, Object> attributes;
    private Map<Integer, Integer> flagsMap;

    @Override
    public boolean apply(List<AlignmentRegion> batch) throws IOException {
        String name;
        Map.Entry<String, Integer> nameEntry;
        Integer flags;
        Map.Entry<Integer, Integer> flagsEntry;


        for (AlignmentRegion alignmentRegion : batch) {
            System.out.println("en un alignmentRegion con num alignments = " + alignmentRegion.getAlignments().size());
            nameMap = new HashMap<>();
            flagsMap = new HashMap<>();
            for (Alignment alignment : alignmentRegion.getAlignments()) {
                name = alignment.getName();
                nameMap.put(name, !nameMap.containsKey(name)? 1: nameMap.get(name)+1);
                flags = alignment.getFlags();
                flagsMap.put(flags, !flagsMap.containsKey(flags)? 1: flagsMap.get(flags)+1);
            }

            nameEntry = new AbstractMap.SimpleEntry<String, Integer>("", 0);
            for (Map.Entry<String, Integer> entry: nameMap.entrySet()) {
                if (entry.getValue() > nameEntry.getValue()) {
                    nameEntry = entry;
                }

            }
            flagsEntry = new AbstractMap.SimpleEntry<Integer, Integer>(0, 0);
            for (Map.Entry<Integer, Integer> entry: flagsMap.entrySet()) {
                if (entry.getValue() > flagsEntry.getValue()) {
                    flagsEntry = entry;
                }
                System.out.println("clave: " + entry.getKey() + ", valor : " + entry.getValue());
            }

            alignmentRegion.setModeAlignment(new Alignment(nameEntry.getKey(), chromosome, start, end, unclippedStart, unclippedEnd, length
                    , mappingQuality, qualities, mateReferenceName, mateAlignmentStart, inferredInsertSize, flagsEntry.getKey(), differences, attributes));
        }

        return false;
    }
}
