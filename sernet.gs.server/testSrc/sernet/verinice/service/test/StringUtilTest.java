package sernet.verinice.service.test;

import org.junit.Assert;
import org.junit.Test;

import sernet.gs.service.StringUtil;

public class StringUtilTest {

    @Test
    public void testTruncate() {
        Assert.assertEquals("Hello", StringUtil.truncate("Hello", 10));
        Assert.assertEquals("Hello", StringUtil.truncate("Hello", 5));
        Assert.assertEquals("Hel…", StringUtil.truncate("Hello", 4));
        Assert.assertEquals("Hell…", StringUtil.truncate("Hello World!", 5));
        Assert.assertEquals("", StringUtil.truncate("", 5));
        Assert.assertEquals(null, StringUtil.truncate(null, 5));
    }

}
