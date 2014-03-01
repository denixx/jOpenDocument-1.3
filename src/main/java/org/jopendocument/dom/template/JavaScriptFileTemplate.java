/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 jOpenDocument, by ILM Informatique. All rights reserved.
 * 
 * The contents of this file are subject to the terms of the GNU General Public License Version 3
 * only ("GPL"). You may not use this file except in compliance with the License. You can obtain a
 * copy of the License at http://www.gnu.org/licenses/gpl-3.0.html See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each file.
 */

package org.jopendocument.dom.template;

import java.io.File;
import java.io.IOException;

import javax.script.ScriptEngineManager;

import org.jdom.JDOMException;
import org.jopendocument.dom.template.engine.ScriptEngineDataModel;

public class JavaScriptFileTemplate extends EngineTemplate {

    public JavaScriptFileTemplate(String fileName) throws IOException, TemplateException, JDOMException {
        this(new File(fileName));
    }

    public JavaScriptFileTemplate(File f) throws IOException, TemplateException, JDOMException {
        super(f, new ScriptEngineDataModel(new ScriptEngineManager().getEngineByName("javascript")));
    }
}
