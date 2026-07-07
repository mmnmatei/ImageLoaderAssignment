package com.example.imageloader.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class HashUtilsTest {

    @Test
    fun `md5 returns correct hash for empty string`() {
        val input = ""
        val expected = "d41d8cd98f00b204e9800998ecf8427e"
        assertEquals(expected, HashUtils.md5(input))
    }

    @Test
    fun `md5 returns correct hash for known string`() {
        val input = "https://example.com/image.jpg"
        // Expected MD5 for "https://example.com/image.jpg" is 18867d45576d8283d6fabb82406789c8
        val expected = "18867d45576d8283d6fabb82406789c8"
        assertEquals(expected, HashUtils.md5(input))
    }

    @Test
    fun `md5 is consistent for same input`() {
        val input = "test_input"
        val hash1 = HashUtils.md5(input)
        val hash2 = HashUtils.md5(input)
        assertEquals(hash1, hash2)
    }
}
