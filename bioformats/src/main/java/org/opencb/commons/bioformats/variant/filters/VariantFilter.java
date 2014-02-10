package org.opencb.commons.bioformats.variant.filters;

import org.opencb.commons.bioformats.variant.Variant;
import org.opencb.commons.filters.Filter;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public abstract class VariantFilter extends Filter<Variant> {

    public VariantFilter() {
        super(0);
    }

    public VariantFilter(int priority) {
        super(priority);
    }
}
