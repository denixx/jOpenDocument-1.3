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

import org.jopendocument.util.CollectionMap;
import org.jopendocument.util.ExceptionUtils;

import javax.xml.validation.Schema;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class Validator {

    private final Document doc;

    protected Validator(Document doc) {
        super();
        this.doc = doc;
    }

    protected final Document getDoc() {
        return this.doc;
    }

    /**
     * Validate a document, stopping at the first problem.
     * 
     * @return <code>null</code> if <code>doc</code> is valid, a String describing the first problem
     *         otherwise.
     */
    public abstract String isValid();

    /**
     * Validate the whole document.
     * 
     * @return all problems (with line number) indexed by type, e.g. ERROR unexpected attribute
     *         "style:join-border" => [on line 22:50, on line 14901:290].
     */
    public abstract CollectionMap<String, String> validateCompletely();

    static public final class JAXPValidator extends Validator {

        private final Schema schema;

        /**
         * Validate a document using JAXP.
         * 
         * @param doc the document to validate
         * @param schema the schema.
         */
        public JAXPValidator(final Document doc, final Schema schema) {
            super(doc);
            this.schema = schema;
        }

        @Override
        public String isValid() {
            final SAXException exn = JDOMUtils.validate(getDoc(), this.schema, null);
            if (exn == null)
                return null;
            else if (exn instanceof SAXParseException)
                return exn.getLocalizedMessage() + " " + RecordingErrorHandler.getDesc((SAXParseException) exn);
            else
                return exn.getLocalizedMessage();
        }

        @Override
        public CollectionMap<String, String> validateCompletely() {
            final RecordingErrorHandler recErrorHandler = new RecordingErrorHandler();
            final SAXException exn = JDOMUtils.validate(getDoc(), this.schema, recErrorHandler);
            assert exn == null : "Exception thrown despite the error handler";
            return recErrorHandler.getMap();
        }
    }

    static public final class DTDValidator extends Validator {

        private final SAXBuilder b;

        public DTDValidator(final Document doc) {
            this(doc, new SAXBuilder());
        }

        /**
         * Validate a document using its DTD.
         * 
         * @param doc the document to validate
         * @param b a builder which can resolve doc's DTD.
         */
        public DTDValidator(final Document doc, final SAXBuilder b) {
            super(doc);
            this.b = b;
        }

        @Override
        public String isValid() {
            try {
                JDOMUtils.validateDTD(getDoc(), this.b, null);
                return null;
            } catch (JDOMException e) {
                return ExceptionUtils.getStackTrace(e);
            }
        }

        @Override
        public CollectionMap<String, String> validateCompletely() {
            try {
                final RecordingErrorHandler recErrorHandler = new RecordingErrorHandler();
                JDOMUtils.validateDTD(getDoc(), this.b, recErrorHandler);
                return recErrorHandler.getMap();
            } catch (JDOMException e) {
                throw new IllegalStateException("Unable to read the document", e);
            }
        }

    }

    private static final class RecordingErrorHandler implements ErrorHandler {
        private final CollectionMap<String, String> res;

        private RecordingErrorHandler() {
            this(new CollectionMap<String, String>());
        }

        private RecordingErrorHandler(CollectionMap<String, String> res) {
            this.res = res;
        }

        public final CollectionMap<String, String> getMap() {
            return this.res;
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
            addExn("WARNING", e);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            addExn("FATAL", e);
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            addExn("ERROR", e);
        }

        private void addExn(final String level, SAXParseException e) {
            // e.g. ERROR unexpected attribute "style:join-border" => on line 14901:290
            this.res.put(level + " " + e.getMessage(), getDesc(e));
        }

        static String getDesc(SAXParseException e) {
            final String f = e.getSystemId() == null ? "" : " of document " + e.getSystemId();
            return "on line " + e.getLineNumber() + ":" + e.getColumnNumber() + f;
        }
    }
}