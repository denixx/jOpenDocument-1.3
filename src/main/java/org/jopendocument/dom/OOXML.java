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

/*
 * Créé le 28 oct. 2004
 */
package org.jopendocument.dom;

import org.jopendocument.util.CollectionUtils;
import org.jopendocument.util.JDOMUtils;
import org.jopendocument.util.Validator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Parent;
import org.jdom.Text;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;

/**
 * Various bits of OpenDocument XML.
 * 
 * @author Sylvain CUAZ
 * @see #get(XMLFormatVersion)
 */
public abstract class OOXML implements Comparable<OOXML> {

    /**
     * If this system property is set to <code>true</code> then {@link #get(XMLFormatVersion)} will
     * never return <code>null</code>, allowing to support unknown versions.
     */
    public static final String LAST_FOR_UNKNOWN_PROP = OOXML.class.getPackage().getName() + ".lastOOXMLForUnknownVersion";
    private static final XML_OO instanceOO = new XML_OO();
    private static final SortedMap<String, XML_OD> instancesODByDate = new TreeMap<String, XML_OD>();
    private static final Map<String, XML_OD> instancesODByVersion = new HashMap<String, XML_OD>();
    private static final List<OOXML> values;
    private static OOXML defaultInstance;
    private static final Pattern WHITE_SPACE_TO_ENCODE = Pattern.compile("\n|\t| {2,}");

    static {
        register(new XML_OD_1_0());
        register(new XML_OD_1_1());
        register(new XML_OD_1_2());
        values = new ArrayList<OOXML>(instancesODByDate.size() + 1);
        values.add(instanceOO);
        values.addAll(instancesODByDate.values());

        setDefault(getLast());
    }

    private static void register(XML_OD xml) {
        assert xml.getVersion() == XMLVersion.OD;
        instancesODByDate.put(xml.getDateString(), xml);
        instancesODByVersion.put(xml.getFormatVersion().getOfficeVersion(), xml);
    }

    /**
     * Returns the instance that match the requested version.
     * 
     * @param version the version.
     * @return the corresponding instance, <code>null</code> for unsupported versions.
     * @see #LAST_FOR_UNKNOWN_PROP
     */
    public static OOXML get(XMLFormatVersion version) {
        return get(version, Boolean.getBoolean(LAST_FOR_UNKNOWN_PROP));
    }

    public static OOXML get(XMLFormatVersion version, final boolean lastForUnknown) {
        if (version.getXMLVersion() == XMLVersion.OOo) {
            return instanceOO;
        } else {
            final XML_OD res = instancesODByVersion.get(version.getOfficeVersion());
            if (res == null && lastForUnknown)
                return getLast(version.getXMLVersion());
            else
                return res;
        }
    }

    public static OOXML get(Element root) {
        return XMLFormatVersion.get(root).getXML();
    }

    /**
     * Return all known instances in the order they were published.
     * 
     * @return all known instances ordered.
     * @see #compareTo(OOXML)
     */
    static public final List<OOXML> values() {
        return values;
    }

    static public final OOXML getLast() {
        return CollectionUtils.getLast(values);
    }

    static public final OOXML getLast(XMLVersion version) {
        if (version == XMLVersion.OOo)
            return instanceOO;
        else
            return instancesODByDate.get(instancesODByDate.lastKey());
    }

    public static void setDefault(OOXML ns) {
        defaultInstance = ns;
    }

    public static OOXML getDefault() {
        return defaultInstance;
    }

    static private final String rt2oo(String content, String tagName, String styleName) {
        return content.replaceAll("\\[" + tagName + "\\]", "<text:span text:style-name=\"" + styleName + "\">").replaceAll("\\[/" + tagName + "\\]", "</text:span>");
    }

    // from OpenDocument-v1.2-schema.rng : a coordinate is a length
    static private final BigDecimal parseCoordinate(final Element elem, final String attrName, final Namespace ns, LengthUnit unit) {
        return parseLength(elem, attrName, ns, unit);
    }

    static private final BigDecimal parseLength(final Element elem, final String attrName, final Namespace ns, LengthUnit unit) {
        final String attr = elem.getAttributeValue(attrName, ns);
        if (attr == null)
            return null;
        return LengthUnit.parseLength(attr, unit);
    }

    // *** instances

    private final XMLFormatVersion version;
    private final String dateString;

    private OOXML(XMLFormatVersion version, final String dateString) {
        this.version = version;
        this.dateString = dateString;
    }

    /**
     * The date the specification was published.
     * 
     * @return the date in "yyyyMMdd" format.
     */
    public final String getDateString() {
        return this.dateString;
    }

    /**
     * Compare the date the specification was published.
     * 
     * @param o the object to be compared.
     * @see #getDateString()
     */
    @Override
    public int compareTo(OOXML o) {
        return this.dateString.compareTo(o.dateString);
    }

    public final XMLVersion getVersion() {
        return this.getFormatVersion().getXMLVersion();
    }

    public final XMLFormatVersion getFormatVersion() {
        return this.version;
    }

    public abstract boolean canValidate();

    /**
     * Verify that the passed document is a valid OpenOffice.org 1 or ODF document.
     * 
     * @param doc the xml to test.
     * @return a validator on <code>doc</code>.
     */
    public abstract Validator getValidator(Document doc);

    /**
     * Return the names of font face declarations.
     * 
     * @return at index 0 the name of the container element, at 1 the qualified name of its
     *         children.
     */
    public abstract String[] getFontDecls();

    /**
     * Return the top-level script element in the content.
     * 
     * @return the top-level script element name.
     */
    public abstract String getOfficeScripts();

    /**
     * The name of the elements where scripts are defined.
     * 
     * @return the name of the children of {@link #getOfficeScripts()} defining scripts.
     */
    public abstract String getOfficeScript();

    /**
     * The name of the element where event listeners are defined.
     * 
     * @return the name of the child of {@link #getOfficeScripts()} defining event listeners.
     */
    public abstract String getOfficeEventListeners();

    public abstract String getEventListener();

    public final Element getLineBreak() {
        return new Element("line-break", getVersion().getTEXT());
    }

    public abstract Element getTab();

    public abstract String getFrameQName();

    public abstract Element createFormattingProperties(final String family);

    protected final List encodeRT_L(String content, Map<String, String> styles) {
        String res = JDOMUtils.OUTPUTTER.escapeElementEntities(content);
        for (final Entry<String, String> e : styles.entrySet()) {
            res = rt2oo(res, e.getKey(), e.getValue());
        }
        try {
            return JDOMUtils.parseString(res, getVersion().getALL());
        } catch (JDOMException e) {
            // should not happpen as we did escapeElementEntities which gives valid xml and then
            // rt2oo which introduce only static xml
            throw new IllegalStateException("could not parse " + res, e);
        }
    }

    /**
     * Convert rich text (with [] tags) into XML.
     * 
     * @param content the string to convert, eg "texte [b]gras[/b]".
     * @param styles the mapping from tagname (eg "b") to the name of the character style (eg
     *        "Gras").
     * @return the corresponding element.
     */
    public final Element encodeRT(String content, Map<String, String> styles) {
        return new Element("span", getVersion().getTEXT()).addContent(encodeRT_L(content, styles));
    }

    // create the necessary <text:s c="n"/>
    private Element createSpaces(String spacesS) {
        return new Element("s", getVersion().getTEXT()).setAttribute("c", spacesS.length() + "", getVersion().getTEXT());
    }

    /**
     * Encode a String to OO XML. Handles substition of whitespaces to their OO equivalent.
     * 
     * @param s a plain ole String, eg "term\tdefinition".
     * @return an Element suitable to be inserted in an OO XML document, eg
     * 
     *         <pre>
     *     &lt;text:span&gt;term&lt;text:tab-stop/&gt;definition&lt;/text:span&gt;
     * </pre>
     * 
     *         .
     */
    public final Element encodeWS(final String s) {
        return new Element("span", getVersion().getTEXT()).setContent(encodeWSasList(s));
    }

    public final List<Content> encodeWSasList(final String s) {
        final List<Content> res = new ArrayList<Content>();
        final Matcher m = WHITE_SPACE_TO_ENCODE.matcher(s);
        int last = 0;
        while (m.find()) {
            res.add(new Text(s.substring(last, m.start())));
            switch (m.group().charAt(0)) {
            case '\n':
                res.add(getLineBreak());
                break;
            case '\t':
                res.add(getTab());
                break;
            case ' ':
                res.add(createSpaces(m.group()));
                break;

            default:
                throw new IllegalStateException("unknown item: " + m.group());
            }
            last = m.end();
        }
        res.add(new Text(s.substring(last)));
        return res;
    }

    @SuppressWarnings("unchecked")
    public final void encodeWS(final Text t) {
        final Parent parent = t.getParent();
        final int ind = parent.indexOf(t);
        t.detach();
        parent.getContent().addAll(ind, encodeWSasList(t.getText()));
    }

    @SuppressWarnings("unchecked")
    public final Element encodeWS(final Element elem) {
        final XPath path;
        try {
            path = OOUtils.getXPath(".//text()", getVersion());
        } catch (JDOMException e) {
            // static path, hence always valid
            throw new IllegalStateException("cannot create XPath", e);
        }
        try {
            final Iterator iter = new ArrayList(path.selectNodes(elem)).iterator();
            while (iter.hasNext()) {
                final Text t = (Text) iter.next();
                encodeWS(t);
            }
        } catch (JDOMException e) {
            throw new IllegalArgumentException("cannot find text nodes of " + elem, e);
        }
        return elem;
    }

    /**
     * Return the coordinates of the top-left and bottom-right of the passed shape.
     * 
     * @param elem an XML element.
     * @param unit the unit of the returned numbers.
     * @return an array of 4 numbers, <code>null</code> if <code>elem</code> is not a shape, numbers
     *         themselves are never <code>null</code>.
     */
    public final BigDecimal[] getCoordinates(Element elem, LengthUnit unit) {
        return this.getCoordinates(elem, unit, true, true);
    }

    /**
     * Return the coordinates of the top-left and bottom-right of the passed shape.
     * 
     * @param elem an XML element.
     * @param unit the unit of the returned numbers.
     * @param horizontal <code>true</code> if the x coordinates should be computed,
     *        <code>false</code> meaning items 0 and 2 of the result are <code>null</code>.
     * @param vertical <code>true</code> if the y coordinates should be computed, <code>false</code>
     *        meaning items 1 and 3 of the result are <code>null</code>.
     * @return an array of 4 numbers, <code>null</code> if <code>elem</code> is not a shape, numbers
     *         themselves are only <code>null</code> if requested with <code>horizontal</code> or
     *         <code>vertical</code>.
     */
    public final BigDecimal[] getCoordinates(Element elem, LengthUnit unit, final boolean horizontal, final boolean vertical) {
        return getCoordinates(elem, getVersion().getNS("svg"), unit, horizontal, vertical);
    }

    static private final BigDecimal[] getCoordinates(Element elem, final Namespace svgNS, LengthUnit unit, final boolean horizontal, final boolean vertical) {
        if (elem.getName().equals("g") && elem.getNamespacePrefix().equals("draw")) {
            // put below if to allow null to be returned by getLocalCoordinates() if elem isn't a
            // shape
            if (!horizontal && !vertical)
                return new BigDecimal[] { null, null, null, null };

            // an OpenDocument group (of shapes) doesn't have any coordinates nor any width and
            // height so iterate through its components to find its coordinates
            BigDecimal minX = null, minY = null;
            BigDecimal maxX = null, maxY = null;
            for (final Object c : elem.getChildren()) {
                final Element child = (Element) c;
                final BigDecimal[] childCoord = getCoordinates(child, svgNS, unit, horizontal, vertical);
                // e.g. <office:event-listeners>, <svg:desc>, <svg:title>
                if (childCoord != null) {
                    {
                        final BigDecimal x = childCoord[0];
                        final BigDecimal x2 = childCoord[2];
                        if (x != null) {
                            assert x2 != null;
                            if (minX == null || x.compareTo(minX) < 0)
                                minX = x;
                            if (maxX == null || x2.compareTo(maxX) > 0)
                                maxX = x2;
                        }
                    }
                    {
                        final BigDecimal y = childCoord[1];
                        final BigDecimal y2 = childCoord[3];
                        if (y != null) {
                            assert y2 != null;
                            if (minY == null || y.compareTo(minY) < 0)
                                minY = y;
                            if (maxY == null || y2.compareTo(maxY) > 0)
                                maxY = y2;
                        }
                    }
                }
            }
            // works because we check above if both horizontal and vertical are false
            if (minX == null && minY == null)
                throw new IllegalArgumentException("Empty group : " + JDOMUtils.output(elem));
            return new BigDecimal[] { minX, minY, maxX, maxY };
        } else {
            return getLocalCoordinates(elem, svgNS, unit, horizontal, vertical);
        }
    }

    // return null if elem isn't a shape (no x/y or no width/height)
    // BigDecimal null if and only if horizontal/vertical is false
    static private final BigDecimal[] getLocalCoordinates(Element elem, final Namespace svgNS, LengthUnit unit, final boolean horizontal, final boolean vertical) {
        final BigDecimal x = parseCoordinate(elem, "x", svgNS, unit);
        final BigDecimal x1 = parseCoordinate(elem, "x1", svgNS, unit);
        if (x == null && x1 == null)
            return null;

        final BigDecimal y = parseCoordinate(elem, "y", svgNS, unit);
        final BigDecimal y1 = parseCoordinate(elem, "y1", svgNS, unit);
        if (y == null && y1 == null)
            throw new IllegalArgumentException("Have x but missing y in " + JDOMUtils.output(elem));

        final BigDecimal startX;
        final BigDecimal endX;
        if (horizontal) {
            if (x == null) {
                startX = x1;
                endX = parseCoordinate(elem, "x2", svgNS, unit);
            } else {
                startX = x;
                final BigDecimal width = parseLength(elem, "width", svgNS, unit);
                endX = width == null ? null : startX.add(width);
            }
            // return null if there's no second coordinate (it's a point)
            if (endX == null)
                return null;
        } else {
            startX = null;
            endX = null;
        }

        final BigDecimal startY;
        final BigDecimal endY;
        if (vertical) {
            if (y == null) {
                startY = y1;
                endY = parseCoordinate(elem, "y2", svgNS, unit);
            } else {
                startY = y;
                final BigDecimal height = parseLength(elem, "height", svgNS, unit);
                endY = height == null ? null : startY.add(height);
            }
            // return null if there's no second coordinate (it's a point)
            if (endY == null)
                return null;
        } else {
            startY = null;
            endY = null;
        }

        return new BigDecimal[] { startX, startY, endX, endY };
    }

    private static final class XML_OO extends OOXML {
        public XML_OO() {
            super(XMLFormatVersion.getOOo(), "20020501");
        }

        @Override
        public boolean canValidate() {
            return true;
        }

        @Override
        public Validator getValidator(Document doc) {
            // DTDs are stubborn, xmlns have to be exactly where they want
            // in this case the root element
            for (final Namespace n : getVersion().getALL())
                doc.getRootElement().addNamespaceDeclaration(n);
            return new Validator.DTDValidator(doc, OOUtils.getBuilderLoadDTD());
        }

        @Override
        public String getOfficeScripts() {
            return "script";
        }

        @Override
        public String getOfficeScript() {
            return "script-data";
        }

        @Override
        public String getOfficeEventListeners() {
            return "events";
        }

        @Override
        public String getEventListener() {
            return "event";
        }

        @Override
        public String[] getFontDecls() {
            return new String[] { "font-decls", "style:font-decl" };
        }

        @Override
        public final Element getTab() {
            return new Element("tab-stop", getVersion().getTEXT());
        }

        @Override
        public String getFrameQName() {
            return "draw:text-box";
        }

        @Override
        public Element createFormattingProperties(String family) {
            return new Element("properties", this.getVersion().getSTYLE());
        }
    }

    private static class XML_OD extends OOXML {
        private final String schemaFile;
        private Schema schema = null;

        public XML_OD(final String dateString, final String versionString, final String schemaFile) {
            super(XMLFormatVersion.get(XMLVersion.OD, versionString), dateString);
            this.schemaFile = schemaFile;
        }

        @Override
        public boolean canValidate() {
            return this.schemaFile != null;
        }

        private Schema getSchema() throws SAXException {
            if (this.schema == null && this.schemaFile != null) {
                System.setProperty(SchemaFactory.class.getName() + ":" + XMLConstants.RELAXNG_NS_URI, "com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory");
                this.schema = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI).newSchema(getClass().getResource("oofficeDTDs/" + this.schemaFile));
            }
            return this.schema;
        }

        @Override
        public Validator getValidator(Document doc) {
            final Schema schema;
            try {
                schema = this.getSchema();
            } catch (SAXException e) {
                throw new IllegalStateException("relaxNG schemas pb", e);
            }
            return schema == null ? null : new Validator.JAXPValidator(doc, schema);
        }

        @Override
        public String getOfficeScripts() {
            return "scripts";
        }

        @Override
        public String getOfficeScript() {
            return "script";
        }

        @Override
        public String getOfficeEventListeners() {
            return "event-listeners";
        }

        @Override
        public String getEventListener() {
            return "event-listener";
        }

        @Override
        public final String[] getFontDecls() {
            return new String[] { "font-face-decls", "style:font-face" };
        }

        @Override
        public final Element getTab() {
            return new Element("tab", getVersion().getTEXT());
        }

        @Override
        public String getFrameQName() {
            return "draw:frame";
        }

        @Override
        public Element createFormattingProperties(String family) {
            return new Element(family + "-properties", this.getVersion().getSTYLE());
        }
    }

    private static final class XML_OD_1_0 extends XML_OD {
        public XML_OD_1_0() {
            super("20061130", "1.0", null);
        }
    }

    private static final class XML_OD_1_1 extends XML_OD {
        public XML_OD_1_1() {
            super("20070201", "1.1", "OpenDocument-strict-schema-v1.1.rng");
        }
    }

    private static final class XML_OD_1_2 extends XML_OD {
        public XML_OD_1_2() {
            super("20110317", "1.2", "OpenDocument-v1.2-schema.rng");
        }
    }
}