package com.esperantajvortaroj.app

import org.junit.Assert
import org.junit.Test

class UtilsTest {
    @Test
    fun testAddHats() {
        Assert.assertEquals("", Utils.addHats(""))
        Assert.assertEquals("saluton", Utils.addHats("saluton"))
        Assert.assertEquals("serĉi", Utils.addHats("sercxi"))
        Assert.assertEquals("ĉŝĝĵĥŭĉŝĝĵĥŭ", Utils.addHats("CxSxGxJxHxUxcxsxgxjxhxux"))
    }
}
