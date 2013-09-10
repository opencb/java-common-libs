package org.opencb.variant.lib.core.formats;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 9/10/13
 * Time: 8:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariantInfo {

    private VcfVariantStat stats;
    private List<VariantEffect> effect;

    public VariantInfo(){

        effect = new ArrayList<>();
    }

    public VcfVariantStat getStats() {
        return stats;
    }

    public void setStats(VcfVariantStat stats) {
        this.stats = stats;
    }
}
