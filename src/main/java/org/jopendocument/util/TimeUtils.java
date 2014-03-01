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

package org.jopendocument.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

public class TimeUtils {
    static private DatatypeFactory typeFactory = null;

    static public final DatatypeFactory getTypeFactory() {
        if (typeFactory == null)
            try {
                typeFactory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException e) {
                throw new IllegalStateException(e);
            }
        return typeFactory;
    }

    /**
     * Get non-null seconds with the the correct class.
     * 
     * @param d a duration.
     * @return the seconds, never <code>null</code>.
     * @see Duration#getField(javax.xml.datatype.DatatypeConstants.Field)
     * @see Duration#getMinutes()
     */
    static public final BigDecimal getSeconds(final Duration d) {
        final BigDecimal res = (BigDecimal) d.getField(DatatypeConstants.SECONDS);
        return res != null ? res : BigDecimal.ZERO;
    }

    /**
     * Convert the time part of a calendar to a duration.
     * 
     * @param cal a calendar, e.g. 23/12/2011 11:55:33.066 GMT+02.
     * @return a duration, e.g. P0Y0M0DT11H55M33.066S.
     */
    public final static Duration timePartToDuration(final Calendar cal) {
        final BigDecimal seconds = BigDecimal.valueOf(cal.get(Calendar.SECOND)).add(BigDecimal.valueOf(cal.get(Calendar.MILLISECOND)).movePointLeft(3));
        return getTypeFactory().newDuration(true, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.valueOf(cal.get(Calendar.HOUR_OF_DAY)), BigInteger.valueOf(cal.get(Calendar.MINUTE)),
                seconds);
    }

    /**
     * Normalize <code>cal</code> so that any Calendar with the same local time have the same
     * result. If you don't need a Calendar this is faster than
     * {@link #copyLocalTime(Calendar, Calendar)}.
     * 
     * @param cal a calendar, e.g. 0:00 CEST.
     * @return the time in millisecond of the UTC calendar with the same local time, e.g. 0:00 UTC.
     */
    public final static long normalizeLocalTime(final Calendar cal) {
        return cal.getTimeInMillis() + cal.getTimeZone().getOffset(cal.getTimeInMillis());
    }

    /**
     * Copy the local time from one calendar to another. Except if both calendars have the same time
     * zone, from.getTimeInMillis() will be different from to.getTimeInMillis().
     * 
     * @param from the source calendar, e.g. 23/12/2011 11:55:33.066 GMT-12.
     * @param to the destination calendar, e.g. 01/01/2000 0:00 GMT+13.
     * @return the modified destination calendar, e.g. 23/12/2011 11:55:33.066 GMT+13.
     */
    public final static Calendar copyLocalTime(final Calendar from, final Calendar to) {
        to.clear();
        for (final int field : new int[] { Calendar.YEAR, Calendar.DAY_OF_YEAR, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND }) {
            to.set(field, from.get(field));
        }
        return to;
    }
}
