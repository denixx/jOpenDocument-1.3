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
public class TextTocMark {

    protected String textOutlineLevel;
    protected String textStringValue;

    /**
     * Gets the value of the textOutlineLevel property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getTextOutlineLevel() {
        return this.textOutlineLevel;
    }

    /**
     * Gets the value of the textStringValue property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getTextStringValue() {
        return this.textStringValue;
    }

    /**
     * Sets the value of the textOutlineLevel property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setTextOutlineLevel(final String value) {
        this.textOutlineLevel = value;
    }

    /**
     * Sets the value of the textStringValue property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setTextStringValue(final String value) {
        this.textStringValue = value;
    }

}
