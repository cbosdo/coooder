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
package org.libreoffice.coooder.comp.ui;

import java.util.Calendar;

import org.libreoffice.coooder.XHighlighter;
import org.libreoffice.coooder.XLanguage;
import org.libreoffice.coooder.theLanguagesManager;

import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.task.XJobExecutor;
import com.sun.star.task.XStatusIndicator;
import com.sun.star.task.XStatusIndicatorFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;


public final class ParseTriggerImpl extends WeakBase implements XServiceInfo, XJobExecutor {

    private static final String IMPLEMENTATION_NAME = ParseTriggerImpl.class.getName();
    private static final String[] SERVICE_NAMES = { "org.libreoffice.coooder.ui.ParseTrigger" };

    private final XComponentContext mContext;


    public ParseTriggerImpl(XComponentContext pContext) {
        mContext = pContext;
    };

    public static XSingleComponentFactory __getComponentFactory(String pImplementationName) {
        XSingleComponentFactory xFactory = null;

        if (pImplementationName.equals(IMPLEMENTATION_NAME)) {
            xFactory = Factory.createComponentFactory(ParseTriggerImpl.class, SERVICE_NAMES);
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

    public boolean supportsService(String pService) {
        int len = SERVICE_NAMES.length;

        for( int i=0; i < len; i++) {
            if (pService.equals(SERVICE_NAMES[i]))
                return true;
        }
        return false;
    }

    public String[] getSupportedServiceNames() {
        return SERVICE_NAMES;
    }


    //------------------------------------------ com.sun.star.task.XJobExecutor


    public void trigger(String pEvent)
    {
        XStatusIndicator monitor = null;
        try {
            // Check if the selection is a text selection
            XMultiComponentFactory xMngr = mContext.getServiceManager();
            Object desktop = xMngr.createInstanceWithContext("com.sun.star.frame.Desktop", mContext);
            XDesktop xDesktop = (XDesktop)UnoRuntime.queryInterface(XDesktop.class, desktop);
            XComponent xComponent = xDesktop.getCurrentComponent();
            XModel xDocModel = (XModel)UnoRuntime.queryInterface(XModel.class, xComponent);

            XServiceInfo xInfos = (XServiceInfo)UnoRuntime.queryInterface(
                    XServiceInfo.class, xDocModel.getCurrentSelection());
            if (!xInfos.supportsService("com.sun.star.text.TextRanges")) {
                throw new Exception("Can only highlight code snippets");
            }

            // Ask for the language
            String langId = askLanguage();

            if (langId != null) {

                XLanguage xLanguage = theLanguagesManager.get(mContext).getLanguage(langId);

                // Parse
                Object highlighter = xMngr.createInstanceWithContext(
                        "org.libreoffice.coooder.Highlighter", mContext);
                XHighlighter xHighlighter = (XHighlighter)UnoRuntime.queryInterface(
                        XHighlighter.class, highlighter);

                xHighlighter.setLanguage(xLanguage);

                // Create and set the status indicator
                XFrame xFrm = xDocModel.getCurrentController().getFrame();
                XStatusIndicatorFactory xStatusFactory = (XStatusIndicatorFactory)UnoRuntime.queryInterface(
                        XStatusIndicatorFactory.class, xFrm );
                monitor = xStatusFactory.createStatusIndicator();
                xHighlighter.setStatusIndicator( monitor );
                monitor.start( "Hightlighting...", 100 );


                // Parse the code snippet
                long debut = Calendar.getInstance().getTimeInMillis();
                xHighlighter.parse();
                long fin = Calendar.getInstance().getTimeInMillis();
                long duree = fin - debut;

                System.out.println("Highlighting duration: (in ms)" + duree);

                monitor.end();
            }
        } catch (Exception e) {
            monitor.end();

            String title = "Error during syntax highlighting";
            AbstractDialog.showErrorDialog(mContext, title, e.getMessage());
        }
    }

    private String askLanguage() {
        LanguageDialog dlg = new LanguageDialog(mContext);
        return dlg.execute();
    }
}
