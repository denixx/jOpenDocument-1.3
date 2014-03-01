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

import org.jopendocument.dom.ContentType;
import org.jopendocument.dom.ContentTypeVersioned;
import org.jopendocument.dom.ODDocument;
import org.jopendocument.dom.ODPackage;
import org.jopendocument.dom.XMLFormatVersion;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom.Element;

public class TextDocument extends ODDocument {

    public static TextDocument createFromFile(File f) throws IOException {
        return new ODPackage(f).getTextDocument();
    }

    /**
     * This method should be avoided, use {@link ODPackage#getTextDocument()}.
     * 
     * @param fd a package.
     * @return the text document.
     */
    public static TextDocument get(final ODPackage fd) {
        return fd.hasODDocument() ? fd.getTextDocument() : new TextDocument(fd);
    }

    public static TextDocument createEmpty(String s) throws IOException {
        return createEmpty(s, XMLFormatVersion.getDefault());
    }

    public static TextDocument createEmpty(String s, XMLFormatVersion ns) throws IOException {
        final ContentTypeVersioned ct = ContentType.TEXT.getVersioned(ns.getXMLVersion());
        final TextDocument res = ct.createPackage(ns).getTextDocument();
        final Element textP = Paragraph.createEmpty(ns.getXMLVersion());
        textP.addContent(s);
        res.getBody().addContent(textP);
        return res;
    }

    private TextDocument(final ODPackage orig) {
        super(orig);
    }

    public final Paragraph getParagraph(int i) {
        return new Paragraph(this.getParagraphChildren().get(i), this);
    }

    @SuppressWarnings("unchecked")
    private final List<Element> getParagraphChildren() {
        final Element proto = Paragraph.createEmpty(getVersion());
        return this.getBody().getChildren(proto.getName(), proto.getNamespace());
    }

    public final int getParagraphCount() {
        return this.getParagraphChildren().size();
    }

    public final String getCharacterContent(final boolean ooMode) {
        return TextNode.getChildrenCharacterContent(this.getBody(), this.getFormatVersion(), ooMode);
    }

    /**
     * Append a paragraph or a heading.
     * 
     * @param p paragraph to add.
     */
    public synchronized void add(TextNode<?> p) {
        this.add(p, null, -1);
    }

    public synchronized void add(TextNode<?> p, Element where, int index) {
        // add it first to avoid infinite loop, since setDocument() can call this method
        final Element addToElem = where == null ? this.getBody() : where;
        if (index < 0)
            addToElem.addContent(p.getElement());
        else
            addToElem.addContent(index, p.getElement());

        try {
            p.setDocument(this);
        } catch (RuntimeException e) {
            // the paragraph can throw an exception to notify that is not compatible with us (eg
            // missing styles), in that case remove it
            p.getElement().detach();
            throw e;
        }
    }
}
