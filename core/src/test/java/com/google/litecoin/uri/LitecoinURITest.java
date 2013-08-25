/*
 * Copyright 2012 the original author or authors.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.google.litecoin.uri;

import com.google.litecoin.core.Address;
import com.google.litecoin.core.AddressFormatException;
import com.google.litecoin.core.NetworkParameters;
import com.google.litecoin.core.Utils;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

public class LitecoinURITest {

    private LitecoinURI testObject = null;

    private static final String PRODNET_GOOD_ADDRESS = "LQz2pJYaeqntA9BFB8rDX5AL2TTKGd5AuN";

    /**
     * Tests conversion to Litecoin URI
     * 
     * @throws LitecoinURIParseException
     *             If something goes wrong
     * @throws AddressFormatException 
     */
    @Test
    public void testConvertToLitecoinURI() throws Exception {
        Address goodAddress = new Address(NetworkParameters.prodNet(), PRODNET_GOOD_ADDRESS);
        
        // simple example
        assertEquals("litecoin:" + PRODNET_GOOD_ADDRESS + "?amount=12.34&label=Hello&message=AMessage", LitecoinURI.convertToLitecoinURI(goodAddress, Utils.toNanoCoins("12.34"), "Hello", "AMessage"));
        
        // example with spaces, ampersand and plus
        assertEquals("litecoin:" + PRODNET_GOOD_ADDRESS + "?amount=12.34&label=Hello%20World&message=Mess%20%26%20age%20%2B%20hope", LitecoinURI.convertToLitecoinURI(goodAddress, Utils.toNanoCoins("12.34"), "Hello World", "Mess & age + hope"));

        // amount negative
        try {
            LitecoinURI.convertToLitecoinURI(goodAddress, Utils.toNanoCoins("-0.1"), "hope", "glory");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Amount must be positive"));
        }

        // no amount, label present, message present
        assertEquals("litecoin:" + PRODNET_GOOD_ADDRESS + "?label=Hello&message=glory", LitecoinURI.convertToLitecoinURI(goodAddress, null, "Hello", "glory"));
        
        // amount present, no label, message present
        assertEquals("litecoin:" + PRODNET_GOOD_ADDRESS + "?amount=0.1&message=glory", LitecoinURI.convertToLitecoinURI(goodAddress, Utils.toNanoCoins("0.1"), null, "glory"));
        assertEquals("litecoin:" + PRODNET_GOOD_ADDRESS + "?amount=0.1&message=glory", LitecoinURI.convertToLitecoinURI(goodAddress, Utils.toNanoCoins("0.1"), "", "glory"));

        // amount present, label present, no message
        assertEquals("litecoin:" + PRODNET_GOOD_ADDRESS + "?amount=12.34&label=Hello", LitecoinURI.convertToLitecoinURI(goodAddress,Utils.toNanoCoins("12.34"), "Hello", null));
        assertEquals("litecoin:" + PRODNET_GOOD_ADDRESS + "?amount=12.34&label=Hello", LitecoinURI.convertToLitecoinURI(goodAddress, Utils.toNanoCoins("12.34"), "Hello", ""));
              
        // amount present, no label, no message
        assertEquals("litecoin:" + PRODNET_GOOD_ADDRESS + "?amount=1000", LitecoinURI.convertToLitecoinURI(goodAddress, Utils.toNanoCoins("1000"), null, null));
        assertEquals("litecoin:" + PRODNET_GOOD_ADDRESS + "?amount=1000", LitecoinURI.convertToLitecoinURI(goodAddress, Utils.toNanoCoins("1000"), "", ""));
        
        // no amount, label present, no message
        assertEquals("litecoin:" + PRODNET_GOOD_ADDRESS + "?label=Hello", LitecoinURI.convertToLitecoinURI(goodAddress, null, "Hello", null));
        
        // no amount, no label, message present
        assertEquals("litecoin:" + PRODNET_GOOD_ADDRESS + "?message=Agatha", LitecoinURI.convertToLitecoinURI(goodAddress, null, null, "Agatha"));
        assertEquals("litecoin:" + PRODNET_GOOD_ADDRESS + "?message=Agatha", LitecoinURI.convertToLitecoinURI(goodAddress, null, "", "Agatha"));
      
        // no amount, no label, no message
        assertEquals("litecoin:" + PRODNET_GOOD_ADDRESS, LitecoinURI.convertToLitecoinURI(goodAddress, null, null, null));
        assertEquals("litecoin:" + PRODNET_GOOD_ADDRESS, LitecoinURI.convertToLitecoinURI(goodAddress, null, "", ""));
    }

    /**
     * Test the simplest well-formed URI
     * 
     * @throws LitecoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Simple() throws LitecoinURIParseException {
        testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS);
        assertNotNull(testObject);
        assertNull("Unexpected amount", testObject.getAmount());
        assertNull("Unexpected label", testObject.getLabel());
        assertEquals("Unexpected label", 20, testObject.getAddress().getHash160().length);
    }

    /**
     * Test a broken URI (bad scheme)
     */
    @Test
    public void testBad_Scheme() {
        try {
            testObject = new LitecoinURI(NetworkParameters.prodNet(), "blimpcoin:" + PRODNET_GOOD_ADDRESS);
            fail("Expecting LitecoinURIParseException");
        } catch (LitecoinURIParseException e) {
        }
    }

    /**
     * Test a broken URI (bad syntax)
     */
    @Test
    public void testBad_BadSyntax() {
        // Various illegal characters
        try {
            testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + "|" + PRODNET_GOOD_ADDRESS);
            fail("Expecting LitecoinURIParseException");
        } catch (LitecoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }

        try {
            testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS + "\\");
            fail("Expecting LitecoinURIParseException");
        } catch (LitecoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }

        // Separator without field
        try {
            testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":");
            fail("Expecting LitecoinURIParseException");
        } catch (LitecoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }
    }

    /**
     * Test a broken URI (missing address)
     */
    @Test
    public void testBad_Address() {
        try {
            testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME);
            fail("Expecting LitecoinURIParseException");
        } catch (LitecoinURIParseException e) {
        }
    }

    /**
     * Test a broken URI (bad address type)
     */
    @Test
    public void testBad_IncorrectAddressType() {
        try {
            testObject = new LitecoinURI(NetworkParameters.testNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS);
            fail("Expecting LitecoinURIParseException");
        } catch (LitecoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad address"));
        }
    }

    /**
     * Handles a simple amount
     * 
     * @throws LitecoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Amount() throws LitecoinURIParseException {
        // Test the decimal parsing
        testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                + "?amount=9876543210.12345678");
        assertEquals("987654321012345678", testObject.getAmount().toString());

        // Test the decimal parsing
        testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                + "?amount=.12345678");
        assertEquals("12345678", testObject.getAmount().toString());

        // Test the integer parsing
        testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                + "?amount=9876543210");
        assertEquals("987654321000000000", testObject.getAmount().toString());
    }

    /**
     * Handles a simple label
     * 
     * @throws LitecoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Label() throws LitecoinURIParseException {
        testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                + "?label=Hello%20World");
        assertEquals("Hello World", testObject.getLabel());
    }

    /**
     * Handles a simple label with an embedded ampersand and plus
     * 
     * @throws LitecoinURIParseException
     *             If something goes wrong
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testGood_LabelWithAmpersandAndPlus() throws LitecoinURIParseException, UnsupportedEncodingException {
        String testString = "Hello Earth & Mars + Venus";
        String encodedLabel = LitecoinURI.encodeURLString(testString);
        testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS + "?label="
                + encodedLabel);
        assertEquals(testString, testObject.getLabel());
    }

    /**
     * Handles a Russian label (Unicode test)
     * 
     * @throws LitecoinURIParseException
     *             If something goes wrong
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testGood_LabelWithRussian() throws LitecoinURIParseException, UnsupportedEncodingException {
        // Moscow in Russian in Cyrillic
        String moscowString = "\u041c\u043e\u0441\u043a\u0432\u0430";
        String encodedLabel = LitecoinURI.encodeURLString(moscowString);
        testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS + "?label="
                + encodedLabel);
        assertEquals(moscowString, testObject.getLabel());
    }

    /**
     * Handles a simple message
     * 
     * @throws LitecoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Message() throws LitecoinURIParseException {
        testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                + "?message=Hello%20World");
        assertEquals("Hello World", testObject.getMessage());
    }

    /**
     * Handles various well-formed combinations
     * 
     * @throws LitecoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Combinations() throws LitecoinURIParseException {
        testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                + "?amount=9876543210&label=Hello%20World&message=Be%20well");
        assertEquals(
                "LitecoinURI['address'='LQz2pJYaeqntA9BFB8rDX5AL2TTKGd5AuN','amount'='987654321000000000','label'='Hello World','message'='Be well']",
                testObject.toString());
    }

    /**
     * Handles a badly formatted amount field
     * 
     * @throws LitecoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Amount() throws LitecoinURIParseException {
        // Missing
        try {
            testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?amount=");
            fail("Expecting LitecoinURIParseException");
        } catch (LitecoinURIParseException e) {
            assertTrue(e.getMessage().contains("amount"));
        }

        // Non-decimal (BIP 21)
        try {
            testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?amount=12X4");
            fail("Expecting LitecoinURIParseException");
        } catch (LitecoinURIParseException e) {
            assertTrue(e.getMessage().contains("amount"));
        }
    }

    /**
     * Handles a badly formatted label field
     * 
     * @throws LitecoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Label() throws LitecoinURIParseException {
        try {
            testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?label=");
            fail("Expecting LitecoinURIParseException");
        } catch (LitecoinURIParseException e) {
            assertTrue(e.getMessage().contains("label"));
        }
    }

    /**
     * Handles a badly formatted message field
     * 
     * @throws LitecoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Message() throws LitecoinURIParseException {
        try {
            testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?message=");
            fail("Expecting LitecoinURIParseException");
        } catch (LitecoinURIParseException e) {
            assertTrue(e.getMessage().contains("message"));
        }
    }

    /**
     * Handles duplicated fields (sneaky address overwrite attack)
     * 
     * @throws LitecoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Duplicated() throws LitecoinURIParseException {
        try {
            testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?address=aardvark");
            fail("Expecting LitecoinURIParseException");
        } catch (LitecoinURIParseException e) {
            assertTrue(e.getMessage().contains("address"));
        }
    }

    /**
     * Handles case when there are too many equals
     * 
     * @throws LitecoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_TooManyEquals() throws LitecoinURIParseException {
        try {
            testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?label=aardvark=zebra");
            fail("Expecting LitecoinURIParseException");
        } catch (LitecoinURIParseException e) {
            assertTrue(e.getMessage().contains("cannot parse name value pair"));
        }
    }

    /**
     * Handles case when there are too many question marks
     * 
     * @throws LitecoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_TooManyQuestionMarks() throws LitecoinURIParseException {
        try {
            testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?label=aardvark?message=zebra");
            fail("Expecting LitecoinURIParseException");
        } catch (LitecoinURIParseException e) {
            assertTrue(e.getMessage().contains("Too many question marks"));
        }
    }
    
    /**
     * Handles unknown fields (required and not required)
     * 
     * @throws LitecoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testUnknown() throws LitecoinURIParseException {
        // Unknown not required field
        testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                + "?aardvark=true");
        assertEquals("LitecoinURI['address'='LQz2pJYaeqntA9BFB8rDX5AL2TTKGd5AuN','aardvark'='true']", testObject.toString());

        assertEquals("true", (String) testObject.getParameterByName("aardvark"));

        // Unknown not required field (isolated)
        try {
            testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?aardvark");
            fail("Expecting LitecoinURIParseException");
        } catch (LitecoinURIParseException e) {
            assertTrue(e.getMessage().contains("cannot parse name value pair"));
        }

        // Unknown and required field
        try {
            testObject = new LitecoinURI(NetworkParameters.prodNet(), LitecoinURI.LITECOIN_SCHEME + ":" + PRODNET_GOOD_ADDRESS
                    + "?req-aardvark=true");
            fail("Expecting LitecoinURIParseException");
        } catch (LitecoinURIParseException e) {
            assertTrue(e.getMessage().contains("req-aardvark"));
        }
    }

    @Test
    public void brokenURIs() throws LitecoinURIParseException {
        // Check we can parse the incorrectly formatted URIs produced by blockchain.info and its iPhone app.
        String str = "litecoin://1KzTSfqjF2iKCduwz59nv2uqh1W2JsTxZH?amount=0.01000000";
        LitecoinURI uri = new LitecoinURI(str);
        assertEquals("1KzTSfqjF2iKCduwz59nv2uqh1W2JsTxZH", uri.getAddress().toString());
        assertEquals(Utils.toNanoCoins(0, 1), uri.getAmount());
    }
}
