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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.libreoffice.coooder.HighlightingException;
import org.libreoffice.coooder.XLanguage;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameContainer;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.style.XStyle;
import com.sun.star.style.XStyleFamiliesSupplier;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.xml.dom.NodeType;
import com.sun.star.xml.dom.XAttr;
import com.sun.star.xml.dom.XNamedNodeMap;
import com.sun.star.xml.dom.XNode;
import com.sun.star.xml.dom.XNodeList;


/**
 * A set of useful methods used in the highlighter implementation.
 *
 * @author Cedric Bosdonnat
 *
 */
public class Utils {

    private static final String UNIX_SEPARATOR = "\n";

    /**
     * Check if an array contains a given value.
     *
     * @param pArray the array where to look for the value
     * @param pItem the value to look for
     *
     * @return <code>true</code> if the value has been found in the array
     */
    public static boolean arrayContains(String[] pArray, String pItem) {
        boolean contained = false;

        int i = 0;
        while (!contained && i < pArray.length) {
            contained = pArray[i].equals(pItem);
            i++;
        }

        return contained;
    }

    /**
     * Move the cursor to the right with an offset indicated by an integer.
     *
     * <p>This method has to be used instead of casting from an integer to
     * a short: the result might be different.</p>
     *
     * @param pCursor the cursor to move
     * @param pOffset the number to characters to go after
     * @param pExpand <code>true</code> if the cursor expands the selection while
     *      moving, <code>false</code> otherwise.
     */
    public static void goRight(XTextCursor pCursor, int pOffset, boolean pExpand) {

        int i = pOffset;
        short move = 0;

        while (i > 0) {
            if (pOffset > Short.MAX_VALUE) {
                move = Short.MAX_VALUE;
                i -= Short.MAX_VALUE;
            } else {
                move = (short)i;
                i = 0;
            }
            pCursor.goRight(move, pExpand);
        }
    }

    /**
     * Set the given style on the selection of a cursor.
     *
     * @param pSelection the cursor for which to set the style
     * @param pStyle the style to set
     *
     * @throws Exception if the <code>CharStyleName</code> property can't be set
     *      on the cursor.
     */
    public static void setStyle(XTextCursor pSelection, String pStyle) throws Exception {
        XPropertySet props = (XPropertySet)UnoRuntime.queryInterface(
                XPropertySet.class, pSelection);
        props.setPropertyValue("CharStyleName", pStyle);
    }

    public static void createStyle(XTextDocument pDoc, String pStyle, XLanguage pLanguage) throws Exception {
        // Create the style in the document if missing
        XStyleFamiliesSupplier xStyleFamilies = (XStyleFamiliesSupplier)UnoRuntime.queryInterface(
                XStyleFamiliesSupplier.class, pDoc);
        Object o = xStyleFamilies.getStyleFamilies().getByName("CharacterStyles");
        XNameContainer xFamily = (XNameContainer)UnoRuntime.queryInterface(XNameContainer.class, o);

        if (!xFamily.hasByName(pStyle)) {
            // Get the style definition
            HashMap styles = ((LanguageImpl)pLanguage).getStyles();
            Style styleDef = (Style)styles.get(pStyle);
            if (styleDef == null) {
                styleDef = new Style();
            }

            // Create the style
            XMultiServiceFactory xFactory = (XMultiServiceFactory)UnoRuntime.queryInterface(
                    XMultiServiceFactory.class, pDoc);
            Object style = xFactory.createInstance("com.sun.star.style.CharacterStyle");
            XStyle xStyle = (XStyle)UnoRuntime.queryInterface(XStyle.class, style);

            XPropertySet xPropSet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, xStyle);

            xPropSet.setPropertyValue("CharColor", styleDef.getColor());
            xPropSet.setPropertyValue("CharWeight", styleDef.getFontWeight());
            xPropSet.setPropertyValue("CharPosture", styleDef.getFontSlant());

            // Adds the style to the family
            xFamily.insertByName(pStyle, xStyle);
        }
    }

    /**
     * Get the string between the start and stop position using the given cursor.
     *
     * <p>This method assumes that the cursor is at the position <code>0</code></p>
     *
     * @param pCursor the cursor to use
     * @param pStart the start position of the string
     * @param pStop the end position of the string
     *
     * @return the selected string
     */
    public static String getSelection(XTextCursor pCursor, int pStart, int pStop) {

        // Get the selected string
        Utils.goRight(pCursor, pStart, false);
        Utils.goRight(pCursor, pStop - pStart, true);

        return getSelection(pCursor);
    }

    /**
     * Returns the cursor selected string with unix end of lines
     *
     * @param pCursor the text cursor for the selection
     *
     * @return the string
     */
    public static String getSelection(XTextCursor pCursor) {
        String string = pCursor.getString();
        String osLF = System.getProperty("line.separator");
        if (!osLF.equals(UNIX_SEPARATOR)) {
            string = string.replaceAll(osLF, UNIX_SEPARATOR);
        }
        return string;
    }

    /**
     * Get the value of an attribute in a given node.
     *
     * @param pNode the node containing the attribute
     * @param pName the local name of the attribute to look for
     *
     * @return the attribute value
     *
     * @throws HighlightingException if the attribute is missing
     */
    public static String getAttribute(XNode pNode, String pName) throws HighlightingException {
        String result = null;

        XNamedNodeMap attrs = pNode.getAttributes();
        XNode idNode = attrs.getNamedItem(pName);
        if (idNode != null) {
            XAttr idAttr = (XAttr)UnoRuntime.queryInterface(XAttr.class, idNode);
            result = idAttr.getValue();
        } else {
            String message = MessageFormat.format("Expected attribute {0} not found in node {1}",
                    new Object[]{ pName, pNode.getNodeName() });
            throw new HighlightingException(message);
        }
        return result;
    }

    /**
     * Get the text values of some elements in a given node.
     *
     * @param pParentNode the node where to look for the values
     * @param pChildName the name of the elements containing the values to get
     * @param pMandatory <code>true</code> if the elements are mandatory
     *
     * @return the list of values.
     *
     * @throws HighlightingException if there is no value and they are mandatory
     */
    public static String[] getValues(XNode pParentNode, String pChildName,
            boolean pMandatory) throws HighlightingException {

        XNode valuesNode = getChild(pParentNode, pChildName);
        if (pMandatory && valuesNode == null) {
            String msg = MessageFormat.format("Element {0} is mandatory in {1}",
                    new String[]{ pChildName, pParentNode.getNodeName() });
            throw new HighlightingException(msg);
        }

        String[] values = new String[0];
        if (valuesNode != null) {
            values = getValues(valuesNode);
        }

        return values;
    }

    /**
     * Get the text content of the <strong>&lt;value&gt;</strong> elements contained in
     * the given node.
     *
     * @param pNode the node to analyze.
     *
     * @return the list of the values
     */
    public static String[] getValues(XNode pNode) {

        ArrayList values = new ArrayList();
        XNodeList children = pNode.getChildNodes();

        for (int i = 0, length = children.getLength(); i < length; i++) {
            XNode child = children.item(i);

            try {
                boolean isValueNode = child.getNodeName().equals(DefinitionsConstants.NODE_VALUE);
                String value = getTextValue(child);

                if (isValueNode && !value.equals("")) {
                    values.add(value);
                }
            } catch (java.lang.Exception e) {
                // Do nothing
            }
        }

        // Convert to String[]
        String[] aValues = new String[values.size()];
        for (int i = 0, length = values.size(); i < length; i++) {
            aValues[i] = (String)values.get(i);
        }

        return aValues;
    }

    /**
     * Find a child element from a given element using its local name.
     *
     * @param pParentNode the parent element for which to get a child
     * @param pChildName the local name of the child element to get.
     *
     * @return the element found or <code>null</code>.
     */
    public static XNode getChild(XNode pParentNode, String pChildName) {

        XNode node = null;

        XNodeList children = pParentNode.getChildNodes();
        int i = 0;

        while (i < children.getLength() && node == null) {
            XNode child = children.item(i);
            boolean isElement = child.getNodeType() == NodeType.ELEMENT_NODE;
            boolean isSearched = child.getNodeName().equals(pChildName);

            if (isElement && isSearched) {
                node = child;
            }
            i++;
        }

        return node;
    }

    /**
     * Get the text value of the first child of the given node.
     *
     * @param pNode the node from which to get the text
     *
     * @return the contained text or an empty string
     */
    public static String getTextValue(XNode pNode) {
        String value = "";

        if (pNode != null && pNode.hasChildNodes()) {
            XNode child = pNode.getFirstChild();
            boolean isTextNode = child != null && child.getNodeType() == NodeType.TEXT_NODE;

            if (isTextNode) {
                XNode text = (XNode)UnoRuntime.queryInterface(XNode.class, child);
                try {
                    value = text.getNodeValue();
                } catch (Exception e) {
                }
            }
        }
        return value;
    }
}
