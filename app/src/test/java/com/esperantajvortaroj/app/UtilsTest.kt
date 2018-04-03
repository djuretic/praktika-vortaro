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

    @Test
    fun testGetPossibleBaseWords() {
        Assert.assertEquals(arrayListOf("amiko"), Utils.getPossibleBaseWords("amiko"))
        Assert.assertEquals(arrayListOf("amikoj", "amiko"), Utils.getPossibleBaseWords("amikoj"))
        Assert.assertEquals(arrayListOf("amikajn", "amikaj", "amika"), Utils.getPossibleBaseWords("amikajn"))

        Assert.assertEquals(arrayListOf("ajn"), Utils.getPossibleBaseWords("ajn"))
    }

    @Test
    fun testGetPossibleBaseWordsVerbs(){
        Assert.assertEquals(arrayListOf("vidas", "vidi"), Utils.getPossibleBaseWords("vidas"))
        Assert.assertEquals(arrayListOf("vidanta", "vidi"), Utils.getPossibleBaseWords("vidanta"))
    }
}
