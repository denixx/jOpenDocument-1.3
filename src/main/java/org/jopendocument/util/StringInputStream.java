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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

/**
 * Permet de créer un InputStream depuis une String.
 * 
 * @author Sylvain CUAZ le 30 nov. 2004
 */
public class StringInputStream extends ByteArrayInputStream {

    public StringInputStream(String s) {
        super(s.getBytes());
    }

    public StringInputStream(String s, String charset) throws UnsupportedEncodingException {
        super(s.getBytes(charset));
    }

}