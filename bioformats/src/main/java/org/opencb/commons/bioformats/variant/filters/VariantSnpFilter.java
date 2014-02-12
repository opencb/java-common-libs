package org.opencb.commons.bioformats.variant.filters;


import org.opencb.commons.bioformats.variant.Variant;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class VariantSnpFilter extends VariantFilter {


    public VariantSnpFilter(int priority) {
        super(priority);
    }

    public VariantSnpFilter() {
        super();
    }

    @Override
    public boolean apply(Variant variant) {
        return (!variant.getId().equalsIgnoreCase(".") && !variant.getId().equalsIgnoreCase(""));
    }

}
