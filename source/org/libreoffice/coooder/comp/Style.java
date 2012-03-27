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

import com.sun.star.awt.FontSlant;
import com.sun.star.awt.FontWeight;

public class Style {

    private String mColor = "000000";
    private boolean mBold = false;
    private boolean mItalic = false;

    public Style() {
    }

    public Style(String pColor, boolean pBold, boolean pItalic) {
        mColor = pColor;
        mBold = pBold;
        mItalic = pItalic;
    }

    public Integer getColor() {
        Integer color = new Integer(0);

        // Rearrange the color on 6 digits
        if (mColor.length() == 3) {
            char red = mColor.charAt(0);
            char green = mColor.charAt(1);
            char blue = mColor.charAt(2);

            mColor = new String(new char[]{red, red, green, green, blue, blue});
        }

        color = Integer.valueOf(mColor, 16);

        return color;
    }

    public Float getFontWeight() {
        Float weight = Float.valueOf(FontWeight.NORMAL);
        if (mBold) {
            weight = Float.valueOf(FontWeight.BOLD);
        }

        return weight;
    }

    public FontSlant getFontSlant() {
        FontSlant posture = FontSlant.NONE;
        if (mItalic) {
            posture = FontSlant.ITALIC;
        }

        return posture;
    }
}
