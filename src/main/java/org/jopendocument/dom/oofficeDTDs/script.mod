<!--

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  
  Copyright 2008 by Sun Microsystems, Inc.
 
  OpenOffice.org - a multi-platform office productivity suite
 
  $RCSfile: script.mod,v $
 
  $Revision: 1.11 $
 
  This file is part of OpenOffice.org.
 
  OpenOffice.org is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License version 3
  only, as published by the Free Software Foundation.
 
  OpenOffice.org is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License version 3 for more details
  (a copy is included in the LICENSE file that accompanied this code).
 
  You should have received a copy of the GNU Lesser General Public License
  version 3 along with OpenOffice.org.  If not, see
  <http://www.openoffice.org/license.html>
  for a copy of the LGPLv3 License.
 
-->

<!ELEMENT script:libraries (script:library-embedded | script:library-linked)*>
<!ATTLIST script:libraries xmlns:script CDATA #FIXED "http://openoffice.org/2000/script">
<!ATTLIST script:libraries xmlns:xlink CDATA #FIXED "http://www.w3.org/1999/xlink">

<!ENTITY % boolean "(true|false)">

<!ELEMENT script:library-embedded (script:module*)>
<!ATTLIST script:library-embedded script:name %string; #REQUIRED>
<!ATTLIST script:library-embedded script:readonly %boolean; #IMPLIED>

<!ELEMENT script:library-linked EMPTY>
<!ATTLIST script:library-linked script:name %string; #REQUIRED>
<!ATTLIST script:library-linked xlink:href %string; #REQUIRED>
<!ATTLIST script:library-linked xlink:type (simple) #FIXED "simple">
<!ATTLIST script:library-linked script:readonly %boolean; #IMPLIED>

<!ELEMENT script:module (script:source-code)>
<!ATTLIST script:module script:name %string; #REQUIRED>

<!ELEMENT script:source-code (#PCDATA)>


<!ENTITY % script-language "script:language %string; #REQUIRED">
<!ENTITY % event-name "script:event-name %string; #REQUIRED">
<!ENTITY % location "script:location (document|application) #REQUIRED">
<!ENTITY % macro-name "script:macro-name %string; #REQUIRED">

<!ELEMENT script:event (#PCDATA)>
<!ATTLIST script:event %script-language;
                       %event-name;
                       %location;
					   %macro-name;>
