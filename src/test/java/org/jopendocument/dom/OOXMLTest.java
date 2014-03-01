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

import org.jopendocument.util.JDOMUtils;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.jopendocument.dom.OOXML;
import org.jopendocument.dom.XMLVersion;

public class OOXMLTest extends TestCase {

    private static final OOXML xml = OOXML.getLast(XMLVersion.OD);

    public void testEncodeRT() {
        final Map<String, String> styles = new HashMap<String, String>();
        styles.put("b", "Gras");
        styles.put("gris", "Gris");
        final String expected = "<text:span xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\">4 &lt; <text:span text:style-name=\"Gras\">5</text:span></text:span>";
        assertEquals(expected, JDOMUtils.output(xml.encodeRT("4 < [b]5[/b]", styles)));
    }

    public void testEncodeWS() {
        final String s = "hi\thow are   you ?\n[That] was >= 3 and <=3 spaces";
        final String expected = "<text:span xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\">hi<text:tab />how are<text:s text:c=\"3\" />you ?<text:line-break />[That] was &gt;= 3 and &lt;=3 spaces</text:span>";
        assertEquals(expected, JDOMUtils.output(xml.encodeWS(s)));
    }

}
