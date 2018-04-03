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

    @Test
    fun testGetWholeWord() {
        val sentence = "Aa bbb;CC abc"
        Assert.assertEquals("Aa", Utils.getWholeWord(sentence, 0))
        Assert.assertEquals("Aa", Utils.getWholeWord(sentence, 1))
        Assert.assertNull(Utils.getWholeWord(sentence, 2))

        Assert.assertEquals("bbb", Utils.getWholeWord(sentence, 4))
    }
}
