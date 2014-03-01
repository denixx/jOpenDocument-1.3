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

package org.jopendocument.model.office;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.jopendocument.model.draw.DrawFillImage;
import org.jopendocument.model.draw.DrawGradient;
import org.jopendocument.model.draw.DrawHatch;
import org.jopendocument.model.draw.DrawMarker;
import org.jopendocument.model.draw.DrawStrokeDash;
import org.jopendocument.model.draw.DrawTransparency;
import org.jopendocument.model.number.NumberBooleanStyle;
import org.jopendocument.model.number.NumberCurrencyStyle;
import org.jopendocument.model.number.NumberDateStyle;
import org.jopendocument.model.number.NumberNumberStyle;
import org.jopendocument.model.number.NumberPercentageStyle;
import org.jopendocument.model.number.NumberTextStyle;
import org.jopendocument.model.number.NumberTimeStyle;
import org.jopendocument.model.style.StyleDefaultStyle;
import org.jopendocument.model.style.StylePresentationPageLayout;
import org.jopendocument.model.style.StyleStyle;
import org.jopendocument.model.text.TextBibliographyConfiguration;
import org.jopendocument.model.text.TextEndnotesConfiguration;
import org.jopendocument.model.text.TextFootnotesConfiguration;
import org.jopendocument.model.text.TextLinenumberingConfiguration;
import org.jopendocument.model.text.TextListStyle;
import org.jopendocument.model.text.TextOutlineStyle;

/**
 * 
 */
public class OfficeStyles {

    List<StyleDefaultStyle> defaultStyles = new Vector<StyleDefaultStyle>();
    List<StyleStyle> styles = new Vector<StyleStyle>();
    protected List<Object> styleDefaultStyleOrStyleStyleOrTextListStyleOrNumberNumberStyleOrNumberCurrencyStyleOrNumberPercentageStyleOrNumberDateStyleOrNumberTimeStyleOrNumberBooleanStyleOrNumberTextStyleOrDrawGradientOrDrawHatchOrDrawFillImageOrDrawMarkerOrDrawStrokeDashOrStylePresentationPageLayoutOrDrawTransparencyOrTextOutlineStyleOrTextFootnotesConfigurationOrTextEndnotesConfigurationOrTextBibliographyConfigurationOrTextLinenumberingConfiguration;

    public void addDefaultStyle(final StyleDefaultStyle defaultStyle) {
        this.defaultStyles.add(defaultStyle);
    }
    public void addStyle(final StyleStyle style) {
        this.styles.add(style);
    }
    public StyleDefaultStyle getDefaultCellStyle() {
        for (final StyleDefaultStyle s : this.defaultStyles) {
            if (s.getStyleFamily().equals("table-cell")) {
                return s;
            }
        }
        return null;
    }

    /**
     * Gets the value of the
     * styleDefaultStyleOrStyleStyleOrTextListStyleOrNumberNumberStyleOrNumberCurrencyStyleOrNumberPercentageStyleOrNumberDateStyleOrNumberTimeStyleOrNumberBooleanStyleOrNumberTextStyleOrDrawGradientOrDrawHatchOrDrawFillImageOrDrawMarkerOrDrawStrokeDashOrStylePresentationPageLayoutOrDrawTransparencyOrTextOutlineStyleOrTextFootnotesConfigurationOrTextEndnotesConfigurationOrTextBibliographyConfigurationOrTextLinenumberingConfiguration
     * property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the
     * styleDefaultStyleOrStyleStyleOrTextListStyleOrNumberNumberStyleOrNumberCurrencyStyleOrNumberPercentageStyleOrNumberDateStyleOrNumberTimeStyleOrNumberBooleanStyleOrNumberTextStyleOrDrawGradientOrDrawHatchOrDrawFillImageOrDrawMarkerOrDrawStrokeDashOrStylePresentationPageLayoutOrDrawTransparencyOrTextOutlineStyleOrTextFootnotesConfigurationOrTextEndnotesConfigurationOrTextBibliographyConfigurationOrTextLinenumberingConfiguration
     * property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getStyleDefaultStyleOrStyleStyleOrTextListStyleOrNumberNumberStyleOrNumberCurrencyStyleOrNumberPercentageStyleOrNumberDateStyleOrNumberTimeStyleOrNumberBooleanStyleOrNumberTextStyleOrDrawGradientOrDrawHatchOrDrawFillImageOrDrawMarkerOrDrawStrokeDashOrStylePresentationPageLayoutOrDrawTransparencyOrTextOutlineStyleOrTextFootnotesConfigurationOrTextEndnotesConfigurationOrTextBibliographyConfigurationOrTextLinenumberingConfiguration()
     *         .add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link StyleDefaultStyle }
     * {@link StyleStyle } {@link TextListStyle } {@link NumberNumberStyle }
     * {@link NumberCurrencyStyle } {@link NumberPercentageStyle } {@link NumberDateStyle }
     * {@link NumberTimeStyle } {@link NumberBooleanStyle } {@link NumberTextStyle }
     * {@link DrawGradient } {@link DrawHatch } {@link DrawFillImage } {@link DrawMarker }
     * {@link DrawStrokeDash } {@link StylePresentationPageLayout } {@link DrawTransparency }
     * {@link TextOutlineStyle } {@link TextFootnotesConfiguration }
     * {@link TextEndnotesConfiguration } {@link TextBibliographyConfiguration }
     * {@link TextLinenumberingConfiguration }
     * 
     * 
     */
    public List<Object> getStyleDefaultStyleOrStyleStyleOrTextListStyleOrNumberNumberStyleOrNumberCurrencyStyleOrNumberPercentageStyleOrNumberDateStyleOrNumberTimeStyleOrNumberBooleanStyleOrNumberTextStyleOrDrawGradientOrDrawHatchOrDrawFillImageOrDrawMarkerOrDrawStrokeDashOrStylePresentationPageLayoutOrDrawTransparencyOrTextOutlineStyleOrTextFootnotesConfigurationOrTextEndnotesConfigurationOrTextBibliographyConfigurationOrTextLinenumberingConfiguration() {
        if (this.styleDefaultStyleOrStyleStyleOrTextListStyleOrNumberNumberStyleOrNumberCurrencyStyleOrNumberPercentageStyleOrNumberDateStyleOrNumberTimeStyleOrNumberBooleanStyleOrNumberTextStyleOrDrawGradientOrDrawHatchOrDrawFillImageOrDrawMarkerOrDrawStrokeDashOrStylePresentationPageLayoutOrDrawTransparencyOrTextOutlineStyleOrTextFootnotesConfigurationOrTextEndnotesConfigurationOrTextBibliographyConfigurationOrTextLinenumberingConfiguration == null) {
            this.styleDefaultStyleOrStyleStyleOrTextListStyleOrNumberNumberStyleOrNumberCurrencyStyleOrNumberPercentageStyleOrNumberDateStyleOrNumberTimeStyleOrNumberBooleanStyleOrNumberTextStyleOrDrawGradientOrDrawHatchOrDrawFillImageOrDrawMarkerOrDrawStrokeDashOrStylePresentationPageLayoutOrDrawTransparencyOrTextOutlineStyleOrTextFootnotesConfigurationOrTextEndnotesConfigurationOrTextBibliographyConfigurationOrTextLinenumberingConfiguration = new ArrayList<Object>();
        }
        return this.styleDefaultStyleOrStyleStyleOrTextListStyleOrNumberNumberStyleOrNumberCurrencyStyleOrNumberPercentageStyleOrNumberDateStyleOrNumberTimeStyleOrNumberBooleanStyleOrNumberTextStyleOrDrawGradientOrDrawHatchOrDrawFillImageOrDrawMarkerOrDrawStrokeDashOrStylePresentationPageLayoutOrDrawTransparencyOrTextOutlineStyleOrTextFootnotesConfigurationOrTextEndnotesConfigurationOrTextBibliographyConfigurationOrTextLinenumberingConfiguration;
    }

}
