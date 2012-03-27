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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.libreoffice.coooder.HighlightingException;
import org.libreoffice.coooder.XLanguage;

import com.sun.star.io.XInputStream;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.ucb.XSimpleFileAccess2;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.xml.dom.XDocument;
import com.sun.star.xml.dom.XDocumentBuilder;
import com.sun.star.xml.dom.XNode;
import com.sun.star.xml.dom.XNodeList;


public final class LanguageImpl extends WeakBase implements XServiceInfo, XLanguage {

    private static final String IMPLEMENTATION_NAME = LanguageImpl.class.getName();
    private static final String[] SERVICE_NAMES = { "org.libreoffice.coooder.Language" };

    private XComponentContext mContext;

    private String mId;
    private String mName;

    private String[] mCommentSingle;
    private String[][] mCommentMulti;
    private String[] mHardquote = new String[0];
    private String[] mHardEscapes = new String[0];
    private String[] mQuotemarks;
    private String mEscapeChar;
    private String[] mRegexps;
    private String[][] mKeywords;
    private boolean mObjectOriented;
    private String[] mObjectSplitters;
    private String[][] mSymbols;
    private boolean mCommentCaseSensitivity;
    private HashMap mKeywordCaseSensitivity = new HashMap();
    private HashMap mStyles;


    public LanguageImpl(XComponentContext pContext) {
        mContext = pContext;
    };

    public static XSingleComponentFactory __getComponentFactory(String pImplementationName) {
        XSingleComponentFactory xFactory = null;

        if (pImplementationName.equals(IMPLEMENTATION_NAME)) {
            xFactory = Factory.createComponentFactory(LanguageImpl.class, SERVICE_NAMES);
        }
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo( XRegistryKey pRegistryKey ) {
        return Factory.writeRegistryServiceInfo(IMPLEMENTATION_NAME,
                                                SERVICE_NAMES, pRegistryKey);
    }


    //------------------------------------------ com.sun.star.lang.XServiceInfo


    public String getImplementationName() {
         return IMPLEMENTATION_NAME;
    }

    public boolean supportsService( String sService ) {
        int len = SERVICE_NAMES.length;

        for( int i=0; i < len; i++) {
            if (sService.equals(SERVICE_NAMES[i]))
                return true;
        }
        return false;
    }

    public String[] getSupportedServiceNames() {
        return SERVICE_NAMES;
    }

    //---------------------------------------- org.libreoffice.coooder.XLanguage


    public String getId() {
        return mId;
    }

    public void setId(String pId) {
        mId = pId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String pName) {
        mName = pName;
    }

    public String[] getCommentSingle() {
        return mCommentSingle;
    }

    public void setCommentSingle(String[] pComments) {
        mCommentSingle = pComments;
    }

    public String[][] getCommentMulti() {
        return mCommentMulti;
    }

    public void setCommentMulti(String[][] pComments) {
        mCommentMulti = pComments;
    }

    public String[] getHardquote() {
        return mHardquote;
    }

    public void setHardquote(String[] pHardquote) {
        mHardquote = pHardquote;
    }

    public String[] getHardEscapes() {
        return mHardEscapes;
    }

    public void setHardEscapes(String[] pHardEscapes) {
        mHardEscapes = pHardEscapes;
    }

    public String[] getQuotemarks() {
        return mQuotemarks;
    }

    public void setQuotemarks(String[] pQuotemarks) {
        mQuotemarks = pQuotemarks;
    }

    public String getEscapeChar() {
        return mEscapeChar;
    }

    public void setEscapeChar(String pEscapeChar) {
        mEscapeChar = pEscapeChar;
    }

    public String[] getRegexps() {
        return mRegexps;
    }

    public void setRegexps(String[] pRegexps) {
        mRegexps = pRegexps;
    }

    public String[][] getKeywords() {
        return mKeywords;
    }

    public void setKeywords(String[][] pKeywords) {
        mKeywords = pKeywords;
    }

    public boolean getObjectOriented() {
        return mObjectOriented;
    }

    public void setObjectOriented(boolean pObjectOriented) {
        mObjectOriented = pObjectOriented;
    }

    public String[] getObjectSplitters() {
        return mObjectSplitters;
    }

    public void setObjectSplitters(String[] pObjectSplitters) {
        mObjectSplitters = pObjectSplitters;
    }

    public String[][] getSymbols() {
        return mSymbols;
    }

    public void setSymbols(String[][] pSymbols) {
        mSymbols = pSymbols;
    }

    public boolean getCommentCaseSensitivity() {
        return mCommentCaseSensitivity;
    }

    public void setCommentCaseSensitivity(boolean pCommentCaseSensitivity) {
        mCommentCaseSensitivity = pCommentCaseSensitivity;
    }

    public boolean isKeywordCaseSensitive(int pKeywordGroup) {
        boolean caseSensitive = true;
        Integer key = Integer.valueOf(pKeywordGroup);
        if (mKeywordCaseSensitivity.containsKey(key)) {
            caseSensitive = ((Boolean)mKeywordCaseSensitivity.get(key)).booleanValue();
        }
        return caseSensitive;
    }

    public void setKeywordCaseSensitivity(int pKeywordGroup, boolean pCaseSensitive) {

        Integer key = Integer.valueOf(pKeywordGroup);
        Boolean value = Boolean.valueOf(pCaseSensitive);
        mKeywordCaseSensitivity.put(key, value);
    }

    public HashMap getStyles() {
        return mStyles;
    }

    public void defineFromXml(String pFileUrl) throws HighlightingException {

        long start = Calendar.getInstance().getTimeInMillis();

        XInputStream in = null;
        try {
            XMultiComponentFactory xFactory = mContext.getServiceManager();
            Object o = xFactory.createInstanceWithContext(
                    "com.sun.star.ucb.SimpleFileAccess", mContext);
            XSimpleFileAccess2 xFileAccess = (XSimpleFileAccess2)UnoRuntime.queryInterface(
                    XSimpleFileAccess2.class, o);
            in = xFileAccess.openFileRead(pFileUrl);

            // Parse the XML file
            o = xFactory.createInstanceWithContext(
                    "com.sun.star.xml.dom.DocumentBuilder", mContext);
            XDocumentBuilder xDocBuilder = (XDocumentBuilder)UnoRuntime.queryInterface(
                    XDocumentBuilder.class, o);

            XDocument xDom = xDocBuilder.parse(in);

            // Initialize the language service instance from the configuration
            XNode languageNode = xDom.getFirstChild();

            // Normalize the tree
            languageNode.normalize();

            if (languageNode.getNodeName().equals(DefinitionsConstants.NODE_LANGUAGE)) {

                // Initialize the main properties
                mId = Utils.getAttribute(languageNode, DefinitionsConstants.ATTR_ID);
                mName = Utils.getAttribute(languageNode, DefinitionsConstants.ATTR_NAME);

                String oo = Utils.getAttribute(languageNode, DefinitionsConstants.ATTR_OO);
                mObjectOriented = Boolean.parseBoolean(oo);

                mEscapeChar = Utils.getAttribute(languageNode, DefinitionsConstants.ATTR_ESCAPE_CHAR);

                // Map of map of ids
                HashMap ids = new HashMap();

                // Initialize the object splitters
                HashMap splittersIds = initSplitters(languageNode);
                ids.put(DefinitionsConstants.STYLE_MEMBER, splittersIds);

                // Initialize the comments
                HashMap commentsIds = initComments(languageNode);
                ids.put(DefinitionsConstants.STYLE_COMMENT, commentsIds);

                // Initialize the symbols
                HashMap symbolsIds = initSymbols(languageNode);
                ids.put(DefinitionsConstants.STYLE_SYMBOL, symbolsIds);

                // Initialize the quotemarks
                mQuotemarks = Utils.getValues(languageNode,
                        DefinitionsConstants.NODE_QUOTEMARKS, true);

                // Initialize the hardquotes
                mHardquote = Utils.getValues(languageNode,
                        DefinitionsConstants.NODE_HARDQUOTES, false);

                // Initialize the hardquote escapes
                mHardEscapes = Utils.getValues(languageNode,
                        DefinitionsConstants.NODE_HARDESCAPES, false);

                // Initialize the keywords
                HashMap keywordsIds = initKeywords(languageNode);
                ids.put(DefinitionsConstants.STYLE_KEYWORD, keywordsIds);

                // Initialize the regexps
                HashMap regexpIds = initRegexps(languageNode);
                ids.put(DefinitionsConstants.STYLE_REGEXP, regexpIds);

                // Initialize the styles informations
                initStyles(languageNode, ids);
            }

        } catch (Exception e) {
            // Raises Highlighting exception
            throw new HighlightingException(e.getMessage());
        } finally {
            // Close the stream properly
            try { in.closeInput(); } catch (Exception e) { }

            long end = Calendar.getInstance().getTimeInMillis();
            long duration = end - start;

            System.out.println("Language file parsing: (in ms)" + duration);
        }
    }

    /**
     * Initializes the object splitters from the DOM language node and build a map
     * of the splitters IDs.
     *
     * @param pLanguageNode the DOM language node.
     *
     * @return a map of {@link String} to {@link Integer} mapping the object
     *      splitters ID to their position in the splitters array.
     */
    private HashMap initSplitters(XNode pLanguageNode) {
        HashMap ids = new HashMap();

        // Get the splitters into a temporary list
        ArrayList splitters = new ArrayList();
        XNode splittersNode = Utils.getChild(pLanguageNode,
                DefinitionsConstants.NODE_OO_SPLITTERS);
        if (splittersNode != null) {
            XNodeList children = splittersNode.getChildNodes();
            for (int i = 0, length = children.getLength(); i < length; i++) {
                XNode regexpNode = children.item(i);

                if (regexpNode.getNodeName().equals(
                        DefinitionsConstants.NODE_OO_SPLITTER)) {
                    try {
                        // Get the ID
                        String id = Utils.getAttribute(regexpNode,
                                DefinitionsConstants.ATTR_ID);

                        // Get the value
                        String value = Utils.getAttribute(regexpNode,
                                DefinitionsConstants.ATTR_VALUE);

                        // Update the list and map
                        splitters.add(value);
                        int pos = splitters.size() - 1;
                        ids.put(id, Integer.valueOf(pos));

                    } catch (HighlightingException e) {
                        // Do not add the splitter without ID or value
                    }
                }
            }
        }

        // Set the splitters
        mObjectSplitters = new String[splitters.size()];
        for (int i = 0, length = mObjectSplitters.length; i < length; i++) {
            mObjectSplitters[i] = (String) splitters.get(i);
        }

        return ids;
    }

    /**
     * Sets the comments and build a mapping with their IDs for use to associate the styles.
     *
     * @param pLanguageNode the language DOM node from which to extract the comments
     *
     * @return a map of {@link String} to {@link Integer} mapping the single comments ID
     *      to their position in the single comments array.
     */
    private HashMap initComments(XNode pLanguageNode) {

        XNode commentsNode = Utils.getChild(pLanguageNode, DefinitionsConstants.NODE_COMMENTS);

        // Temporary arrays for the comments
        ArrayList singleComments = new ArrayList();
        ArrayList multiComments = new ArrayList();

        // Map for the single-line comments ids
        HashMap ids = new HashMap();

        if (commentsNode != null) {
            // Get the case sensitivity of the comments
            try {
                String value = Utils.getAttribute(commentsNode, DefinitionsConstants.ATTR_CASE_SENSITIVE);
                mCommentCaseSensitivity = Boolean.parseBoolean(value);
            } catch (Exception e) {
                // Default is true
                mCommentCaseSensitivity = true;
            }

            // Get the comments themselves
            XNodeList children = commentsNode.getChildNodes();
            for (int i = 0, length = children.getLength(); i < length; i++) {
                XNode commentNode = children.item(i);

                boolean isSingle = commentNode.getNodeName().equals(
                        DefinitionsConstants.NODE_SINGLE);
                boolean isMultiple = commentNode.getNodeName().equals(
                        DefinitionsConstants.NODE_MULTIPLE);

                if (isSingle) {
                    // Get the opening symbol
                    XNode openNode = Utils.getChild(commentNode, DefinitionsConstants.NODE_OPENING);
                    String open = Utils.getTextValue(openNode);

                    // Update the map and list
                    try {
                        String id = Utils.getAttribute(commentNode, DefinitionsConstants.ATTR_ID);
                        singleComments.add(open);

                        int pos = singleComments.size() - 1;
                        ids.put(id, Integer.valueOf(pos));
                    } catch (HighlightingException e) {
                        // Do not add the comment: it has no ID
                    }
                }

                if (isMultiple) {
                    // Get the opening symbol
                    XNode openNode = Utils.getChild(commentNode, DefinitionsConstants.NODE_OPENING);
                    String open = Utils.getTextValue(openNode);

                    // Get the closing symbol
                    XNode closeNode = Utils.getChild(commentNode, DefinitionsConstants.NODE_CLOSING);
                    String close = Utils.getTextValue(closeNode);

                    // Update the list
                    multiComments.add(new String[]{open, close});
                }
            }
        }

        // Set the single comments from their temporary list
        mCommentSingle = new String[singleComments.size()];
        for (int i = 0, length = mCommentSingle.length; i < length; i++) {
            mCommentSingle[i] = (String) singleComments.get(i);
        }

        // Set the multiple comments from their temporary list
        mCommentMulti = new String[multiComments.size()][];
        for (int i = 0, length = mCommentMulti.length; i < length; i++) {
            mCommentMulti[i] = (String[]) multiComments.get(i);
        }

        return ids;
    }

    /**
     * Initializes the keywords from the DOM language node and build a map
     * of the keywords sets IDs.
     *
     * @param pLanguageNode the DOM language node.
     *
     * @return a map of {@link String} to {@link Integer} mapping the keywords sets ID
     *      to their position in the keywords array.
     */
    private HashMap initKeywords(XNode pLanguageNode) {

        HashMap ids = new HashMap();
        XNode keywordsNode = Utils.getChild(pLanguageNode,
                DefinitionsConstants.NODE_KEYWORDS);

        // Get the keywords in a temporary list
        ArrayList keywordSets = new ArrayList();
        if (keywordsNode != null) {
            XNodeList children = keywordsNode.getChildNodes();
            for (int i = 0, length = children.getLength(); i < length; i++) {
                XNode keywordsSet = children.item(i);
                SetNode set = new SetNode(keywordsSet);

                if (set.getId() != null && set.getValues() != null) {
                    // Update the list and map
                    keywordSets.add(set.getValues());
                    int pos = keywordSets.size() - 1;
                    ids.put(set.getId(), Integer.valueOf(pos));

                    setKeywordCaseSensitivity(pos, set.isCaseSensitive());
                }
            }
        }

        // Set the keywords from the temporary list
        String[][] aKeywords = new String[keywordSets.size()][];
        for (int i = 0, length = aKeywords.length; i < length; i++) {
            aKeywords[i] = (String[])keywordSets.get(i);
        }
        mKeywords = aKeywords;

        return ids;
    }

    /**
     * Initializes the symbols from the DOM language node and build a map
     * of the symbols sets IDs.
     *
     * @param pLanguageNode the DOM language node.
     *
     * @return a map of {@link String} to {@link Integer} mapping the symbols sets ID
     *      to their position in the symbols array.
     */
    private HashMap initSymbols(XNode pLanguageNode) {

        HashMap ids = new HashMap();
        XNode symbolsNode = Utils.getChild(pLanguageNode,
                DefinitionsConstants.NODE_SYMBOLS);

        // Get the keywords in a temporary list
        ArrayList symbolsSets = new ArrayList();
        if (symbolsNode != null) {
            XNodeList children = symbolsNode.getChildNodes();
            for (int i = 0, length = children.getLength(); i < length; i++) {
                XNode symbolsSet = children.item(i);
                SetNode set = new SetNode(symbolsSet);

                if (set.getId() != null && set.getValues() != null) {
                    // Update the list and map
                    symbolsSets.add(set.getValues());
                    int pos = symbolsSets.size() - 1;
                    ids.put(set.getId(), Integer.valueOf(pos));
                }
            }
        }

        // Set the keywords from the temporary list
        String[][] aSymbols = new String[symbolsSets.size()][];
        for (int i = 0, length = aSymbols.length; i < length; i++) {
            aSymbols[i] = (String[])symbolsSets.get(i);
        }
        mSymbols = aSymbols;

        return ids;
    }

    /**
     * Initializes the regexps from the DOM language node and build a map
     * of the regexps IDs.
     *
     * @param pLanguageNode the DOM language node.
     *
     * @return a map of {@link String} to {@link Integer} mapping the regexps ID
     *      to their position in the regexps array.
     */
    private HashMap initRegexps(XNode pLanguageNode) {

        HashMap ids = new HashMap();

        // Get the regexps into a temporary list
        ArrayList regexps = new ArrayList();
        XNode regexpsNode = Utils.getChild(pLanguageNode,
                DefinitionsConstants.NODE_REGEXPS);
        if (regexpsNode != null) {
            XNodeList children = regexpsNode.getChildNodes();
            for (int i = 0, length = children.getLength(); i < length; i++) {
                XNode regexpNode = children.item(i);

                if (regexpNode.getNodeName().equals(
                        DefinitionsConstants.NODE_REGEXP)) {
                    try {
                        // Get the ID
                        String id = Utils.getAttribute(regexpNode,
                                DefinitionsConstants.ATTR_ID);

                        // Get the value
                        String value = Utils.getAttribute(regexpNode,
                                DefinitionsConstants.ATTR_VALUE);

                        // Update the list and map
                        regexps.add(value);
                        int pos = regexps.size() - 1;
                        ids.put(id, Integer.valueOf(pos));

                    } catch (HighlightingException e) {
                        // Do not add the regexp without ID or value
                    }
                }
            }
        }

        // Set the regexps
        String[] aRegexps = new String[regexps.size()];
        for (int i = 0, length = aRegexps.length; i < length; i++) {
            aRegexps[i] = (String) regexps.get(i);
        }
        mRegexps = aRegexps;

        return ids;
    }

    private void initStyles(XNode pLanguageNode, HashMap pIds) {

        XNode stylesNode = Utils.getChild(pLanguageNode, DefinitionsConstants.NODE_STYLES);

        // Get all the defined styles in a structure
        XNodeList children = stylesNode.getChildNodes();
        mStyles = new HashMap();
        for (int i = 0, length = children.getLength(); i < length; i++) {
            XNode child = children.item(i);

            if (child.getNodeName().equals(
                    DefinitionsConstants.NODE_STYLE)) {

                boolean bold = false;
                try {
                    String value = Utils.getAttribute(child, DefinitionsConstants.ATTR_BOLD);
                    bold = Boolean.parseBoolean(value);
                } catch (HighlightingException e) {
                    bold = false;
                }

                boolean italic = false;
                try {
                    String value = Utils.getAttribute(child, DefinitionsConstants.ATTR_ITALIC);
                    italic = Boolean.parseBoolean(value);
                } catch (HighlightingException e) {
                    italic = false;
                }

                try {
                    String element = Utils.getAttribute(child, DefinitionsConstants.ATTR_ELEMENT);
                    String color = Utils.getAttribute(child, DefinitionsConstants.ATTR_COLOR);

                    // Correct the element for the single comments, regexps, splitters,
                    // and keywords
                    element = computeStyleName(element, pIds);

                    mStyles.put(element, new Style(color, bold, italic));
                } catch (HighlightingException e) {
                    // Do not add the style if one of the mandatory attribute is missing
                }
            }
        }

        // Loop over the ids to see if there is no missing style
        Iterator iter = pIds.entrySet().iterator();
        while (iter.hasNext()) {
            Entry entry = (Entry)iter.next();
            String style = (String)entry.getKey();
            HashMap ids = (HashMap)entry.getValue();

            addMissingStyles(ids, style);
        }
    }

    private void addMissingStyles(HashMap pIds, String pStyle) {
        Iterator iter = pIds.values().iterator();
        while (iter.hasNext()) {
            Integer value = (Integer)iter.next();

            String styleName = pStyle + value.toString() + "_" + getId();

            if (!mStyles.containsKey(styleName)) {
                mStyles.put(styleName, new Style());
            }
        }
    }

    private String computeStyleName(String pElement, HashMap pIds) {

        String styleName = null;

        Iterator iter = pIds.entrySet().iterator();
        while (iter.hasNext() && styleName == null) {
            Entry entry = (Entry)iter.next();
            HashMap ids = (HashMap)entry.getValue();

            if (ids.containsKey(pElement)) {
                Integer id = (Integer)ids.get(pElement);
                styleName = (String)entry.getKey() + id.toString();
            }
        }

        if (styleName == null) {
            styleName = pElement;
        }

        return styleName + "_" + getId();
    }
}
