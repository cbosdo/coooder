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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.libreoffice.coooder.HighlightingException;
import org.libreoffice.coooder.XHighlighter;
import org.libreoffice.coooder.XLanguage;

import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.task.XStatusIndicator;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Highlighter implementation using the GeSHi algorithm adapted to LibreOffice API.
 *
 * @author cbosdonnat
 */
public final class HighlighterImpl extends WeakBase implements XServiceInfo, XHighlighter {

    private static final String IMPLEMENTATION_NAME = HighlighterImpl.class.getName();
    private static final String[] SERVICE_NAMES = { "org.libreoffice.coooder.Highlighter" };

    private static final int OPEN = 0;
    private static final int CLOSE = 1;

    private static final String NUMBER_REGEX = "[-+]?\\b(?:[0-9]*\\.)?[0-9]+\\b";


    private final XComponentContext mContext;
    private XLanguage mLanguage;

    private CompiledPatternsCache mPatternsCache;

    private XTextRange mSelectionStart;
    private XTextDocument mTextDocument;
    private int mLength;

    private XStatusIndicator mStatus;

    public HighlighterImpl(XComponentContext pContext) {
        mContext = pContext;
        mPatternsCache = new CompiledPatternsCache();
    };

    public static XSingleComponentFactory __getComponentFactory( String pImplementationName ) {
        XSingleComponentFactory xFactory = null;

        if (pImplementationName.equals(IMPLEMENTATION_NAME)) {
            xFactory = Factory.createComponentFactory(HighlighterImpl.class, SERVICE_NAMES);
        }
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo(XRegistryKey pRegistryKey) {
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


    //------------------------------------- org.libreoffice.coooder.XHighlighter

    public XLanguage getLanguage() {
        return mLanguage;
    }

    public void setLanguage(XLanguage pLanguage) {
        mLanguage = pLanguage;
    }

    public void setStatusIndicator(XStatusIndicator pStatus) {
        mStatus = pStatus;
    }

    public void parse() throws HighlightingException {
        XMultiComponentFactory mngr = mContext.getServiceManager();
        try {
            // Start with a clean patterns cache
            mPatternsCache.cleanCache();

            updateProgress(0);

            boolean COMMENT_MATCHED = false;
            int parseStartPos = 0;
            int parseEndPos = 0;

            // Get the visual cursor
            XTextViewCursor selection = null;

            Object o = mngr.createInstanceWithContext("com.sun.star.frame.Desktop", mContext);
            XDesktop desktop = (XDesktop)UnoRuntime.queryInterface(XDesktop.class, o);

            XComponent component = desktop.getCurrentComponent();
            XServiceInfo compInfos = (XServiceInfo)UnoRuntime.queryInterface(XServiceInfo.class, component);
            if (compInfos.supportsService("com.sun.star.text.TextDocument")) {
                XModel model = (XModel)UnoRuntime.queryInterface(XModel.class, component);
                XController controller = model.getCurrentController();
                XTextViewCursorSupplier cursSupplier = (XTextViewCursorSupplier)UnoRuntime.queryInterface(
                        XTextViewCursorSupplier.class, controller);
                selection = cursSupplier.getViewCursor();
            }
            String selectedString = Utils.getSelection(selection);

            mSelectionStart = selection.getStart();
            mLength = selectedString.length();

            // Create an invisible cursor.
            mTextDocument = (XTextDocument)UnoRuntime.queryInterface(
                    XTextDocument.class, component);
            XTextCursor cursor = mTextDocument.getText().createTextCursor();

            // Iterate over each character of the selected text
            for (int i = 0; i < selectedString.length(); i++) {

                String car = selectedString.substring(i, i + 1);
                String hq = "";
                if (mLanguage.getHardquote().length > 0) {
                    hq = mLanguage.getHardquote()[0];
                }

                if (Utils.arrayContains(mLanguage.getQuotemarks(), car)) {
                    // A string has been found

                    // Parse the non-string part before this string
                    parseNonString(cursor, parseStartPos, parseEndPos);

                    // Get the end of the string
                    String escape = "";
                    if (!mLanguage.getEscapeChar().equals("")) {
                        escape = mLanguage.getEscapeChar();
                    }
                    int closePos = getClosePos(selectedString, i + 1, car, escape);

                    String style = getStyleName(DefinitionsConstants.STYLE_STRING, null);
                    setStyle(cursor, style, i, closePos + 1);

                    // Parse the string to highlight escaped characters
                    parseString(cursor, i + 1, closePos);

                    i = closePos;

                    // Set the positions for non string parsing for after the string
                    parseStartPos = closePos + 1;
                    updateProgress(Math.round((parseStartPos * 100) / mLength));

                } else if (!hq.equals("") &&
                        i + hq.length() < selectedString.length() &&
                        selectedString.substring(i, i + hq.length()).equals(hq)) {
                    // The start of a hard quoted string

                    // Parse the non-string part before this string
                    parseNonString(cursor, parseStartPos, parseEndPos);

                    // Get the end of the hardquoted string
                    String close = mLanguage.getHardquote()[1];
                    i += hq.length() - 1;
                    int closePos = getHardquotedClosePos(selectedString, i + hq.length(),
                            close, mLanguage.getHardEscapes());

                    // Set the string style on the characters
                    String style = getStyleName(DefinitionsConstants.STYLE_STRING, null);
                    setStyle(cursor, style, i - 1, closePos + close.length());

                    // Parse the hardquoted string
                    parseHarquoted(cursor, i + hq.length() - 1, closePos + 1);
                    i = closePos;

                    // Set the positions for non string parsing for after the string
                    parseStartPos = closePos + 1;
                    updateProgress(Math.round((parseStartPos * 100) / mLength));

                } else {
                    // Is this a multiline comment?
                    for (int j = 0; j < mLanguage.getCommentMulti().length; j++) {
                        String open = mLanguage.getCommentMulti()[j][OPEN];
                        String close = mLanguage.getCommentMulti()[j][CLOSE];

                        // Get the text of the open delimiter length from the cursor
                        String test_str = "";
                        if (i + open.length() < selectedString.length()) {
                            test_str = selectedString.substring(i, i + open.length());
                        }

                        if (open.equals(test_str)) {
                            COMMENT_MATCHED = true;

                            // Parse the non-string part before this comment
                            parseNonString(cursor, parseStartPos, parseEndPos);

                            // Find the position of the close element
                            int closePos = selectedString.indexOf(close, i + open.length());

                            if (closePos == -1) {
                                closePos = selectedString.length();
                            } else {
                                closePos -= open.length() - close.length();
                            }

                            // Set the comment style on the characters
                            String style = getStyleName(DefinitionsConstants.STYLE_COMMENT_MULTI, null);
                            setStyle(cursor, style, i, closePos + close.length());

                            // Short-cut through all the multiline code
                            i = closePos + open.length() - 1;

                            // Set the parse positions for after the comment
                            parseStartPos = i + 1;
                            updateProgress(Math.round((parseStartPos * 100) / mLength));

                            break;
                        }
                    }

                    // If we haven't matched a multiline comment, try single-line comments
                    if (!COMMENT_MATCHED) {
                        for (int j = 0; j < mLanguage.getCommentSingle().length; j++) {
                            String commentMark = mLanguage.getCommentSingle()[j];
                            String test_str = "";
                            if (i + commentMark.length()  < selectedString.length()) {
                                test_str = selectedString.substring(i, i + commentMark.length());
                            }

                            boolean match = commentMark.equals(test_str);
                            // Check for case sensitivity
                            if (!mLanguage.getCommentCaseSensitivity()) {
                                match = commentMark.toLowerCase().equals(test_str.toLowerCase());
                            }
                            if (match) {

                                // Parse the non-string part before this comment
                                parseNonString(cursor, parseStartPos, parseEndPos);

                                COMMENT_MATCHED = true;

                                // Get the end of the paragraph
                                cursor.gotoRange(mSelectionStart, false);
                                Utils.goRight(cursor, i, false);

                                XParagraphCursor paraCursor = (XParagraphCursor)UnoRuntime.queryInterface(
                                        XParagraphCursor.class, cursor);
                                paraCursor.gotoEndOfParagraph(true);

                                // Set the comment style on the characters
                                String style = getStyleName(DefinitionsConstants.STYLE_COMMENT, Integer.valueOf(j));
                                Utils.createStyle(mTextDocument, style, mLanguage);
                                Utils.setStyle(paraCursor, style);

                                int length = paraCursor.getString().length();
                                i += length;

                                // Set the parse positions for the after the comment
                                parseStartPos = i;
                                updateProgress(Math.round((parseStartPos * 100) / mLength));
                            }
                        }
                    }
                }

                parseEndPos = i + 1;

                // Where are we adding this char?
                if (COMMENT_MATCHED) {
                    COMMENT_MATCHED = false;
                }
            }

            parseNonString(cursor, parseStartPos, parseEndPos);

            updateProgress(100);

        } catch (Exception e) {
            throw new HighlightingException("Parsing error", e);
        }
    }


    //-------------------------------------------------------- Internal methods

    private void updateProgress(int pValue) {
        if (mStatus != null) {
            mStatus.setValue(pValue);
        }
    }

    private void parseNonString(XTextCursor pCursor, int pStart, int pEnd) throws Exception {

        // Get the selected String
        pCursor.gotoRange(mSelectionStart, false);
        String selected = Utils.getSelection(pCursor, pStart, pEnd);

        // Use Matcher.find() and its regionStart and regionEnd fields for all this method

        // Regular expressions
        // Loop over the language's regular expressions and highlight them
        for (int i = 0; i < mLanguage.getRegexps().length; i++) {
            String regexp = mLanguage.getRegexps()[i];

            // FIXME A Regexp can be more complex than just a simple string (eg: css.php)
            String style = getStyleName(DefinitionsConstants.STYLE_REGEXP, Integer.valueOf(i));
            replaceStyle(style, regexp, 0, 0, selected, pCursor, pStart);
        }

        // Look for the numbers and highlight the numbers
        String style = getStyleName(DefinitionsConstants.STYLE_NUMBER, null);
        replaceStyle(style, NUMBER_REGEX, 0, 0, selected, pCursor, pStart);

        // Loop over the keywords and their categories to highlight them
        String[][] keywords = mLanguage.getKeywords();
        for (int i = 0; i < keywords.length; i++) {
            String[] keywordset = keywords[i];
            for (int j = 0; j < keywordset.length; j++) {

                int flags = 0;
                // Check if the keyword is case insensitive
                if (!mLanguage.isKeywordCaseSensitive(i)) {
                    flags = Pattern.CASE_INSENSITIVE;
                }

                String keyword = keywordset[j];
                style = getStyleName(DefinitionsConstants.STYLE_KEYWORD, Integer.valueOf(i));

                // Escapes the possible leading ? to avoid breaking the regexp
                if (keyword.startsWith("?")) {
                    keyword = "\\" + keyword;
                }

                String regexp = "([^a-zA-Z0-9\\$_\\|\\#;>|^]|^)(" + keyword
                    + ")(?=[^a-zA-Z0-9_<\\|%\\-&]|$)";

                replaceStyle(style, regexp, flags, 2, selected, pCursor, pStart);
            }
        }

        // Highlight the object's methods and fields
        if (mLanguage.getObjectOriented()) {
            for (int i = 0; i < mLanguage.getObjectSplitters().length; i++) {
                String splitter = mLanguage.getObjectSplitters()[i];
                if (selected.contains(splitter)) {
                    String regexp = "(" + Pattern.quote(splitter) + "[\\s]*)([a-zA-Z\\*\\(][a-zA-Z0-9_\\*]*)";
                    style = getStyleName(DefinitionsConstants.STYLE_MEMBER, Integer.valueOf(i));
                    replaceStyle(style, regexp, 0, 2, selected, pCursor, pStart);
                }
            }
        }

        // Highlight the symbols
        for (int i = 0; i < mLanguage.getSymbols().length; i++) {
            String[] symbolsSet = mLanguage.getSymbols()[i];
            for (int j = 0; j < symbolsSet.length; j++) {
                String regexp = Pattern.quote(symbolsSet[j]);
                style = getStyleName(DefinitionsConstants.STYLE_SYMBOL, Integer.valueOf(i));
                replaceStyle(style, regexp, 0, 0, selected, pCursor, pStart);
            }
        }
    }

    /**
     * Parse a string and highlight the escaped characters and the
     * character following them.
     *
     * @param pCursor the cursor
     * @param pStart the start position of the string
     * @param pStop the end position of the string
     *
     * @throws Exception if the styles can't be set on the escaped strings.
     */
    private void parseString(XTextCursor pCursor, int pStart, int pStop) throws Exception {

        if (!mLanguage.getEscapeChar().equals("")) {
            // Get the selected String
            pCursor.gotoRange(mSelectionStart, false);
            String selected = Utils.getSelection(pCursor, pStart, pStop);

            // Highlight the escaped characters
            String escapeChar = mLanguage.getEscapeChar();
            for (int i = 0; i < selected.length(); i++) {

                if (i < selected.length() - 1 &&
                        selected.subSequence(i, i + 1).equals(escapeChar)) {
                    // Highlight with the next character
                    String style = getStyleName(DefinitionsConstants.STYLE_ESCAPED, null);
                    setStyle(pCursor, style,
                            pStart + i, pStart + i + 2);
                    i++;
                }
            }
        }
    }

    /**
     * Parse a hardquoted string and highlight the escaped strings.
     *
     * @param pCursor the cursor
     * @param pStart the start position of the string
     * @param pStop the end position of the string
     *
     * @throws Exception if the styles can't be set on the escaped strings.
     */
    private void parseHarquoted(XTextCursor pCursor, int pStart, int pStop) throws Exception {
        String[] hardescapes = mLanguage.getHardEscapes();

        pCursor.gotoRange(mSelectionStart, false);
        String selected = Utils.getSelection(pCursor, pStart, pStop);

        // Highlight the hardescaped strings
        for (int i = 0; i < selected.length(); i++) {
            String hardescaped = "";
            int j = 0;
            while (hardescaped.equals("") && j < hardescapes.length) {
                String hardescape = hardescapes[j];
                if (hardescape.length() + i < selected.length()) {
                    String test_str = selected.substring(i, i + hardescape.length());
                    if (test_str.equals(hardescape)) {
                        hardescaped = test_str;
                    }
                }
                j++;
            }

            if (!hardescaped.equals("")) {
                String style = getStyleName(DefinitionsConstants.STYLE_ESCAPED, null);
                setStyle(pCursor, style, pStart + i,
                        pStart + i + hardescaped.length());
            }
        }
    }

    /**
     * Set the given character style to all the elements matching the regexp.
     *
     * @param pStyle the style to apply
     * @param pRegexp the regexp to match
     * @param pFlags the regexp flags
     * @param pGroup the group of the regexp on which to apply the style
     * @param pSelected the text to search
     * @param pCursor the text cursor
     * @param pStart the start position of the text to search relative to the selection start
     *
     * @throws Exception if anything wrong happens
     */
    private void replaceStyle(String pStyle, String pRegexp, int pFlags, int pGroup, String pSelected,
            XTextCursor pCursor, int pStart) throws Exception {
        Matcher matcher = mPatternsCache.getPattern(pRegexp, pFlags).matcher(pSelected);
        while (matcher.find()) {
            int start = matcher.start(pGroup);
            int end = matcher.end(pGroup);

            setStyle(pCursor, pStyle, pStart + start, pStart + end);
        }
    }

    /**
     * Finds the first non-escaped occurrence of the close string in the
     * selection after the start position.
     *
     * @param pSelected the selection where to look for the close string
     * @param pStart the position from which to start searching
     * @param pClose the close string to look for
     * @param pEscape the escape sequence
     *
     * @return the position of the close string or <code>-1</code>
     */
    private int getClosePos(String pSelected, int pStart, String pClose, String pEscape) {

        int closePos = -1;

        boolean escape_open = false;

        int i = pStart;
        while (closePos < 0 && i < pSelected.length()) {

            String test_close = pSelected.substring(i, i + pClose.length());
            if (!escape_open && test_close.equals(pClose)) {
                // Test the closing char
                closePos = i;
            } else if (!escape_open) {

                // Test for an escape character
                String escaped = "";
                if (i + pEscape.length() < pSelected.length()) {
                    String test_escape = pSelected.substring(i, i + pEscape.length());

                    if (test_escape.equals(pEscape)) {
                        escaped = test_escape;
                    }
                }

                if (!escaped.equals("")) {
                    i += escaped.length();
                    escape_open = true;
                } else {
                    i++;
                }
            } else {
                i++;
                escape_open = false;
            }
        }


        if (closePos == -1) {
            closePos = pSelected.length() - 1;
        }

        return closePos;
    }

    /**
     * Finds the first non-escaped occurrence of the close string in the
     * selection after the start position.
     *
     * @param pSelected the selection where to look for the close string
     * @param pStart the position from which to start searching
     * @param pClose the close string to look for
     * @param pEscapes the escape sequences that doesn't count as a close string
     *
     * @return the position of the close string or <code>-1</code>
     */
    private int getHardquotedClosePos(String pSelected, int pStart, String pClose, String[] pEscapes) {

        int closePos = -1;

        int i = pStart;
        while (closePos < 0 && i < pSelected.length()) {

            // Test the closing char
            String test_close = pSelected.substring(i, i + pClose.length());
            if (test_close.equals(pClose)) {
                closePos = i;
            }

            // Test for an escape sequence
            String escaped = "";

            int j = 0;
            while (j < pEscapes.length && escaped.equals("")) {
                String escape = pEscapes[j];
                if (i + escape.length() < pSelected.length()) {
                    String test_escape = pSelected.substring(i, i + escape.length());

                    if (test_escape.equals(escape)) {
                        escaped = test_escape;
                    }
                }
                j++;
            }

            if (!escaped.equals("")) {
                i += escaped.length() - 1;
            }
            i++;
        }


        if (closePos == -1) {
            closePos = pSelected.length() - 1;
        }

        return closePos;
    }

    private String getStyleName(String pStyle, Object pArgument) {

        String style = pStyle;
        if (pArgument != null) {
            style += pArgument.toString();
        }
        style += "_" + mLanguage.getId();

        return style;
    }

    private void setStyle(XTextCursor pSelection, String pStyle, int pStart, int pStop)
        throws Exception {

        pSelection.gotoRange(mSelectionStart, false);
        Utils.goRight(pSelection, pStart, false);
        Utils.goRight(pSelection, pStop - pStart, true);

        Utils.createStyle(mTextDocument, pStyle, mLanguage);
        Utils.setStyle(pSelection, pStyle);
    }
}
