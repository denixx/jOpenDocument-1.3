/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2013 jOpenDocument, by ILM Informatique. All rights reserved.
 * 
 * The contents of this file are subject to the terms of the GNU
 * General Public License Version 3 only ("GPL").  
 * You may not use this file except in compliance with the License. 
 * You can obtain a copy of the License at http://www.gnu.org/licenses/gpl-3.0.html
 * See the License for the specific language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each file.
 * 
 */

package org.jopendocument.dom;

import org.jopendocument.util.Tuple2;

import java.math.BigDecimal;

import junit.framework.TestCase;
import org.jopendocument.dom.LengthUnit;

public class LengthUnitTest extends TestCase {

    public void testParseLength() throws Exception {
        assertEquals(Tuple2.create(new BigDecimal("15.2"), LengthUnit.CM), LengthUnit.parseLength("15.2cm"));
        testPositiveLength("-15.2cm", true);
        testPositiveLength("-15.2cm", false);
        testPositiveLength("-0cm", true);
        testPositiveLength("0cm", true);
        // positive or zero
        assertEquals(BigDecimal.ZERO, LengthUnit.parsePositiveLength("0cm", LengthUnit.MM, false).stripTrailingZeros());
        // whatever the unit (use compareTo() to compare the value and not some other attribute)
        assertEquals(BigDecimal.ZERO.compareTo(LengthUnit.parsePositiveLength("0cm", LengthUnit.INCH, false)), 0);
        assertEquals(new BigDecimal("152").compareTo(LengthUnit.parsePositiveLength("15.2cm", LengthUnit.MM, true)), 0);

        assertEquals(new BigDecimal("152"), LengthUnit.parseLength("15.2cm", LengthUnit.MM).stripTrailingZeros());
        assertEquals(new BigDecimal("1.52"), LengthUnit.parseLength("15.2mm", LengthUnit.CM).stripTrailingZeros());
        assertEquals(new BigDecimal("2.54"), LengthUnit.parseLength("1in", LengthUnit.CM).stripTrailingZeros());
        assertEquals(new BigDecimal("1"), LengthUnit.parseLength("72pt", LengthUnit.INCH).stripTrailingZeros());
        assertEquals(new BigDecimal("2"), LengthUnit.parseLength("12pc", LengthUnit.INCH).stripTrailingZeros());
    }

    private void testPositiveLength(String l, boolean strict) {
        try {
            LengthUnit.parsePositiveLength(l, LengthUnit.CM, strict);
            fail("A length is positive");
        } catch (Exception e) {
            // ok
        }
    }

    public void testFormat() throws Exception {
        // assert that we use a plain string without exponent
        final BigDecimal bigDec = new BigDecimal("123.45678909E+10");
        assertTrue(bigDec.toString().endsWith("E+12"));
        assertEquals("1234567890900cm", LengthUnit.CM.format(bigDec));
        // assert that we don't use BigDecimal internally (otherwise we would have the exact
        // approximation of 2.357f, e.g. 2.3570001125335693)
        assertEquals("2.357cm", LengthUnit.CM.format(2.357f));
    }
}
