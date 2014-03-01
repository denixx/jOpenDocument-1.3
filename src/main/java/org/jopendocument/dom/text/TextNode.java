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

package org.jopendocument.dom.text;

import org.jopendocument.dom.ODDocument;
import org.jopendocument.dom.OOXML;
import org.jopendocument.dom.StyleStyle;
import org.jopendocument.dom.StyledNode;
import org.jopendocument.dom.XMLFormatVersion;
import org.jopendocument.dom.spreadsheet.Cell;
import org.jopendocument.util.CollectionUtils;
import org.jopendocument.util.cc.IPredicate;
import org.jopendocument.util.DescendantIterator;
import org.jopendocument.util.JDOMUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;

/**
 * A text node that can be created ex nihilo. Ie without a document at first.
 * 
 * @author Sylvain CUAZ
 * 
 * @param <S> type of style.
 */
public abstract class TextNode<S extends StyleStyle> extends StyledNode<S, TextDocument> {

    // see §6.1.2 White Space Characters of OpenDocument v1.2
    private static final Pattern multiSpacePattern = Pattern.compile("[\t\r\n ]+");

    static public String getChildrenCharacterContent(final Element parentElem, final XMLFormatVersion vers, final boolean ooMode) {
        final List<String> ps = new ArrayList<String>();
        for (final Object o : parentElem.getChildren()) {
            final Element child = (Element) o;
            if ((child.getName().equals("p") || child.getName().equals("h")) && child.getNamespacePrefix().equals("text")) {
                ps.add(getCharacterContent(child, vers, ooMode));
            }
        }
        return CollectionUtils.join(ps, "\n");
    }

    /**
     * Return the text value of the passed element. This method doesn't just return the XML text
     * content, it also parses XML elements (like paragraphs, tabs and line-breaks). For the
     * differences between the OO way (as of 3.1) and the OpenDocument way see section 5.1.1
     * White-space Characters of OpenDocument-v1.0-os and §6.1.2 of OpenDocument-v1.2-part1. In
     * essence OpenOffice never trim strings.
     * 
     * @param pElem a text element, e.g. text:p or text:h.
     * @param vers the version of the element.
     * @param ooMode whether to use the OO way or the standard way.
     * @return the parsed text value.
     */
    static public final String getCharacterContent(final Element pElem, final XMLFormatVersion vers, final boolean ooMode) {
        final OOXML xml = OOXML.get(vers, false);
        if (!xml.getVersion().getTEXT().equals(pElem.getNamespace()))
            throw new IllegalArgumentException("element isn't of version " + vers);

        final StringBuilder sb = new StringBuilder();
        final Namespace textNS = pElem.getNamespace();
        final Element tabElem = xml.getTab();
        final Element newLineElem = xml.getLineBreak();
        // true if the string ends with a space that wasn't expanded from an XML element (e.g.
        // <tab/> or <text:s/>)
        boolean spaceSuffix = false;
        final Iterator<?> iter = new DescendantIterator(pElem, new IPredicate<Content>() {
            @Override
            public boolean evaluateChecked(Content input) {
                if (input instanceof Element) {
                    // don't descend into frames, graphical shapes...
                    return !((Element) input).getNamespace().getPrefix().equals("draw");
                }
                return true;
            }
        });
        while (iter.hasNext()) {
            final Object o = iter.next();
            if (o instanceof Text) {
                final String text = multiSpacePattern.matcher(((Text) o).getText()).replaceAll(" ");
                // trim leading
                if (!ooMode && text.startsWith(" ") && (spaceSuffix || sb.length() == 0))
                    sb.append(text.substring(1));
                else
                    sb.append(text);
                spaceSuffix = text.endsWith(" ");
            } else if (o instanceof Element) {
                // perhaps handle conditions (conditional-text, hiddenparagraph, hidden-text)
                final Element elem = (Element) o;
                if (JDOMUtils.equals(elem, tabElem)) {
                    sb.append("\t");
                } else if (JDOMUtils.equals(elem, newLineElem)) {
                    sb.append("\n");
                } else if (elem.getName().equals("s") && elem.getNamespace().equals(textNS)) {
                    final int count = Integer.valueOf(elem.getAttributeValue("c", textNS, "1"));
                    final char[] toAdd = new char[count];
                    Arrays.fill(toAdd, ' ');
                    sb.append(toAdd);
                }
            }
        }
        // trim trailing
        if (!ooMode && spaceSuffix)
            sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    protected TextDocument parent;

    public TextNode(Element local, final Class<S> styleClass) {
        this(local, styleClass, null);
    }

    protected TextNode(Element local, final Class<S> styleClass, final TextDocument parent) {
        super(local, styleClass);
        this.parent = parent;
    }

    @Override
    public final TextDocument getODDocument() {
        return this.parent;
    }

    public final void setDocument(TextDocument doc) {
        if (doc != this.parent) {
            if (doc == null) {
                this.parent = null;
                this.getElement().detach();
            } else if (doc.getContentDocument() != this.getElement().getDocument()) {
                doc.add(this);
            } else {
                this.checkDocument(doc);
                this.parent = doc;
            }
        }
    }

    protected abstract void checkDocument(ODDocument doc);

    public final String getCharacterContent() {
        return this.getCharacterContent(Cell.getTextValueMode());
    }

    public final String getCharacterContent(final boolean ooMode) {
        // TODO add format version field to this class (e.g. required to add a tab to a paragraph)
        if (getODDocument() == null)
            throw new IllegalStateException("Unknown format version");
        return getCharacterContent(this.getElement(), getODDocument().getFormatVersion(), ooMode);
    }
}