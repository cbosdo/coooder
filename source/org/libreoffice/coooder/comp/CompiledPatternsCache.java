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

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Manages the compiled regexp patterns in order to save execution time.
 *
 * <em>This improvement saves only 3% of time on the whole process.</em>
 *
 * @author cbosdonnat
 *
 */
public class CompiledPatternsCache {

    private HashMap mCache = new HashMap();

    /**
     * Get the compiled pattern corresponding to the given regexp and flags.
     *
     * <p>Get the {@link Pattern} from the cache if it has already been used
     * or compile the pattern and store it.</p>
     *
     * @param pRegexp the regexp to get
     * @param pFlags the flags for the regexp
     *
     * @return the compiled {@link Pattern} object
     */
    public Pattern getPattern(String pRegexp, int pFlags) {
        String key = pRegexp + "-" + pFlags;
        Pattern compiled = (Pattern)mCache.get(key);

        if (compiled == null) {
            compiled = Pattern.compile(pRegexp, pFlags);
            mCache.put(key, compiled);
        }

        return compiled;
    }

    /**
     * Cleans the cache.
     */
    public void cleanCache() {
        mCache.clear();
    }
}
