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

import com.sun.star.awt.MessageBoxButtons;
import com.sun.star.awt.Rectangle;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XMessageBox;
import com.sun.star.awt.XMessageBoxFactory;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameContainer;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.ui.dialogs.ExecutableDialogResults;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

public abstract class AbstractDialog {

    private XComponentContext mContext;
    private XControl mDialog;

    public AbstractDialog(XComponentContext pContext, String pTitle) {
        mContext = pContext;

        initialize(pTitle);
    }

    protected XComponentContext getContext() {
        return mContext;
    }

    protected void initialize(String pTitle) {
        mDialog = null;
        try {
            // Create the dialog
            XMultiComponentFactory xMngr = mContext.getServiceManager();
            Object dlg = xMngr.createInstanceWithContext(
                    "com.sun.star.awt.UnoControlDialog", mContext);
            Object model = xMngr.createInstanceWithContext(
                    "com.sun.star.awt.UnoControlDialogModel", mContext);

            int[] bounds = getDialogBounds();
            XPropertySet dlgProps = setBounds(model, bounds[0], bounds[1], bounds[2], bounds[3]);
            dlgProps.setPropertyValue("Title", pTitle);

            XControl xDlgControl = (XControl)UnoRuntime.queryInterface(
                    XControl.class, dlg);
            XControlModel xDlgModel = (XControlModel)UnoRuntime.queryInterface(
                    XControlModel.class, model);
            xDlgControl.setModel(xDlgModel);

            mDialog = xDlgControl;

            // Create the dialog controls
            createControls(xDlgControl);
        } catch (Exception e) {
            mDialog = null;
        }
    }

    protected abstract int[] getDialogBounds();

    protected abstract void createControls(XControl pDlgControl) throws Exception;

    protected short doExecute() {
        short status = ExecutableDialogResults.CANCEL;

        if (mDialog != null) {
            try {
                XMultiComponentFactory xMngr = mContext.getServiceManager();

                // Execute the dialog
                Object toolkit = xMngr.createInstanceWithContext(
                        "com.sun.star.awt.Toolkit", mContext);
                XToolkit xToolkit = (XToolkit)UnoRuntime.queryInterface(XToolkit.class, toolkit);

                XControl xControl = (XControl)UnoRuntime.queryInterface(XControl.class, mDialog);
                xControl.createPeer(xToolkit, null);

                XDialog xDlg = (XDialog)UnoRuntime.queryInterface(XDialog.class, mDialog);
                status = xDlg.execute();
            } catch (Exception e) {
                status = ExecutableDialogResults.CANCEL;
            }
        }

        return status;
    }

    protected void doEndExecute() {
        XDialog xDlg = (XDialog)UnoRuntime.queryInterface(XDialog.class, mDialog);
        xDlg.endExecute();
    }

    protected XPropertySet setBounds(Object pModel, int pX, int pY, int pH, int pW) throws Exception {

        XPropertySet xProps = (XPropertySet)UnoRuntime.queryInterface(
                XPropertySet.class, pModel);
        xProps.setPropertyValue("PositionX", Integer.valueOf(pX));
        xProps.setPropertyValue("PositionY", Integer.valueOf(pY));
        xProps.setPropertyValue("Height", Integer.valueOf(pH));
        xProps.setPropertyValue("Width", Integer.valueOf(pW));

        return xProps;
    }

    protected XMultiServiceFactory getFactory() {
        return (XMultiServiceFactory)UnoRuntime.queryInterface(
                XMultiServiceFactory.class, mDialog.getModel());
    }

    protected XNameContainer getModelContainer() {
        return (XNameContainer)UnoRuntime.queryInterface(
                XNameContainer.class, mDialog.getModel());
    }

    protected XControlContainer getControlContainer() {
        return (XControlContainer)UnoRuntime.queryInterface(
                XControlContainer.class, mDialog);
    }

    public static void showErrorDialog(XComponentContext pContext, String pTitle, String pMsg) {
        try {
            XMultiComponentFactory xMngr = pContext.getServiceManager();

            // Execute the dialog
            Object toolkit = xMngr.createInstanceWithContext("com.sun.star.awt.Toolkit", pContext);
            XMessageBoxFactory xFactory = (XMessageBoxFactory)UnoRuntime.queryInterface(
                    XMessageBoxFactory.class, toolkit);

            // Get the peer parent window
            Object oDesktop = xMngr.createInstanceWithContext("com.sun.star.frame.Desktop", pContext);
            XDesktop xDesktop = (XDesktop)UnoRuntime.queryInterface(XDesktop.class, oDesktop);
            XWindow xWin = xDesktop.getCurrentFrame().getContainerWindow();
            XWindowPeer xPeer = (XWindowPeer)UnoRuntime.queryInterface(XWindowPeer.class, xWin);

            XMessageBox msgBox = xFactory.createMessageBox(xPeer, new Rectangle(), "errorbox",
                    MessageBoxButtons.BUTTONS_OK, pTitle, pMsg);

            msgBox.execute();

        } catch (Exception e) {
            // Nothing to do
        }
    }
}
