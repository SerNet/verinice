/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/
package sernet.verinice.bpm.rcp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import sernet.verinice.interfaces.bpm.IndividualServiceParameter;

public class Base64UtilsTest {

    @Test
    public void serializeString() throws IOException {
        String serialized = Base64Utils.toString("Hello World!");
        Assert.assertEquals("rO0ABXQADEhlbGxvIFdvcmxkIQ==", serialized);
    }

    @Test
    public void deserializeString() throws IOException {
        Object deserialized = Base64Utils.fromString("rO0ABXQADEhlbGxvIFdvcmxkIQ==");
        Assert.assertEquals("Hello World!", deserialized);
    }

    @Test
    public void serializeTemplateMap() throws IOException {
        HashMap<String, IndividualServiceParameter> templateMap = new HashMap<>();
        IndividualServiceParameter parameter = new IndividualServiceParameter();
        parameter.setTitle("Foo");
        templateMap.put(parameter.getTitle(), parameter);
        String serialized = Base64Utils.toString(templateMap);
        Assert.assertEquals(
                "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAAAx3CAAAABAAAAABdAADRm9vc3IAOXNlcm5ldC52ZXJpbmljZS5pbnRlcmZhY2VzLmJwbS5JbmRpdmlkdWFsU2VydmljZVBhcmFtZXRlcpaYZGedybvRAgANWgATd2l0aEFSZWxlYXNlUHJvY2Vzc0wACGFzc2lnbmVldAASTGphdmEvbGFuZy9TdHJpbmc7TAASYXNzaWduZWVSZWxhdGlvbklkcQB+AARMABRhc3NpZ25lZVJlbGF0aW9uTmFtZXEAfgAETAALZGVzY3JpcHRpb25xAH4ABEwAB2R1ZURhdGV0ABBMamF2YS91dGlsL0RhdGU7TAAHb3JnVXVpZHEAfgAETAAKcHJvcGVydGllc3QAD0xqYXZhL3V0aWwvU2V0O0wADXByb3BlcnR5TmFtZXNxAH4ABkwAEnJlbWluZGVyUGVyaW9kRGF5c3QAE0xqYXZhL2xhbmcvSW50ZWdlcjtMAAV0aXRsZXEAfgAETAAGdHlwZUlkcQB+AARMAAR1dWlkcQB+AAR4cABwcHBwcHBwcHBxAH4AAnBweA==",
                serialized);
    }

    @Test
    public void roundtripTemplateMap() throws IOException {
        HashMap<String, IndividualServiceParameter> templateMap = new HashMap<>();
        IndividualServiceParameter parameter = new IndividualServiceParameter();
        parameter.setTitle("Foo");
        templateMap.put(parameter.getTitle(), parameter);
        String serialized = Base64Utils.toString(templateMap);

        Object deserialized = Base64Utils.fromString(serialized);
        Assert.assertThat(deserialized, CoreMatchers.is(Map.class));
        @SuppressWarnings("rawtypes")
        Map deserializedAsMap = (Map) deserialized;
        Assert.assertEquals(1, deserializedAsMap.size());
        Object entryValue = deserializedAsMap.get("Foo");
        Assert.assertThat(entryValue, CoreMatchers.is(IndividualServiceParameter.class));
        IndividualServiceParameter deserializedParameter = (IndividualServiceParameter) entryValue;
        Assert.assertEquals("Foo", deserializedParameter.getTitle());

    }

}
