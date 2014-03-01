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

import org.jopendocument.util.DecimalUtils;
import org.jopendocument.util.Tuple2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Units of length.
 * 
 * @author Sylvain CUAZ
 * @see <a href="http://www.w3.org/TR/xsl/#d0e5752">W3C Definitions</a>
 */
public enum LengthUnit {
    /**
     * The millimetre.
     */
    MM("mm", BigDecimal.ONE),
    /**
     * The centimetre.
     */
    CM("cm", BigDecimal.TEN),
    /**
     * The inch.
     */
    INCH("in", new BigDecimal("25.4")),
    /**
     * The point (1/72in).
     */
    POINT("pt", INCH.multiplier.divide(new BigDecimal(72), DecimalUtils.HIGH_PRECISION)),
    /**
     * The pica (12pt i.e. 1/6in).
     */
    PICA("pc", INCH.multiplier.divide(new BigDecimal(6), DecimalUtils.HIGH_PRECISION));

    private final String symbol;
    private final BigDecimal multiplier;

    private LengthUnit(final String abbr, BigDecimal multiplier) {
        this.symbol = abbr;
        this.multiplier = multiplier;
    }

    /**
     * The symbol for this unit of length.
     * 
     * @return the symbol, eg "cm".
     */
    public final String getSymbol() {
        return this.symbol;
    }

    /**
     * Convert from this unit to another.
     * 
     * @param d a length, eg 1.
     * @param other another unit, eg {@link #CM}.
     * @return the {@link RoundingMode#HALF_UP rounded} result, eg 2.54 if this is {@link #INCH}
     */
    public final BigDecimal convertTo(final BigDecimal d, LengthUnit other) {
        if (this == other) {
            return d;
        } else {
            return d.multiply(this.multiplier).divide(other.multiplier, DecimalUtils.HIGH_PRECISION);
        }
    }

    public final String format(final Number n) {
        if (n == null)
            throw new NullPointerException();
        // don't use exponents
        final String s = n instanceof BigDecimal ? ((BigDecimal) n).toPlainString() : n.toString();
        return s + getSymbol();
    }

    public static final LengthUnit fromSymbol(final String s) {
        for (final LengthUnit lu : values())
            if (lu.symbol.equals(s))
                return lu;
        return null;
    }

    // match all lengths in relaxNG : length, nonNegativeLength and positiveLength ; eg 15.2cm
    // if you want to tell them apart do it in java on the BigDecimal.
    private static final Pattern lenghPattern = Pattern.compile("(-?\\d+(\\.\\d+)?)(\\p{Alpha}+)?");

    // 0: value, eg 15 ; 1: unit, eg "cm" or null
    private static final String[] parseLength2String(String l) {
        final Matcher m = lenghPattern.matcher(l);
        if (!m.matches())
            throw new IllegalStateException("unable to parse " + l);
        return new String[] { m.group(1), m.group(3) };
    }

    public static final Tuple2<BigDecimal, LengthUnit> parseLength(final String l) {
        if (l == null)
            return null;
        final String[] valAndUnit = parseLength2String(l);
        final LengthUnit unit = LengthUnit.fromSymbol(valAndUnit[1]);
        if (unit == null)
            throw new IllegalStateException("unknown unit " + unit);
        return Tuple2.create(new BigDecimal(valAndUnit[0]), unit);
    }

    /**
     * Parse a length.
     * 
     * @param l the length, can be <code>null</code>, e.g. "2.0cm".
     * @param to the result unit, e.g. {@link LengthUnit#MM}.
     * @return the parsed length, <code>null</code> if <code>l</code> is, e.g. 20.
     */
    public static final BigDecimal parseLength(final String l, final LengthUnit to) {
        if (l == null)
            return null;
        final Tuple2<BigDecimal, LengthUnit> valAndUnit = LengthUnit.parseLength(l);
        return valAndUnit.get1().convertTo(valAndUnit.get0(), to);
    }

    public static final BigDecimal parsePositiveLength(final String l, final LengthUnit to, boolean strict) {
        final BigDecimal res = parseLength(l, to);
        if (res.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException(res + " < 0");
        if (strict && res.compareTo(BigDecimal.ZERO) == 0)
            throw new IllegalArgumentException(res + " == 0");
        return res;
    }
}
