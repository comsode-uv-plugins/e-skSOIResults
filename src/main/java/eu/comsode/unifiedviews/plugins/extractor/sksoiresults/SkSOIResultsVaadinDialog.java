package eu.comsode.unifiedviews.plugins.extractor.sksoiresults;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog .
 */
public class SkSOIResultsVaadinDialog extends AbstractDialog<SkSOIResultsConfig_V1> {

    public SkSOIResultsVaadinDialog() {
        super(SkSOIResults.class);
    }

    @Override
    public void setConfiguration(SkSOIResultsConfig_V1 c) throws DPUConfigException {

    }

    @Override
    public SkSOIResultsConfig_V1 getConfiguration() throws DPUConfigException {
        final SkSOIResultsConfig_V1 c = new SkSOIResultsConfig_V1();

        return c;
    }

    @Override
    public void buildDialogLayout() {
    }

}
