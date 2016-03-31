/*
 * Copyright 2016 Moritz Reiter.
 *
 * <p>This file is part of Verinice.
 *
 * <p>Verinice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * <p>Verinice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License
 * along with Verinice. If not, see http://www.gnu.org/licenses/.
 */

package sernet.verinice.samt.rcp;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.test.helper.vnaimport.BeforeEachVNAImportHelper;

import java.math.BigDecimal;

@SuppressWarnings("restriction")
public class IsaDecoratorUtilTest extends BeforeEachVNAImportHelper {

    private static final String VNA_FILE = "IsaDecoratorUtilTest-data.vna";

    private static final String SOURCE_ID = "329b3c";

    // ExtIds
    private static final String GENERIC_AUDIT_EXT_ID = "ENTITY_19020";
    private static final String VDA_ISA_EXT_ID = "ENTITY_17976";
    private static final String CONTROL_GROUP_1_EXT_ID = "ENTITY_18340";
    private static final String CONTROL_GROUP_5_EXT_ID = "ENTITY_18053";
    private static final String CONTROL_GROUP_6_EXT_ID = "ENTITY_18072";
    private static final String CONTROL_GROUP_7_EXT_ID = "ENTITY_18387";
    private static final String ISA_CONTROL_1_1_EXT_ID = "ENTITY_18373";
    private static final String ISA_CONTROL_1_2_EXT_ID = "ENTITY_18359";
    private static final String ISA_CONTROL_1_3_EXT_ID = "ENTITY_18345";
    private static final String ISA_CONTROL_5_1_EXT_ID = "ENTITY_18058";
    private static final String ISA_CONTROL_6_1_EXT_ID = "ENTITY_18091";
    private static final String ISA_CONTROL_6_2_EXT_ID = "ENTITY_18077";
    private static final String ISA_CONTROL_6_3_EXT_ID = "ENTITY_18105";
    private static final String ISA_CONTROL_7_1_EXT_ID = "ENTITY_18392";

    Audit genericAudit;
    Audit vdaIsa;

    @Before
    public void loadData() throws CommandException {

        genericAudit = (Audit) loadElement(SOURCE_ID, GENERIC_AUDIT_EXT_ID);
        vdaIsa = (Audit) loadElement(SOURCE_ID, VDA_ISA_EXT_ID);
    }

    @Test
    public void decoratorColorForIsaControl1_1IsRed() throws CommandException {

        SamtTopic isaControl1_1 = (SamtTopic) loadElement(SOURCE_ID, ISA_CONTROL_1_1_EXT_ID);

        assertThat(IsaDecoratorUtil.decoratorColor(isaControl1_1),
                equalTo(IsaDecoratorUtil.DecoratorColor.RED));
    }

    @Test
    public void decoratorColorForIsaControl1_2IsGreen() throws CommandException {

        SamtTopic isaControl1_2 = (SamtTopic) loadElement(SOURCE_ID, ISA_CONTROL_1_2_EXT_ID);

        assertThat(IsaDecoratorUtil.decoratorColor(isaControl1_2),
                equalTo(IsaDecoratorUtil.DecoratorColor.GREEN));
    }

    @Test
    public void decoratorColorForIsaControl1_3IsYellow() throws CommandException {

        SamtTopic isaControl1_3 = (SamtTopic) loadElement(SOURCE_ID, ISA_CONTROL_1_3_EXT_ID);

        assertThat(IsaDecoratorUtil.decoratorColor(isaControl1_3),
                equalTo(IsaDecoratorUtil.DecoratorColor.YELLOW));
    }

    @Test
    public void decoratorColorForIsaControl5_1IsGreen() throws CommandException {

        SamtTopic isaControl5_1 = (SamtTopic) loadElement(SOURCE_ID, ISA_CONTROL_5_1_EXT_ID);

        assertThat(IsaDecoratorUtil.decoratorColor(isaControl5_1),
                equalTo(IsaDecoratorUtil.DecoratorColor.GREEN));
    }

    @Test
    public void decoratorColorForIsaControl6_1IsNull() throws CommandException {

        SamtTopic isaControl6_1 = (SamtTopic) loadElement(SOURCE_ID, ISA_CONTROL_6_1_EXT_ID);

        assertThat(IsaDecoratorUtil.decoratorColor(isaControl6_1),
                equalTo(IsaDecoratorUtil.DecoratorColor.NULL));
    }

    @Test
    public void decoratorColorForIsaControl6_2IsGreen() throws CommandException {

        SamtTopic isaControl6_2 = (SamtTopic) loadElement(SOURCE_ID, ISA_CONTROL_6_2_EXT_ID);

        assertThat(IsaDecoratorUtil.decoratorColor(isaControl6_2),
                equalTo(IsaDecoratorUtil.DecoratorColor.GREEN));
    }

    @Test
    public void decoratorColorForIsaControl6_3IsRed() throws CommandException {

        SamtTopic isaControl6_3 = (SamtTopic) loadElement(SOURCE_ID, ISA_CONTROL_6_3_EXT_ID);

        assertThat(IsaDecoratorUtil.decoratorColor(isaControl6_3),
                equalTo(IsaDecoratorUtil.DecoratorColor.RED));
    }

    @Test
    public void decoratorColorForIsaControl7_1IsRed() throws CommandException {

        SamtTopic isaControl7_1 = (SamtTopic) loadElement(SOURCE_ID, ISA_CONTROL_7_1_EXT_ID);

        assertThat(IsaDecoratorUtil.decoratorColor(isaControl7_1),
                equalTo(IsaDecoratorUtil.DecoratorColor.RED));
    }

    @Test
    public void decoratorColorForControlGroup1IsYellow() throws CommandException {

        ControlGroup controlGroup1 = (ControlGroup) loadElement(SOURCE_ID, CONTROL_GROUP_1_EXT_ID);

        assertThat(IsaDecoratorUtil.decoratorColor(controlGroup1),
                equalTo(IsaDecoratorUtil.DecoratorColor.YELLOW));
    }

    @Test
    public void decoratorColorForControlGroup5IsGreen() throws CommandException {

        ControlGroup controlGroup5 = (ControlGroup) loadElement(SOURCE_ID, CONTROL_GROUP_5_EXT_ID);

        assertThat(IsaDecoratorUtil.decoratorColor(controlGroup5),
                equalTo(IsaDecoratorUtil.DecoratorColor.GREEN));
    }

    @Test
    public void decoratorColorForControlGroup6IsRed() throws CommandException {

        ControlGroup controlGroup6 = (ControlGroup) loadElement(SOURCE_ID, CONTROL_GROUP_6_EXT_ID);

        assertThat(IsaDecoratorUtil.decoratorColor(controlGroup6),
                equalTo(IsaDecoratorUtil.DecoratorColor.RED));
    }

    @Test
    public void decoratorColorForControlGroup7IsRed() throws CommandException {

        ControlGroup controlGroup7 = (ControlGroup) loadElement(SOURCE_ID, CONTROL_GROUP_7_EXT_ID);

        assertThat(IsaDecoratorUtil.decoratorColor(controlGroup7),
                equalTo(IsaDecoratorUtil.DecoratorColor.RED));
    }

    @Test
    public void decoratorColorForVdaIsaIsRed() throws CommandException {

        Audit audit = (Audit) loadElement(SOURCE_ID, VDA_ISA_EXT_ID);

        assertThat(IsaDecoratorUtil.decoratorColor(audit),
                equalTo(IsaDecoratorUtil.DecoratorColor.RED));
    }
    
    @Test
    public void resultScoreForVdaIsaIsCorrect() throws CommandException {
        
        Audit audit = (Audit) loadElement(SOURCE_ID, VDA_ISA_EXT_ID);
        
        assertThat(IsaDecoratorUtil.resultScore(audit), equalTo(BigDecimal.valueOf(0.26)));
    }

    @Override
    protected String getFilePath() {

        return this.getClass().getResource(VNA_FILE).getPath();
    }

    @Override
    protected SyncParameter getSyncParameter() throws SyncParameterException {

        return new SyncParameter(true, true, true, false,
                SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
    }
}
