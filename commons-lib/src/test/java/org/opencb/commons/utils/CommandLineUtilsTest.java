package org.opencb.commons.utils;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.opencb.commons.utils.CommandLineUtils.DEPRECATED;

/**
 * Created on 15/03/17.
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class CommandLineUtilsTest {


    @Test
    public void cutLine() {
        String line = "";
        for (int i = 0; i < 240; i++) {
            line += "a";
        }
        for (int i = 0; i < 240; i++) {
            line += "b";
        }
        for (int i = 0; i < 239; i++) {
            line += "c";
        }
        List<String> lines = new ArrayList<String>();
        String row = "";
        do {
            row = line.substring(0, 80);
            line = line.substring(80);
            lines.add(row);
        } while (line.length() > 80);
        lines.add(line);
        for (String l : lines) {
            System.out.println(l);
        }
    }

    @Parameters(commandNames = "myCommand")
    static class Command {
        @Parameter(names = {"-1", "--anInt"}, description = "Really long description: Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec convallis eu nisl pulvinar sagittis. Nullam tristique tempus urna fermentum ornare. Donec rhoncus velit at augue finibus, commodo porta arcu blandit. Sed ut nibh enim. Fusce sollicitudin sed lorem et fermentum. Phasellus auctor lorem id arcu vehicula, sed aliquet felis accumsan. Pellentesque luctus mi sed congue convallis. Nulla vel laoreet purus. Proin vitae egestas tellus, vitae ullamcorper leo. Donec ultrices dui velit")
        public int anInt;
        @Parameter(names = {"-2", "--anInteger"}, required = true, description = "Description")
        public Integer anInteger;
        @Parameter(names = {"-3", "--aLong"}, description = "Description")
        public long aLong;
        @Parameter(names = {"-4", "--aString"}, description = "Description")
        public String aString;
        @Parameter(names = {"-5", "--aStringList"}, description = "Description")
        public List<String> aStringList;
        @Parameter(names = {"-5a", "--aVariableArityStringList"}, description = "Description", variableArity = true)
        public List<String> aVariableArityStringList;
        @Parameter(names = {"-6", "--aHidden"}, description = "Description hidden", hidden = true)
        public String aHidden;
        @Parameter(names = {"-7", "--aDeprecated"}, description = DEPRECATED + "use --non-deprecated")
        public String aDeprecated;
        @DynamicParameter(names = {"-D"}, description = "Description")
        public Map<String, String> dynamic = new HashMap<>();
        @DynamicParameter(names = {"-F"}, assignment = ":", description = "Description")
        public Map<String, String> dynamic2 = new HashMap<>();
    }
}