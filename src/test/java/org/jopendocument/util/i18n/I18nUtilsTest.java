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

package org.jopendocument.util.i18n;

import java.util.Arrays;
import java.util.Locale;

import junit.framework.TestCase;
import org.jopendocument.util.i18n.I18nUtils;

public class I18nUtilsTest extends TestCase {

    public void testFromString() throws Exception {
        for (final Locale l : Arrays.asList(Locale.FRENCH, Locale.FRANCE, Locale.CANADA, Locale.CANADA_FRENCH, Locale.ROOT, Locale.TRADITIONAL_CHINESE)) {
            assertEquals(l, I18nUtils.createLocaleFromString(l.toString()));
        }
    }
}
