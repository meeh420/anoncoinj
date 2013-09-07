/**
 * Copyright 2011 Thilo Planz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.anoncoin.core;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

import static com.google.anoncoin.core.Utils.*;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UtilsTest {

    @Test
    public void testToNanoCoins() {
        // String version
        assertEquals(CENT, toNanoCoins("0.01"));
        assertEquals(CENT, toNanoCoins("1E-2"));
        assertEquals(COIN.add(Utils.CENT), toNanoCoins("1.01"));
        try {
            toNanoCoins("2E-20");
            org.junit.Assert.fail("should not have accepted fractional nanocoins");
        } catch (ArithmeticException e) {
        }

        // int version
        assertEquals(CENT, toNanoCoins(0, 1));

        // TODO: should this really pass?
        assertEquals(COIN.subtract(CENT), toNanoCoins(1, -1));
        assertEquals(COIN.negate(), toNanoCoins(-1, 0));
        assertEquals(COIN.negate(), toNanoCoins("-1"));
    }

    @Test
    public void testFormatting() {
        assertEquals("1.00", anoncoinValueToFriendlyString(toNanoCoins(1, 0)));
        assertEquals("1.23", anoncoinValueToFriendlyString(toNanoCoins(1, 23)));
        assertEquals("0.001", anoncoinValueToFriendlyString(BigInteger.valueOf(COIN.longValue() / 1000)));
        assertEquals("-1.23", anoncoinValueToFriendlyString(toNanoCoins(1, 23).negate()));
    }
    
    /**
     * Test the anoncoinValueToPlainString amount formatter
     */
    @Test
    public void testAnoncoinValueToPlainString() {
        // null argument check
        try {
            anoncoinValueToPlainString(null);
            org.junit.Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Value cannot be null"));
        }

        assertEquals("0.0015", anoncoinValueToPlainString(BigInteger.valueOf(150000)));
        assertEquals("1.23", anoncoinValueToPlainString(toNanoCoins("1.23")));
        assertEquals("-1.23", anoncoinValueToPlainString(toNanoCoins("-1.23")));
        
        assertEquals("0.1", anoncoinValueToPlainString(toNanoCoins("0.1")));
        assertEquals("1.1", anoncoinValueToPlainString(toNanoCoins("1.1")));
        assertEquals("21.12", anoncoinValueToPlainString(toNanoCoins("21.12")));
        assertEquals("321.123", anoncoinValueToPlainString(toNanoCoins("321.123")));
        assertEquals("4321.1234", anoncoinValueToPlainString(toNanoCoins("4321.1234")));
        assertEquals("54321.12345", anoncoinValueToPlainString(toNanoCoins("54321.12345")));
        assertEquals("654321.123456", anoncoinValueToPlainString(toNanoCoins("654321.123456")));
        assertEquals("7654321.1234567", anoncoinValueToPlainString(toNanoCoins("7654321.1234567")));
        assertEquals("87654321.12345678", anoncoinValueToPlainString(toNanoCoins("87654321.12345678")));

        // check there are no trailing zeros
        assertEquals("1", anoncoinValueToPlainString(toNanoCoins("1.0")));
        assertEquals("2", anoncoinValueToPlainString(toNanoCoins("2.00")));
        assertEquals("3", anoncoinValueToPlainString(toNanoCoins("3.000")));
        assertEquals("4", anoncoinValueToPlainString(toNanoCoins("4.0000")));
        assertEquals("5", anoncoinValueToPlainString(toNanoCoins("5.00000")));
        assertEquals("6", anoncoinValueToPlainString(toNanoCoins("6.000000")));
        assertEquals("7", anoncoinValueToPlainString(toNanoCoins("7.0000000")));
        assertEquals("8", anoncoinValueToPlainString(toNanoCoins("8.00000000")));
    }    
    
    @Test
    public void testReverseBytes() {
        Assert.assertArrayEquals(new byte[] {1,2,3,4,5}, Utils.reverseBytes(new byte[] {5,4,3,2,1}));
    }

    @Test
    public void testReverseDwordBytes() {
        Assert.assertArrayEquals(new byte[] {1,2,3,4,5,6,7,8}, Utils.reverseDwordBytes(new byte[] {4,3,2,1,8,7,6,5}, -1));
        Assert.assertArrayEquals(new byte[] {1,2,3,4}, Utils.reverseDwordBytes(new byte[] {4,3,2,1,8,7,6,5}, 4));
        Assert.assertArrayEquals(new byte[0], Utils.reverseDwordBytes(new byte[] {4,3,2,1,8,7,6,5}, 0));
        Assert.assertArrayEquals(new byte[0], Utils.reverseDwordBytes(new byte[0], 0));
    }
}
