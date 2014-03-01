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

package org.jopendocument.model.text;

/**
 * 
 */
public class TextFootnoteRef {

    protected String textReferenceFormat;
    protected String textRefName;
    protected String value;

    /**
     * Gets the value of the textReferenceFormat property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getTextReferenceFormat() {
        return this.textReferenceFormat;
    }

    /**
     * Gets the value of the textRefName property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getTextRefName() {
        return this.textRefName;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getvalue() {
        return this.value;
    }

    /**
     * Sets the value of the textReferenceFormat property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setTextReferenceFormat(final String value) {
        this.textReferenceFormat = value;
    }

    /**
     * Sets the value of the textRefName property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setTextRefName(final String value) {
        this.textRefName = value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setvalue(final String value) {
        this.value = value;
    }

}
