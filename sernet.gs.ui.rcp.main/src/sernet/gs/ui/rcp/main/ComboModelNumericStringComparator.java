package sernet.gs.ui.rcp.main;

import sernet.gs.service.AbstractNumericStringComparator;
import sernet.verinice.iso27k.rcp.ComboModelObject;

public class ComboModelNumericStringComparator<T> extends AbstractNumericStringComparator<ComboModelObject<T>> {

    private static final long serialVersionUID = -3048092637667870391L;

    @Override
    public String convertToString(ComboModelObject<T> o) {
        return o.getObject().toString();
    }
}
