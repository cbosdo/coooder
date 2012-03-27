/*
 *   LibreOffice extension for syntax highlighting
 *   Copyright (C) 2008  Cédric Bosdonnat cedric.bosdonnat.ooo@free.fr
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation;
 *   version 2 of the License.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.libreoffice.coooder.comp;

import org.libreoffice.coooder.HighlightingException;

import com.sun.star.xml.dom.XNode;

public class SetNode {

    private boolean mCaseSensitive;
    private String mId;
    private String[] mValues;

    public SetNode (XNode pSetXmlNode) {

        if (pSetXmlNode.getNodeName().equals(DefinitionsConstants.NODE_SET)) {
            // Check for case sensitivity
            try {
                String value = Utils.getAttribute(pSetXmlNode, DefinitionsConstants.ATTR_CASE_SENSITIVE);
                mCaseSensitive = Boolean.parseBoolean(value);
            } catch (HighlightingException e) {
                // Default value is true
                mCaseSensitive = true;
            }

            // Get the keyword set id
            try {
                mId = Utils.getAttribute(pSetXmlNode, DefinitionsConstants.ATTR_ID);

                // Get the keywords
                mValues = Utils.getValues(pSetXmlNode);
            } catch (HighlightingException e) {
                // No ID: no keywordset
            }
        }
    }

    public boolean isCaseSensitive() {
        return mCaseSensitive;
    }

    public String getId() {
        return mId;
    }

    public String[] getValues() {
        return mValues;
    }
}
