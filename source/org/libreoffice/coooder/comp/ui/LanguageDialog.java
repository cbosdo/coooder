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

import org.libreoffice.coooder.theLanguagesManager;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XListBox;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameContainer;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.ui.dialogs.ExecutableDialogResults;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

public class LanguageDialog extends AbstractDialog {

    public class LanguagesListListener implements XActionListener {
        private XControlContainer mControls;

        public LanguagesListListener(XControlContainer pControls) {
            mControls = pControls;
        }

        public void actionPerformed(ActionEvent pEvent) {
            try {
                XControl list = mControls.getControl(LIST_NAME);
                XListBox xList = (XListBox)UnoRuntime.queryInterface(XListBox.class, list);

                mLangId = xList.getSelectedItem();

                XControl okBtn = mControls.getControl(OK_NAME);
                XPropertySet xProps = (XPropertySet)UnoRuntime.queryInterface(
                        XPropertySet.class, okBtn.getModel());
                xProps.setPropertyValue("Enabled", Boolean.valueOf(!mLangId.equals("")));
            } catch (Exception e) {
                // Nothing to do
            }
        }

        public void disposing(EventObject pEvent) {
            mControls = null;
        }
    }

    private static final String OK_NAME = "btnOk";
    private static final String LABEL_NAME = "lblLanguages";
    private static final String LIST_NAME = "lstLanguages";
    private static final String CANCEL_NAME = "btnCancel";

    private String mLangId;

    public LanguageDialog(XComponentContext pContext) {
        super(pContext, "Language selection");
    }

    public String execute() {
        short status = doExecute();

        // Return the selected language or null
        if (status == ExecutableDialogResults.CANCEL) {
            mLangId = null;
        }

        return mLangId;
    }

    protected int[] getDialogBounds() {
        return new int[]{100, 100, 70, 155};
    }

    protected void createControls(XControl pDlgControl) throws Exception {

        // Create the language field
        createLanguageField(pDlgControl);

        // Create the OK / Cancel buttons
        createButtons(pDlgControl);
    }

    private void createLanguageField(XControl pDlg) throws Exception {
        XMultiServiceFactory xFactory = getFactory();
        XNameContainer xModelContainer = getModelContainer();

        // Create the label
        Object label = xFactory.createInstance("com.sun.star.awt.UnoControlFixedTextModel");
        XPropertySet labelProps = setBounds(label, 5, 13, 10, 40);
        labelProps.setPropertyValue("Label", "Language");
        labelProps.setPropertyValue("Name", LABEL_NAME);

        xModelContainer.insertByName(LABEL_NAME, label);

        // Create the list
        Object list = xFactory.createInstance("com.sun.star.awt.UnoControlListBoxModel");
        XPropertySet listProps = setBounds(list, 50, 10, 15, 100);
        listProps.setPropertyValue("TabIndex", Short.valueOf((short)0));
        listProps.setPropertyValue("LineCount", Short.valueOf((short)10));
        listProps.setPropertyValue("Dropdown", Boolean.TRUE);
        listProps.setPropertyValue("Name", LIST_NAME);

        // TODO Get the display names
        String[] ids = theLanguagesManager.get(getContext()).getLanguagesIdsList();
        listProps.setPropertyValue("StringItemList", ids);

        xModelContainer.insertByName(LIST_NAME, list);

        XControlContainer xControlCont = getControlContainer();
        Object listControl = xControlCont.getControl(LIST_NAME);
        XListBox xList = (XListBox)UnoRuntime.queryInterface(XListBox.class, listControl);

        xList.addActionListener(new LanguagesListListener(xControlCont));
    }

    private void createButtons(XControl pDlg) throws Exception {
        XMultiServiceFactory xFactory = getFactory();
        XNameContainer xModelContainer = getModelContainer();

        // Create the OK Button
        Object okBtn = xFactory.createInstance("com.sun.star.awt.UnoControlButtonModel");
        XPropertySet xOkProps = setBounds(okBtn, 40, 45, 15, 50);
        xOkProps.setPropertyValue("Name", OK_NAME);
        xOkProps.setPropertyValue("Enabled", Boolean.FALSE);
        xOkProps.setPropertyValue("TabIndex", Short.valueOf((short)1));
        xOkProps.setPropertyValue("Label", "OK");
        xOkProps.setPropertyValue("DefaultButton", Boolean.TRUE);
        xOkProps.setPropertyValue("PushButtonType", Short.valueOf((short)PushButtonType.OK_value));

        xModelContainer.insertByName(OK_NAME, okBtn);

        // Create the Cancel Button
        Object cancelBtn = xFactory.createInstance("com.sun.star.awt.UnoControlButtonModel");
        XPropertySet xCancelProps = setBounds(cancelBtn, 100, 45, 15, 50);
        xCancelProps.setPropertyValue("Name", CANCEL_NAME);
        xCancelProps.setPropertyValue("TabIndex", Short.valueOf((short)2));
        xCancelProps.setPropertyValue("Label", "Cancel");
        xCancelProps.setPropertyValue("PushButtonType", Short.valueOf((short)PushButtonType.CANCEL_value));

        xModelContainer.insertByName(CANCEL_NAME, xCancelProps);
    }
}
