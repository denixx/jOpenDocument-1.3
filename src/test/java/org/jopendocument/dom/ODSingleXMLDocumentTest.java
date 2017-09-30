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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.jopendocument.dom.Library.EmbeddedLibrary;
import org.jopendocument.dom.Library.LinkedLibrary;
import org.jopendocument.dom.ODPackage.RootElement;
import org.jopendocument.dom.spreadsheet.SheetTest;
import org.jopendocument.dom.spreadsheet.Table;
import org.jopendocument.dom.style.PageLayoutStyle;
import org.jopendocument.dom.style.SideStyleProperties.Side;
import org.jopendocument.dom.text.Heading;
import org.jopendocument.dom.text.Paragraph;
import org.jopendocument.dom.text.ParagraphStyle;
import org.jopendocument.dom.text.TextDocument;
import org.jopendocument.util.CollectionUtils;
import org.jopendocument.util.CompareUtils;
import org.jopendocument.util.TimeUtils;

import javax.xml.datatype.Duration;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class ODSingleXMLDocumentTest extends TestCase {

    // static necessary since JUnit only accept classes and not instances
    // non-null value to allow single test (since the value is usually set from suite())
    static private XMLVersion staticVersion = XMLVersion.OD;

    public static Test suite() {
        final TestSuite suite = new TestSuite("Test for ODSingleXMLDocument");
        for (final XMLVersion v : XMLVersion.values()) {
            staticVersion = v;
            // works since an instance is created by this call and thus uses staticVersion
            suite.addTest(new TestSuite(ODSingleXMLDocumentTest.class, v.toString()));
        }

        return suite;
    }

    static public final void assertEquals(final BigDecimal o1, final BigDecimal o2) {
        assertTrue(CompareUtils.equalsWithCompareTo(o1, o2));
    }

    private final XMLVersion version;
    private final XMLFormatVersion formatVersion;

    public ODSingleXMLDocumentTest() {
        this(staticVersion);
    }

    public ODSingleXMLDocumentTest(XMLVersion version) {
        super();
        this.version = version;
        this.formatVersion = OOXML.getLast(version).getFormatVersion();
    }

    private ODPackage createPackage() throws IOException {
        return createPackage("test");
    }

    private ODPackage createPackage(final String name) throws IOException {
        return new ODPackage(this.getClass().getResourceAsStream(name + "." + ContentType.TEXT.getVersioned(this.version).getExtension()));
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    private void assertValid(final ODSingleXMLDocument single) {
        SheetTest.assertValid(single.getPackage());
    }

    private void assertValid(final ODDocument doc) {
        SheetTest.assertValid(doc.getPackage());
    }

    public void testGetText() throws Exception {
        final TextDocument textDoc = createPackage().getTextDocument();
        final Paragraph textP = textDoc.getParagraph(1);
        // the text of the frame inside textP isn't included in its character content
        final Element frameElem = textDoc.getPackage().getContent().getDescendantByName(textDoc.getFormatVersion().getXML().getFrameQName(), "Cadre1");
        assertSame(textP.getElement(), frameElem.getParentElement());
        assertEquals("FrameContent", new ODFrame<ODDocument>(textDoc, frameElem).getCharacterContent(true));
        final String p1Text = "Test character content : spaces   and tab\tsample value";
        assertEquals(p1Text, textP.getCharacterContent());

        // heading + p0 + p1
        assertEquals("Titre 2\n" + " ۝   §\n" + p1Text, textDoc.getCharacterContent(true));
    }

    public void testCreate() throws Exception {
        final ODSingleXMLDocument single = createPackage().toSingle();
        assertValid(single);
        assertTrue(single.getPackage().isSingle());

        // test createFromDocument()
        final ODPackage pkg1 = ODPackage.createFromDocuments((Document) single.getDocument().clone(), null);
        assertTrue(pkg1.isSingle());
        assertEquals(single.getPackage().getContentType(), pkg1.getContentType());

        final ODPackage emptyPkg = new ODPackage();
        final ContentType[] types = ContentType.values();
        final ContentTypeVersioned[] versTypes = ContentTypeVersioned.values();
        for (final ContentTypeVersioned ct : versTypes) {
            assertEquals(ct.isTemplate(), ct.getTemplate() == ct);
            assertEquals(!ct.isTemplate(), ct.getNonTemplate() == ct);
            if (ct.isTemplate())
                assertSame(ct, ct.getNonTemplate().getTemplate());
            else if (ct.getTemplate() != null)
                assertSame(ct, ct.getTemplate().getNonTemplate());

            final ODPackage pkg = ct.createPackage(OOXML.getLast(ct.getVersion()).getFormatVersion());
            assertEquals(ct, pkg.getContentType());
            assertEquals(0, pkg.validateSubDocuments().size());

            // OOo has no templates and only one version
            if (ct.getVersion() == XMLVersion.OD) {
                pkg.setTemplate(true);
                assertTrue(pkg.isTemplate());
                pkg.setTemplate(false);
                assertFalse(pkg.isTemplate());

                try {
                    pkg.putFile(RootElement.META.getZipEntry(), RootElement.META.createDocument(XMLFormatVersion.getOOo()));
                    fail("should throw since XML version is not compatible");
                } catch (Exception e) {
                    assertTrue(e.getMessage().contains("Cannot change version"));
                }
                try {
                    pkg.putFile(RootElement.META.getZipEntry(), RootElement.META.createDocument(XMLFormatVersion.get(ct.getVersion(), "1.0")));
                    fail("should throw since office version is not compatible");
                } catch (Exception e) {
                    assertTrue(e.getMessage().contains("Cannot change format version"));
                }
            }
            // no-op
            pkg.setContentType(pkg.getContentType());
            try {
                // same version but different type
                pkg.setContentType(types[(ct.getType().ordinal() + 3) % types.length].getVersioned(ct.getVersion()));
                fail("should throw since type is not compatible");
            } catch (Exception e) {
                assertTrue(e.getMessage().contains("Cannot change type"));
            }

            // an empty package can be changed to anything
            emptyPkg.setContentType(ct);
            assertEquals(ct, emptyPkg.getContentType());
        }

        // TextDocument
        {
            final TextDocument emptyTextDoc = TextDocument.createEmpty("PPPPP", this.formatVersion);
            assertValid(emptyTextDoc);
            assertEquals(ContentType.TEXT, emptyTextDoc.getPackage().getContentType().getType());
            assertEquals("PPPPP", emptyTextDoc.getParagraph(0).getElement().getText());
            try {
                emptyTextDoc.getParagraph(1);
                fail("Only 1 paragraph");
            } catch (IndexOutOfBoundsException e) {
                // ok
            }
        }
    }

    public void testSplit() throws Exception {
        final ODPackage pkg = createPackage();
        SheetTest.assertValid(pkg);
        final ODSingleXMLDocument single = pkg.toSingle();
        assertSingle(pkg, single);

        // a valid package must not contain a single xml document
        final ByteArrayOutputStream out = new ByteArrayOutputStream(32000);
        pkg.save(out);
        assertFalse(new ODPackage(new ByteArrayInputStream(out.toByteArray())).isSingle());

        // split
        assertTrue(pkg.split());
        // the second time nothing happens
        assertFalse(pkg.split());
        assertTrue(single.isDead());
        assertNull(single.getPackage());
        assertFalse(pkg.isSingle());
        assertEquals(0, pkg.validateSubDocuments().size());

        // we can convert once more to single
        assertSingle(pkg, pkg.toSingle());
    }

    private void assertSingle(final ODPackage pkg, final ODSingleXMLDocument single) {
        assertValid(single);
        assertFalse(single.isDead());
        assertSame(pkg, single.getPackage());
        assertTrue(pkg.isSingle());
    }

    public void testAdd() throws Exception {
        final ODSingleXMLDocument single = new ODPackage(this.getClass().getResourceAsStream("empty.odt")).toSingle();
        // really empty
        single.getBody().removeContent();
        final ODSingleXMLDocument single2 = new ODPackage(this.getClass().getResourceAsStream("styles.odt")).toSingle();
        final int single2PCount = single2.getPackage().getTextDocument().getParagraphCount();
        single.add(single2);
        assertValid(single);
        final TextDocument textDocument = single.getPackage().getTextDocument();
        // +1 since we requested a page break
        assertEquals(single2PCount + 1, textDocument.getParagraphCount());

        // test our empty
        {
            // toSingle() before getTextDocument() otherwise checkEntryForDocument() throws
            // exception
            final ODPackage empty = new ODPackage(TextDocument.createEmpty("empty text doc", this.formatVersion).getPackage()).toSingle().getPackage();
            final TextDocument textDoc = empty.getTextDocument();
            final ODPackage nonEmptyPkg = createPackage().toSingle().getPackage();
            final TextDocument nonEmptyTextDoc = nonEmptyPkg.getTextDocument();
            final String nonEmptyCharContent = nonEmptyTextDoc.getCharacterContent(true);

            empty.toSingle().add(nonEmptyPkg.toSingle(), true);
            SheetTest.assertValid(empty);

            // nonEmpty wasn't modified
            assertEquals(nonEmptyCharContent, nonEmptyTextDoc.getCharacterContent(true));
            // "empty text doc" + page break
            assertEquals(nonEmptyTextDoc.getParagraphCount() + 1 + 1, textDoc.getParagraphCount());
            // text was concatenated
            assertEquals("empty text doc", textDoc.getParagraph(0).getCharacterContent());
            assertEquals("empty text doc\n\n" + nonEmptyCharContent, textDoc.getCharacterContent(true));
        }

        // test self-add
        final int pCount = textDocument.getParagraphCount();
        single.add(single, false);
        assertValid(single);
        assertEquals(pCount * 2, textDocument.getParagraphCount());
    }

    public void testAddParagraph() throws Exception {
        final TextDocument single = new ODPackage(this.getClass().getResourceAsStream("styles.odt")).getTextDocument();

        final Paragraph p = new Paragraph();
        p.setStyleName("testPragraph");
        p.addContent("Hello");
        p.addTab();
        p.addStyledContent("World", "testChar");
        // add at the end
        assertNull(p.getElement().getDocument());
        p.setDocument(single);
        assertSame(single.getContentDocument(), p.getElement().getDocument());
        assertEquals("Hello\tWorld", p.getCharacterContent());

        final Heading h = new Heading();
        h.setStyleName("inexistantt");
        h.addContent("Heading text");
        try {
            h.setDocument(single);
            fail("should throw since style doesn't exist");
        } catch (Exception e) {
            // ok
        }
        h.setStyleName("testPragraph");
        // add before p
        final Element pParent = p.getElement().getParentElement();
        single.add(h, pParent, pParent.indexOf(p.getElement()));

        assertValid(single);

        // rm
        p.setDocument(null);
        assertNull(p.getElement().getDocument());
    }

    public void testTable() throws Exception {
        final TextDocument textDoc = createPackage().getTextDocument();
        assertValid(textDoc);
        final ODXMLDocument single = textDoc.getPackage().getContent();
        assertNull(single.getDescendantByName("table:table", "inexistant"));
        final Element table = single.getDescendantByName("table:table", "JODTestTable");
        assertNotNull(table);
        final Table<TextDocument> t = new Table<TextDocument>(textDoc, table);

        // test createColumnStyle()
        assertNull(t.createColumnStyle(null, null).getWidth());
        assertEquals(30, t.createColumnStyle(3, LengthUnit.CM).getWidth(LengthUnit.MM).intValueExact());

        assertEquals(1, t.getHeaderRowCount());
        assertEquals(0, t.getHeaderColumnCount());

        final Calendar c = Calendar.getInstance();
        c.clear();
        c.set(2005, 0, 12, 12, 35);
        assertEquals(c.getTime(), t.getValueAt(2, 1));

        // 11.91cm
        assertEquals(119.06f, t.getWidth());
        // since the table has a width, the new column must also
        t.setColumnCount(6, t.createColumnStyle(3, LengthUnit.CM), true);
        t.setValueAt(3.14, 5, 0);
        assertTableWidth(t, 119.06f);
        final float ratio = t.getColumn(0).getWidth() / t.getColumn(1).getWidth();
        t.setColumnCount(2, 1, true);
        // ratio is kept
        assertEquals(ratio, t.getColumn(0).getWidth() / t.getColumn(1).getWidth());
        assertTableWidth(t, 119.06f);
        // table changes width
        final float width = t.getColumn(0).getWidth();
        t.setColumnCount(4, 0, false);
        assertTableWidth(t, 119.06f + 2 * width);
        t.setColumnCount(1, 123, false);
        assertEquals(1, t.getColumnCount());
        assertTableWidth(t, width);

        assertValid(textDoc);

        t.detach();
        assertNull(single.getDescendantByName("table:table", "JODTestTable"));
    }

    private void assertTableWidth(Table<?> t, float w) {
        assertEquals(w, t.getWidth());
        float total = 0;
        for (int i = 0; i < t.getColumnCount(); i++) {
            total += t.getColumn(i).getWidth();
        }
        assertEquals(round(w), round(total));
    }

    private long round(float w) {
        return Math.round(w * 100.0) / 100;
    }

    public void testFrame() throws Exception {
        final ODPackage pkg = createPackage();
        final Element frameElem = pkg.getContent().getDescendantByName(pkg.getFormatVersion().getXML().getFrameQName(), "Cadre1");

        final ODFrame<ODDocument> frame = new ODFrame<ODDocument>(pkg.getODDocument(), frameElem);
        // for some reason OO converted the 72 to 73 during export
        final BigDecimal width = this.version == XMLVersion.OOo ? new BigDecimal("42.73") : new BigDecimal("42.72");
        assertEquals(width, frame.getWidth());
        // height depends on the content
        assertNull(frame.getHeight());

        assertEquals("right", frame.getStyle().getGraphicProperties().getHorizontalPosition());
        assertEquals("paragraph", frame.getStyle().getGraphicProperties().getHorizontalRelation());

        assertEquals(asList("position"), frame.getStyle().getGraphicProperties().getProtected());
        assertTrue(frame.getStyle().getGraphicProperties().isContentPrinted());
    }

    public void testStyle() throws Exception {
        final StyleDesc<ParagraphStyle> pDesc = Style.getStyleDesc(ParagraphStyle.class, this.version);

        // test getStyle() and Style.getReferences()
        {
            final ODPackage pkg = createPackage();

            // in OOo format there's no name attribute
            final ODXMLDocument content = pkg.getContent();
            if (pkg.getVersion() != XMLVersion.OOo) {
                // test that multiple attributes may refer to paragraph styles
                final XPath ellipseXPath = content.getXPath("//draw:ellipse[@draw:name = 'Ellipse']");
                final Element ellipse = (Element) ellipseXPath.selectSingleNode(content.getDocument());
                final String drawTextStyleName = ellipse.getAttributeValue("text-style-name", ellipse.getNamespace("draw"));
                final ParagraphStyle ellipseTextStyle = pDesc.findStyleWithName(pkg, content.getDocument(), drawTextStyleName);
                assertEquals(singletonList(ellipse), ellipseTextStyle.getReferences());
            }

            final ODXMLDocument styles = pkg.getStyles();
            // otherwise there's only one automatic-styles element
            assertFalse(pkg.isSingle());
            final String duplicateAutoStyleName = "P_in_both_documents";
            // first it's nowhere
            assertNull(pkg.getStyle(content.getDocument(), pDesc, duplicateAutoStyleName));
            assertNull(pkg.getStyle(styles.getDocument(), pDesc, duplicateAutoStyleName));

            // we add it to styles.xml
            final Element stylesAutoStyle = pDesc.createElement(duplicateAutoStyleName);
            styles.addAutoStyle(stylesAutoStyle);
            // automatic-styles can only be referenced from the same document
            assertNull(pkg.getStyle(content.getDocument(), pDesc, duplicateAutoStyleName));
            assertSame(stylesAutoStyle, pkg.getStyle(styles.getDocument(), pDesc, duplicateAutoStyleName));

            // now it exists both in content and styles but doesn't denote the same style
            final Element contentAutoStyle = pDesc.createElement(duplicateAutoStyleName);
            content.addAutoStyle(contentAutoStyle);
            assertSame(contentAutoStyle, pkg.getStyle(content.getDocument(), pDesc, duplicateAutoStyleName));
            assertSame(stylesAutoStyle, pkg.getStyle(styles.getDocument(), pDesc, duplicateAutoStyleName));
            assertNotSame(contentAutoStyle, stylesAutoStyle);

            // use it in content and styles
            final XPath headerXPath = styles.getXPath("//text:p[string() = 'Header']");
            final Element headerElem = (Element) headerXPath.selectSingleNode(styles.getDocument());
            new Paragraph(headerElem).setStyleName(duplicateAutoStyleName);
            final Element contentPElem = (Element) content.getXPath("//text:p[2]").selectSingleNode(content.getDocument());
            new Paragraph(contentPElem).setStyleName(duplicateAutoStyleName);

            // styleP1 can only be referenced from styles.xml
            assertEquals(singletonList(headerElem), StyleStyle.warp(pkg, stylesAutoStyle).getReferences());
            // contentP1 can only be referenced from content.xml
            assertEquals(singletonList(contentPElem), StyleStyle.warp(pkg, contentAutoStyle).getReferences());

            // test non-StyleStyle
            final StyleDesc<PageLayoutStyle> plDesc = Style.getStyleDesc(PageLayoutStyle.class, this.version);
            final Element masterPageElem = (Element) styles.getChild("master-styles").getChildren().get(0);
            final PageLayoutStyle pm1Style = plDesc.findStyleWithName(pkg, masterPageElem.getDocument(),
                    masterPageElem.getAttributeValue(plDesc.getElementName() + "-name", masterPageElem.getNamespace()));
            assertNull(pm1Style.getBackgroundColor());
            assertNull(pm1Style.getPageLayoutProperties().getBorder(Side.TOP));
            assertEquals(new BigDecimal(2), pm1Style.getPageLayoutProperties().getMargin(Side.TOP, LengthUnit.CM));
            // only style:master-page points to pm1
            assertSame(masterPageElem, CollectionUtils.getSole(pm1Style.getReferences()));

            // when merging styles.xml
            pkg.toSingle();
            // the content doesn't change
            assertSame(contentAutoStyle, pkg.getStyle(pkg.getContent().getDocument(), pDesc, duplicateAutoStyleName));
            final String mergedStyleName = new Paragraph((Element) headerXPath.selectSingleNode(pkg.getContent().getDocument())).getStyleName();
            assertNotNull(mergedStyleName);
            assertFalse(duplicateAutoStyleName.equals(mergedStyleName));
        }

        final ODPackage pkg = createPackage();
        final Element heading = (Element) pkg.getContent().getXPath("//text:h[string() = 'Titre 2']").selectSingleNode(pkg.getContent().getDocument());
        final String styleName = heading.getAttributeValue("style-name", heading.getNamespace());
        // common styles are not in content.xml
        assertNull(pkg.getContent().getStyle(pDesc, styleName));
        // but in styles.xml
        testStyleElem(pkg.getXMLFile("styles.xml").getStyle(pDesc, styleName));
        testStyleElem(pkg.getStyle(pDesc, styleName));
        // except if we merge the two
        pkg.toSingle();
        testStyleElem(pkg.getContent().getStyle(pDesc, styleName));
        testStyleElem(pkg.getStyle(pDesc, styleName));

        // test that we can use StyleStyle instances to warp default-style
        // (was causing problems since the element name isn't the normal one, e.g. style:style)
        final ParagraphStyle defaultPStyle = Style.getStyleStyleDesc(ParagraphStyle.class, this.version).findDefaultStyle(pkg);
        assertEquals(StyleStyleDesc.ELEMENT_DEFAULT_NAME, defaultPStyle.getElement().getName());
        assertEquals("Times New Roman", defaultPStyle.getTextProperties().getAttributeValue("font-name", this.version.getSTYLE()));

        // test getParentStyle()
        {
            final TextDocument textDoc = new ODPackage(this.getClass().getResourceAsStream("styles.odt")).getTextDocument();
            final Paragraph p = textDoc.getParagraph(1);
            assertEquals("paragraph auto style : +14", p.getCharacterContent());
            final ParagraphStyle pStyle = p.getStyle();
            assertEquals("14pt", pStyle.getTextProperties().getAttributeValue("font-size", pStyle.getElement().getNamespace("fo")));
            final ParagraphStyle parentStyle = (ParagraphStyle) pStyle.getParentStyle();
            assertEquals(pStyle.getFamily(), parentStyle.getFamily());
            assertEquals("testPragraph", parentStyle.getName());
            try {
                pStyle.getParentStyle(null);
                fail("Null isn't valid");
            } catch (NullPointerException e) {
                // OK
            }
            // ParagraphStyle cannot have conditions so passing p isn't needed
            assertEquals(parentStyle, pStyle.getParentStyle(p));
            assertEquals("Standard", parentStyle.getParentStyle().getName());
            assertNull(parentStyle.getParentStyle().getParentStyle());
        }

        // test create common styles
        {
            final TextDocument emptyTextDoc = TextDocument.createEmpty("foo", this.formatVersion);
            final Paragraph p0 = emptyTextDoc.getParagraph(0);
            assertEquals("foo", p0.getCharacterContent());

            assertNull(emptyTextDoc.getPackage().getStyle(pDesc, "pStyle"));
            assertNull(p0.getStyle());

            final ParagraphStyle pStyle = pDesc.createCommonStyle(emptyTextDoc.getPackage(), "pStyle");
            try {
                pDesc.createCommonStyle(emptyTextDoc.getPackage(), "pStyle");
                fail("Style already exists");
            } catch (Exception e) {
                // OK
            }
            p0.setStyleName("pStyle");

            // correctly added in styles
            assertSame(pStyle.getElement(), emptyTextDoc.getPackage().getStyle(pDesc, "pStyle"));
            assertEquals(pStyle, p0.getStyle());

            // test default value
            assertEquals(Color.BLACK, p0.getStyle().getColor());
            pStyle.getTextProperties().setColor(Color.BLUE);
            assertEquals(Color.BLUE, p0.getStyle().getColor());
        }
    }

    private void testStyleElem(final Element styleElem) {
        assertNotNull(styleElem);
        // OOo has text:level="2" on text:h
        if (this.version != XMLVersion.OOo)
            assertEquals("2", styleElem.getAttributeValue("default-outline-level", styleElem.getNamespace()));
        assertEquals("Heading", styleElem.getAttributeValue("parent-style-name", styleElem.getNamespace()));
    }

    public void testMeta() throws Exception {
        final ODPackage pkg = createPackage();
        final ODMeta meta = pkg.getMeta();
        assertEquals("firstInfo", meta.getUserMeta("Info 1").getValue());
        assertEquals("", meta.getUserMeta("secondName").getValue());

        final Set<String> expected = CollectionUtils.createSet("Info 1", "secondName", "Info 3", "Info 4");
        assertEquals(expected, new HashSet<String>(meta.getUserMetaNames()));

        // does not exist
        assertNull(meta.getUserMeta("toto"));
        // now it does
        assertNotNull(meta.getUserMeta("toto", true));
        meta.removeUserMeta("toto");
        // now it was removed
        assertNull(meta.getUserMeta("toto"));
        final ODUserDefinedMeta toto = meta.getUserMeta("toto", true);
        toto.setValue(3.5);
        assertEquals(ODValueType.FLOAT, toto.getValueType());
        assertEquals(3.5f, ((BigDecimal) toto.getValue()).floatValue());
        final TimeZone marquisesTZ = TimeZone.getTimeZone("Pacific/Marquesas");
        final TimeZone pstTZ = TimeZone.getTimeZone("PST");
        final Calendar cal = Calendar.getInstance(pstTZ);
        final int hour = cal.get(Calendar.HOUR_OF_DAY);
        final int minute = cal.get(Calendar.MINUTE);

        {
            toto.setValue(cal, ODValueType.TIME);
            Duration actual = (Duration) toto.getValue();
            assertEquals(hour, actual.getHours());
            assertEquals(minute, actual.getMinutes());
            // for TIME the time zone is important, the same date has not the same value
            cal.setTimeZone(marquisesTZ);
            toto.setValue(cal, ODValueType.TIME);
            actual = (Duration) toto.getValue();
            // +1h30
            assertFalse(hour == actual.getHours());
            assertFalse(minute == actual.getMinutes());
            // test that we used the time part of the calendar
            final Calendar startInstant = (Calendar) cal.clone();
            startInstant.clear();
            // don't use set() otherwise it can moves to the next day
            startInstant.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            assertEquals(0, startInstant.get(Calendar.HOUR_OF_DAY));
            assertEquals(cal.getTimeInMillis() - startInstant.getTimeInMillis(), actual.getTimeInMillis(startInstant));

            final Duration duration = TimeUtils.getTypeFactory().newDurationDayTime(false, 2, 2, 2, 2);
            toto.setValue(duration);
            assertEquals(ODValueType.TIME, toto.getValueType());
            assertEquals(duration, toto.getValue());
        }

        {
            cal.setTimeZone(pstTZ);
            toto.setValue(cal, ODValueType.DATE);
            assertEquals(cal.getTime(), toto.getValue());
            // on the contrary for DATE the timezone is irrelevant
            cal.setTimeZone(marquisesTZ);
            toto.setValue(cal, ODValueType.DATE);
            assertEquals(cal.getTime(), toto.getValue());
        }

        final int origEditingCycles = meta.getEditingCycles();
        assertTrue(origEditingCycles > 20);
        meta.removeMetaChild("editing-cycles");
        assertEquals(1, meta.getEditingCycles());
        meta.setEditingCycles(origEditingCycles);
        assertEquals(origEditingCycles, meta.getEditingCycles());
    }

    public void testScript() throws Exception {
        // test toSingle()/split()
        {
            final ODPackage pkg = createPackage();
            final Map<String, Library> basicLibraries = pkg.readBasicLibraries();
            assertEquals(CollectionUtils.createSet("Standard", "Gimmicks"), basicLibraries.keySet());

            final LinkedLibrary ll = (LinkedLibrary) basicLibraries.get("Gimmicks");
            assertEquals("Gimmicks", ll.getName());
            assertTrue(ll.getHref().endsWith("share/basic/Gimmicks/script.xlb"));

            final EmbeddedLibrary embl = (EmbeddedLibrary) basicLibraries.get("Standard");
            assertEquals("Standard", embl.getName());
            assertFalse(embl.isReadonly());
            assertFalse(embl.isPasswordProtected());
            assertEquals(Collections.singletonMap("Module1", "REM  *****  BASIC  *****\n\n" + "Sub Main\n" + "\tExit sub\n" + "End Sub\n"), embl.getModules());
            assertEquals(Collections.singleton("Dialog1"), embl.getDialogs().keySet());
            final Element windowElem = embl.getDialogs().get("Dialog1");
            assertEquals("Dialog1", windowElem.getAttributeValue("id", XMLVersion.DIALOG_NS));
            assertEquals("180", windowElem.getAttributeValue("width", XMLVersion.DIALOG_NS));
            assertEquals("120", windowElem.getAttributeValue("height", XMLVersion.DIALOG_NS));

            final Map<String, EventListener> eventListeners = pkg.readEventListeners();
            assertEquals(1, eventListeners.size());
            // different names for different versions
            assertTrue(eventListeners.keySet().iterator().next().contains("load"));

            final ODSingleXMLDocument single = pkg.toSingle();
            // toSingle() has kept scripts
            assertEquals(basicLibraries, pkg.readBasicLibraries());
            assertEquals(eventListeners, pkg.readEventListeners());
            // ODPackage is just forwarding to ODSingleXMLDocument
            assertEquals(single.readBasicLibraries(), pkg.readBasicLibraries());
            SheetTest.assertValid(pkg);

            // we can split again
            pkg.split();
            SheetTest.assertValid(pkg);
            assertEquals(basicLibraries, pkg.readBasicLibraries());
            assertEquals(eventListeners, pkg.readEventListeners());
        }

        // test addBasicLibraries()
        testAddLibs(false);
        testAddLibs(true);

        // test concatenation
        {
            final ODPackage pkg = createPackage();
            final ODSingleXMLDocument single = pkg.toSingle();
            final Map<String, Library> basicLibraries = pkg.readBasicLibraries();
            final Map<String, EventListener> eventListeners = pkg.readEventListeners();

            single.add(single);
            SheetTest.assertValid(pkg);
            // libraries were not changed
            assertEquals(basicLibraries, pkg.readBasicLibraries());
            assertEquals(eventListeners, pkg.readEventListeners());

            final ODPackage empty = new ODPackage(TextDocument.createEmpty("text doc without libs", pkg.getFormatVersion()).getPackage());
            empty.toSingle();
            SheetTest.assertValid(empty);

            assertEquals(Collections.emptyMap(), empty.readBasicLibraries());
            assertEquals(Collections.emptyMap(), empty.readEventListeners());
            empty.toSingle().add(single);
            SheetTest.assertValid(empty);
            // all libraries were added
            assertEquals(basicLibraries, empty.readBasicLibraries());
            assertEquals(eventListeners, empty.readEventListeners());

        }
    }

    protected void testAddLibs(final boolean single) throws IOException {
        final ODPackage pkg = createPackage();
        if (single)
            pkg.toSingle();

        final Map<String, Library> beforeLibraries = pkg.readBasicLibraries();

        // completely new library
        final EmbeddedLibrary addedLib = new EmbeddedLibrary("addedLib", false, false, Collections.singletonMap("moduleAdded", "REM  *****  BASIC  *****\n\nREM empty\n"),
                Collections.<String, Element> emptyMap());
        assertFalse(beforeLibraries.containsKey(addedLib.getName()));
        assertEquals(Collections.singleton(addedLib.getName()), pkg.addBasicLibraries(Collections.singleton(addedLib)));

        final Map<String, Library> expectedLibraries = new HashMap<String, Library>(beforeLibraries);
        expectedLibraries.put(addedLib.getName(), addedLib);
        assertEquals(expectedLibraries, pkg.readBasicLibraries());

        // we can add again, since it's the same it will do nothing
        assertEquals(Collections.emptySet(), pkg.addBasicLibraries(Collections.singleton(addedLib)));
        assertEquals(expectedLibraries, pkg.readBasicLibraries());
        SheetTest.assertValid(pkg);

        // we can even add a new module in an existing library
        final Map<String, String> modules = new HashMap<String, String>(addedLib.getModules());
        modules.put("moduleAdded2", "");
        final EmbeddedLibrary addedLib2 = new EmbeddedLibrary("addedLib", false, false, modules, Collections.<String, Element> emptyMap());
        assertEquals(Collections.emptySet(), pkg.addBasicLibraries(Collections.singleton(addedLib2)));

        expectedLibraries.put(addedLib.getName(), addedLib2);
        assertEquals(expectedLibraries, pkg.readBasicLibraries());
        SheetTest.assertValid(pkg);

        // but the contents have to be the same
        modules.put("moduleAdded2", "different source");
        try {
            pkg.addBasicLibraries(Collections.singleton(new EmbeddedLibrary("addedLib", false, false, modules, Collections.<String, Element> emptyMap())));
            fail("moduleAdded2 exists and has different content");
        } catch (Exception e) {
            // OK
        }

        // return what was actually removed
        assertEquals(Collections.singleton("addedLib"), pkg.removeBasicLibraries(Arrays.asList("addedLib", "nonExistent")));
        assertEquals(beforeLibraries, pkg.readBasicLibraries());
    }
}
