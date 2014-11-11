package org.fruct.oss.accessibilitymap.Model;

import java.util.List;

/**
 * Created by alexander on 11.11.14.
 */
public class Accessibility {
    public Category category;
    public MaintenanceForm maintenanceForm;

    public List<FunctionalAreas> functionalAreas;

    public Accessibility(Category category, MaintenanceForm maintenanceForm, List<FunctionalAreas> functionalAreas) {
        this.category = category;
        this.maintenanceForm = maintenanceForm;
        this.functionalAreas = functionalAreas;
    }
}
