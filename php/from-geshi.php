#!/usr/bin/php
<?php
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

function getColor($color) {
    $COLORS = array(
        "black" => "#000000",
        "navy" => "#000080",
        "brown" => "#a52a2a",
        "green" => "#008000",
        "blue" => "#0000ff"
    );

    $hexcode = $COLORS[$color];
    if ($hexcode != null) {
        $color = $hexcode;
    }
    $color = substr($color, 1, strlen($color) - 1);
    return $color;
}

function computeStyle($cssStyle) {
    $result = array();
    $parts = split(";", $cssStyle);
    foreach ($parts as $part) {
        $style = split(":", trim($part));
        $name = trim($style[0]);
        $value = trim($style[1]);

        if ($name == "font-weight" && $value == "bold") {
            $name = "bold";
            $value = true;
        }

        
        if ($name == "font-style" && $value == "italic") {
            $name = "italic";
            $value = true;
        }

        $result[$name] = $value;
    }

    return $result;
}

function getLF($optimize) {
    $result = "";
    if (!$optimize) {
        $result = "\n";
    }
    return $result;
}

$geshiFile = $argv[1];

$outputFolder = ".";
if ($argc == 3) {
    $outputFolder = $argv[2];
}

// TODO make it configurable
/*
 * Allows to lower the parsing time. 
 * For example parsing the Java definition without optimization
 * takes about 1500 ms vs 900 ms with optimization (on my machine).
 * The gain is then about 40% of time.
 */
$optimize = true;

if (is_file($geshiFile)) {

    define(GESHI_COMMENTS, 'geshi_comments');

    // Compute the language id from the geshi filename
    $fileInfos = pathinfo($geshiFile);
    $langId = $fileInfos["filename"];

    // Include the file and compute the XML output
    include $geshiFile;

    $xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    // Compute the XML file content
    $name = $language_data['LANG_NAME'];
    $objectOriented = $language_data['OOLANG'];
    $escapeChar = $language_data['ESCAPE_CHAR'];

    $styles = array();
    if ($objectOriented == null || !$objectOriented) {
        $objectOriented = "false";
    } else {
        $objectOriented = "true";
    }

    $xml .= "<l:language xmlns:l=\"http://cedric.bosdonnat.free.fr/coooder/language/\" " . getLF($optimize) .
            "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " . getLF($optimize) .
            "   xsi:schemaLocation=\"http://cedric.bosdonnat.free.fr/coooder/language/ language.xsd \" " . getLF($optimize) .
            "	id=\"$langId\" name=\"$name\" objectOriented=\"$objectOriented\" escapeChar=\"$escapeChar\">" . getLF($optimize);

    // Compute the object splitters element
    if ($language_data['OOLANG']) {
        $splitters = "<l:objectSplitters>" . getLF($optimize);
        foreach ($language_data['OBJECT_SPLITTERS'] as $key => $value) {
            $splitterId = "splitter_$key";
            $splitters .= "<l:splitter id=\"" . $splitterId . "\" value=\"" . htmlentities($value) . "\"/>" . getLF($optimize);

            $style = $language_data['STYLES']['METHODS'][$key];
            $styles[$splitterId] = computeStyle($style);
        }
        $splitters .= "</l:objectSplitters>" . getLF($optimize);
        $xml .= $splitters;
    }

    // Compute the comments element
    $commentCaseSensitive = $language_data['CASE_SENSITIVE'][GESHI_COMMENTS];
    if ($commentCaseSensitive == null || !$commentCaseSensitive) {
        $commentCaseSensitive = "false";
    } else {
        $commentCaseSensitive = "true";
    }

    $comments = "<l:comments caseSensitive=\"$commentCaseSensitive\">" . getLF($optimize);
    foreach ($language_data['COMMENT_SINGLE'] as $key => $value) {
        $commentId = "comments_$key";

        // Define the comment xml fragment
        $comment = "<l:single id=\"$commentId\">" . getLF($optimize);
        $comment .= "<l:opening>" . htmlentities($value) . "</l:opening>" . getLF($optimize);
        $comment .= "</l:single>" . getLF($optimize);

        $comments .= $comment;

        // Define the associated style
        $style = $language_data['STYLES']['COMMENTS'][$key];
        $styles[$commentId] = computeStyle($style);
    }

    foreach ($language_data['COMMENT_MULTI'] as $open => $close) {
        $comment = "<l:multiple>" . getLF($optimize);
        $comment .= "<l:opening>" . htmlentities($open) . "</l:opening>" . getLF($optimize);
        $comment .= "<l:closing>" . htmlentities($close) . "</l:closing>" . getLF($optimize);
        $comment .= "</l:multiple>" . getLF($optimize);

        $comments .= $comment;
    } 

    $comments .= "</l:comments>" . getLF($optimize);
    $xml .= $comments;
    
    // Compute the symbols element
    $symbols = "<l:symbols>" . getLF($optimize);
    if ($language_data['SYMBOLS'] != null) {
        $setopened = false;
        foreach ($language_data['SYMBOLS'] as $key => $symbol) {
            if (is_array($symbol)) {
                $setId = "symbols_" + $key;

                $symbols .= "<l:set id=\"" . $setId . "\">" . getLF($optimize);
                foreach ($symbol as $value) {
                    $symbols .= "<l:value>" . htmlentities($value) . "</l:value>" . getLF($optimize);
                    $style = $language_data['STYLES']['SYMBOLS'][$key];
                    $styles[$setId] = computeStyle($style);
                }
                $symbols .= "</l:set>" . getLF($optimize);
            } else {
                if (!$setopened) {
                    $symbols .= "<l:set id=\"symbols_0\">" . getLF($optimize);
                    $setopened = true;
                }
                $symbols .= "<l:value>" . htmlentities($symbol) . "</l:value>" . getLF($optimize);
                if ($key == (count($language_data['SYMBOLS']) - 1)) {
                    $symbols .= "</l:set>" . getLF($optimize);
                    $style = $language_data['STYLES']['SYMBOLS'][0];
                    $styles["symbols_0"] = computeStyle($style);
                }
            }
        }
    }
    $symbols .= "</l:symbols>" . getLF($optimize);
    $xml .= $symbols;

    // Compute the quotemarks element
    $quotemarks = "<l:quotemarks>" . getLF($optimize);
    foreach ($language_data['QUOTEMARKS'] as $quotemark) {
        $quotemarks .= "<l:value>" . htmlentities($quotemark) . "</l:value>" . getLF($optimize);
    }
    $quotemarks .= "</l:quotemarks>" . getLF($optimize);
    $xml .= $quotemarks;

    // Compute the hardquotes element
    if ($language_data['HARDQUOTE'] != null) {
        $hardquotes = "<l:hardquotes>" . getLF($optimize);
        foreach ($language_data['HARDQUOTE'] as $hardquote) {
            $hardquotes .= "<l:value>" . htmlentities($hardquote) . "</l:value>" . getLF($optimize);
        }
        $hardquotes .= "</l:hardquotes>" . getLF($optimize);
        $xml .= $hardquotes;
    }

    // Compute the hardquoteEscapes element
    if ($language_data['HARDESCAPE'] != null) {
        $hardquoteEscapes = "<l:hardquoteEscapes>" . getLF($optimize);
        foreach ($language_data['HARDESCAPE'] as $hardquoteEscape) {
            $hardquoteEscapes .= "<l:value>" . htmlentities($hardquoteEscape) . "</l:value>" . getLF($optimize);
        }
        $hardquoteEscapes .= "</l:hardquoteEscapes>" . getLF($optimize);
        $xml .= $hardquoteEscapes;
    }

    // Compute the keywords element
    $keywords = "<l:keywords>" . getLF($optimize);
    foreach ($language_data['KEYWORDS'] as $key => $keywordsSet) {
        $setId = "keywords_$key";

        // True is the default value for case sensitivity: don't set it
        $caseSensitive = $language_data['CASE_SENSITIVE'][$setId];
        if ( $caseSensitive == null || !$commentSensitive ) {
            $caseSensitive = " caseSensitive=\"false\"";
        }


        $keywords .= "<l:set id=\"$setId\"$caseSensitive>" . getLF($optimize);
        foreach ($keywordsSet as $keyword) {
            $keywords .= "<l:value>" . htmlentities($keyword) . "</l:value>" . getLF($optimize);
        }
        $keywords .= "</l:set>" . getLF($optimize);

        // Define the keywords set style
        $style = $language_data['STYLES']['KEYWORDS'][$key];
        $styles[$setId] = computeStyle($style);
    }
    $keywords .= "</l:keywords>" . getLF($optimize);
    $xml .= $keywords;

    // Compute the regexps element
    $regexps = "<l:regexps>" . getLF($optimize);
    foreach ($language_data['REGEXPS'] as $key => $value) {
        $regexpId = "regexp_$key";
        $regexp = "";
        if (is_string($value)) {
            $regexp = $value;
        } else if (is_array($value)) {
            $regexp = $value[GESHI_SEARCH];
        }
        
        $regexp = str_replace("\n", "\\n", htmlentities($regexp));

        $regexps .= "<l:regexp id=\"$regexpId\" value=\"" . $regexp . "\"/>" . getLF($optimize);
        
        // Define the regexp style
        $style = $language_data['STYLES']['REGEXPS'][$key];
        $styles[$regexpId] = computeStyle($style);
    }
    $regexps .= "</l:regexps>" . getLF($optimize);
    $xml .= $regexps;

    // Get the multi comment style
    $style = $language_data['STYLES']['COMMENTS']['MULTI'];
    if ($style != null) {
        $styles['comment_multi'] = computeStyle($style);
    }

    // Get the string style
    $style = $language_data['STYLES']['STRINGS'][0];
    if ($style != null) {
        $styles['string'] = computeStyle($style);
    }
    
    // Get the number style
    $style = $language_data['STYLES']['NUMBERS'][0];
    if ($style != null) {
        $styles['number'] = computeStyle($style);
    }
    
    // Get the escaped style
    $style = $language_data['STYLES']['ESCAPE_CHAR'][0];
    if ($style != null) {
        $styles['escaped'] = computeStyle($style);
    }

    // Compute the styles element
    $xml .= "<l:styles>" . getLF($optimize);

    foreach ($styles as $id => $style) {
        $attributes = "";

        $color = $style['color'];
        if (!is_null($color)) {
            $color = getColor($color);
            $attributes .= " color=\"$color\"";
        }

        $bold = $style['bold'];
        if (!is_null($bold)) {
            $attributes .= " bold=\"true\"";
        }

        $italic = $style['italic'];
        if (!is_null($italic)) {
            $attributes .= " italic=\"true\"";
        }

        $xml .= "<l:style element=\"$id\"$attributes/>" . getLF($optimize);
    }

    $xml .= "</l:styles>" . getLF($optimize);

    $xml .= "</l:language>" . getLF($optimize);

    // Write the XML file
    file_put_contents("$outputFolder/$langId.xml", $xml);
}
?>
