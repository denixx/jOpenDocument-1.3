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
public class TextTime {

    protected String styleDataStyleName;
    protected String textFixed;
    protected String textTimeAdjust;
    protected String textTimeValue;
    protected String value;

    /**
     * Gets the value of the styleDataStyleName property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getStyleDataStyleName() {
        return this.styleDataStyleName;
    }

    /**
     * Gets the value of the textFixed property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getTextFixed() {
        if (this.textFixed == null) {
            return "false";
        } else {
            return this.textFixed;
        }
    }

    /**
     * Gets the value of the textTimeAdjust property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getTextTimeAdjust() {
        return this.textTimeAdjust;
    }

    /**
     * Gets the value of the textTimeValue property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getTextTimeValue() {
        return this.textTimeValue;
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
     * Sets the value of the styleDataStyleName property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setStyleDataStyleName(final String value) {
        this.styleDataStyleName = value;
    }

    /**
     * Sets the value of the textFixed property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setTextFixed(final String value) {
        this.textFixed = value;
    }

    /**
     * Sets the value of the textTimeAdjust property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setTextTimeAdjust(final String value) {
        this.textTimeAdjust = value;
    }

    /**
     * Sets the value of the textTimeValue property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setTextTimeValue(final String value) {
        this.textTimeValue = value;
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
