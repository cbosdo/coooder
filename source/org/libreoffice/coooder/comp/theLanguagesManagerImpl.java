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

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;

import org.libreoffice.coooder.HighlightingException;
import org.libreoffice.coooder.XLanguage;
import org.libreoffice.coooder.XLanguagesManager;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XHierarchicalPropertySet;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.registry.InvalidRegistryException;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uri.XExternalUriReferenceTranslator;
import com.sun.star.util.XMacroExpander;


public final class theLanguagesManagerImpl extends WeakBase
    implements XServiceInfo, XLanguagesManager {

    private static final String IMPLEMENTATION_NAME = theLanguagesManagerImpl.class.getName();
    private static final String[] SERVICE_NAMES = { "org.libreoffice.coooder.theLanguagesManager" };

    private XComponentContext mContext;
    private HashMap mLoadedLanguages = new HashMap();


    public theLanguagesManagerImpl( XComponentContext pContext ) {
        mContext = pContext;
    };

    public static XSingleComponentFactory __getComponentFactory( String pImplementationName ) {
        XSingleComponentFactory xFactory = null;

        if (pImplementationName.equals(IMPLEMENTATION_NAME)) {
            xFactory = Factory.createComponentFactory(theLanguagesManagerImpl.class, SERVICE_NAMES);
        }
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo(XRegistryKey pRegistryKey) {
        return Factory.writeRegistryServiceInfo(IMPLEMENTATION_NAME,
            SERVICE_NAMES, pRegistryKey) && writeSingletonKey(pRegistryKey);
    }

    public static boolean writeSingletonKey(XRegistryKey pRegistryKey) {
        boolean success = false;

        try {
            XRegistryKey xNewKey = pRegistryKey
            .createKey("/"
                + theLanguagesManagerImpl.class.getName()
                + "/UNO/SINGLETONS/org.libreoffice.coooder.theLanguagesManager");
            xNewKey.setStringValue(theLanguagesManagerImpl.class.getName());
            success = true;
        } catch (InvalidRegistryException e) {
            System.err.println("Error creating singleton key: " + e.toString());
        }

        return success;
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


    //-------------------------------- org.libreoffice.coooder.XLanguagesManager


    public XLanguage getLanguage(String pId) throws HighlightingException {
        LanguageImpl language = null;
        try {
            XMultiComponentFactory xServiceMngr = mContext.getServiceManager();

            // Check the loaded languages cache first
            Object fromCache = mLoadedLanguages.get(pId);
            if (fromCache != null) {

                // The language is already in the cache: no need to reload it!
                language = (LanguageImpl)fromCache;

            } else {

                language = new LanguageImpl(mContext);

                File xml = new File(getLangDirectory(), pId + ".xml");

                Object translator = xServiceMngr.createInstanceWithContext(
                        "com.sun.star.uri.ExternalUriReferenceTranslator", mContext);
                XExternalUriReferenceTranslator xTranslator = (XExternalUriReferenceTranslator)UnoRuntime.
                queryInterface(XExternalUriReferenceTranslator.class, translator);

                String path = xTranslator.translateToInternal(xml.toURI().toString());
                language.defineFromXml(path);

                // Add to the language cache
                mLoadedLanguages.put(pId, language);
            }
        } catch (Exception e) {
            throw new HighlightingException("Can't get the language " + pId, e);
        }

        return language;
    }

    public String[] getLanguagesIdsList() throws HighlightingException {
        String[] ids = null;

        try {
            File dir = getLangDirectory();
            String[] files = dir.list(new FilenameFilter() {

                public boolean accept(File pDir, String pName) {
                    return pName.endsWith(".xml");
                }

            });

            ids = new String[files.length];
            for (int i = 0; i < ids.length; i++) {
                String filename = files[i];
                int posExt = filename.indexOf(".");
                String id = filename.substring(0, posExt);

                ids[i] = id;
            }

            Arrays.sort(ids);

        } catch (Exception e) {
            throw new HighlightingException("Can't get the list of language Ids", e);
        }
        return ids;
    }

    public String getLanguageFilesPath() throws HighlightingException {
        String path = null;

        try {
            XMultiComponentFactory xServiceMngr = mContext.getServiceManager();
            Object configProvider = xServiceMngr.createInstanceWithContext(
                    "com.sun.star.configuration.ConfigurationProvider", mContext);
            XMultiServiceFactory xConfigProvider = (XMultiServiceFactory)UnoRuntime.queryInterface(
                    XMultiServiceFactory.class, configProvider);

            PropertyValue[] params = new PropertyValue[1];
            params[0] = new PropertyValue();
            params[0].Name = new String("nodepath");
            params[0].Value = "/org.libreoffice.Coooder/LanguageFiles";

            Object access = xConfigProvider.createInstanceWithArguments(
                    "com.sun.star.configuration.ConfigurationAccess", params);
            XHierarchicalPropertySet xProps = (XHierarchicalPropertySet)UnoRuntime.queryInterface(
                    XHierarchicalPropertySet.class, access);

            Object value = xProps.getHierarchicalPropertyValue("Path");
            path = (String)value;
        } catch (Exception e) {
            throw new HighlightingException("Can't get languages files path", e);
        }

        return path;
    }

    private File getLangDirectory() throws HighlightingException {
        File dir = null;
        try {
            String path = getLanguageFilesPath();

            int pos = path.lastIndexOf(":");
            path = path.substring(pos + 1);

            Object expander = mContext.getValueByName("/singletons/com.sun.star.util.theMacroExpander");
            XMacroExpander xExpander = (XMacroExpander)UnoRuntime.queryInterface(
                    XMacroExpander.class, expander);

            path = xExpander.expandMacros(path);

            XMultiComponentFactory xServiceMngr = mContext.getServiceManager();
            Object translator = xServiceMngr.createInstanceWithContext(
                    "com.sun.star.uri.ExternalUriReferenceTranslator", mContext);
            XExternalUriReferenceTranslator xTranslator = (XExternalUriReferenceTranslator)UnoRuntime.
                    queryInterface(XExternalUriReferenceTranslator.class, translator);

            path = xTranslator.translateToExternal(path);

            dir = new File(new URI(path));

        } catch (Exception e) {
            throw new HighlightingException("Can't get the list of language Ids", e);
        }
        return dir;
    }
}
