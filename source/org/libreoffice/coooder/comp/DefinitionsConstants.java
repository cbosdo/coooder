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

/**
 * Constants describing the languages.xsd content.
 *
 * @author cbosdonnat
 *
 */
public interface DefinitionsConstants {

    Object NODE_LANGUAGE = "language";
    String ATTR_ID = "id";
    String ATTR_OO = "objectOriented";
    String ATTR_NAME = "name";
    String ATTR_ESCAPE_CHAR = "escapeChar";

    String NODE_OO_SPLITTERS = "objectSplitters";
    Object NODE_OO_SPLITTER = "splitter";

    Object NODE_VALUE = "value";
    String NODE_SYMBOLS = "symbols";
    String NODE_QUOTEMARKS = "quotemarks";
    String NODE_HARDQUOTES = "hardquotes";
    String NODE_HARDESCAPES = "hardquoteEscapes";

    String NODE_COMMENTS = "comments";
    String ATTR_CASE_SENSITIVE = "caseSensitive";
    Object NODE_SINGLE = "single";
    Object NODE_MULTIPLE = "multiple";
    String NODE_OPENING = "opening";
    String NODE_CLOSING = "closing";

    String NODE_KEYWORDS = "keywords";

    Object NODE_SET = "set";

    String NODE_REGEXPS = "regexps";
    String NODE_REGEXP = "regexp";
    String ATTR_VALUE = "value";

    String NODE_STYLES = "styles";
    String NODE_STYLE = "style";
    String ATTR_ELEMENT = "element";
    String ATTR_BOLD = "bold";
    String ATTR_ITALIC = "italic";
    String ATTR_COLOR = "color";


    String STYLE_COMMENT_MULTI = "comment_multi";
    String STYLE_STRING = "string";
    String STYLE_ESCAPED = "escaped";
    String STYLE_SYMBOL = "symbol";
    String STYLE_NUMBER = "number";
    String STYLE_COMMENT = "comment";
    String STYLE_KEYWORD = "keyword";
    String STYLE_REGEXP = "regexp";
    String STYLE_MEMBER = "member";
}
