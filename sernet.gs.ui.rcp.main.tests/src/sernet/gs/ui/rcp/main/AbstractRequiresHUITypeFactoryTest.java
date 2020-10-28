package sernet.gs.ui.rcp.main;

import java.io.File;
import java.net.MalformedURLException;

import org.junit.BeforeClass;

import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.snutils.DBException;

public abstract class AbstractRequiresHUITypeFactoryTest {
    @BeforeClass
    public static void setupTypeFactory() throws DBException, MalformedURLException {
        if (!VeriniceContext.exists(VeriniceContext.HUI_TYPE_FACTORY)) {
            HUITypeFactory huiTypeFactory = HUITypeFactory
                    .createInstance(new File("../sernet.gs.server/WebContent/WEB-INF/"
                            + HUITypeFactory.HUI_CONFIGURATION_FILE).toURI().toURL());
            VeriniceContext.put(VeriniceContext.HUI_TYPE_FACTORY, huiTypeFactory);
        }
    }
}
