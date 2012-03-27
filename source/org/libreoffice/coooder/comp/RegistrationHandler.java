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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;

/**
 * Component main registration class.
 *
 * <p><strong>This class should not be modified.</strong></p>
 *
 * @author Cedric Bosdonnat aka. cedricbosdo
 *
 */
public class RegistrationHandler {

    /**
     * Get a component factory for the implementations handled by this class.
     *
     * <p>This method calls all the methods of the same name from the classes listed
     * in the <code>RegistrationHandler.classes</code> file. <strong>This method
     * should not be modified.</strong></p>
     *
     * @param pImplementationName the name of the implementation to create.
     *
     * @return the factory which can create the implementation.
     */
    public static XSingleComponentFactory __getComponentFactory(String sImplementationName ) {
        XSingleComponentFactory xFactory = null;

        Class[] classes = findServicesImplementationClasses();

        int i = 0;
        while (i < classes.length && xFactory == null) {
            Class clazz = classes[i];
            if ( sImplementationName.equals( clazz.getCanonicalName() ) ) {
                try {
                    Class[] getTypes = new Class[]{String.class};
                    Method getFactoryMethod = clazz.getMethod("__getComponentFactory", getTypes);
                    Object o = getFactoryMethod.invoke(null, new String[]{sImplementationName});
                    xFactory = (XSingleComponentFactory)o;
                } catch (Exception e) {
                    // Nothing to do: skip
                    System.err.println("Error happened");
                    e.printStackTrace();
                }
            }
            i++;
        }
        return xFactory;
    }

    /**
     * Writes the services implementation informations to the UNO registry.
     *
     * <p>This method calls all the methods of the same name from the classes listed
     * in the <code>RegistrationHandler.classes</code> file. <strong>This method
     * should not be modified.</strong></p>
     *
     * @param pRegistryKey the root registry key where to write the informations.
     *
     * @return <code>true</code> if the informations have been successfully written
     *      to the registry key, <code>false</code> otherwise.
     */
    public static boolean __writeRegistryServiceInfo(XRegistryKey xRegistryKey ) {

        Class[] classes = findServicesImplementationClasses();

        boolean success = true;
        int i = 0;
        while (i < classes.length && success) {
            Class clazz = classes[i];
            try {
                Class[] writeTypes = new Class[]{XRegistryKey.class};
                Method getFactoryMethod = clazz.getMethod("__writeRegistryServiceInfo", writeTypes);
                Object o = getFactoryMethod.invoke(null, new Object[]{xRegistryKey});
                success = success && ((Boolean)o).booleanValue();
            } catch (Exception e) {
                success = false;
                e.printStackTrace();
            }
            i++;
        }
        return success;
    }

    /**
     * @return all the UNO implementation classes.
     */
    private static Class[] findServicesImplementationClasses() {

        ArrayList classes = new ArrayList();

        InputStream in = RegistrationHandler.class.getResourceAsStream("RegistrationHandler.classes");
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));

        try {
            String line = reader.readLine();
            while (line != null) {
                if (!line.equals("")) {
                    line = line.trim();
                    try {
                        Class clazz = Class.forName(line);

                        Class[] writeTypes = new Class[]{XRegistryKey.class};
                        Class[] getTypes = new Class[]{String.class};

                        Method writeRegMethod = clazz.getMethod("__writeRegistryServiceInfo", writeTypes);
                        Method getFactoryMethod = clazz.getMethod("__getComponentFactory", getTypes);

                        if (writeRegMethod != null && getFactoryMethod != null) {
                            classes.add(clazz);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
                in.close();
            } catch (Exception e) {};
        }

        Class[] aClasses = new Class[classes.size()];
        for (int i = 0; i < aClasses.length; i++) {
            aClasses[i] = (Class)classes.get(i);
        }
        return aClasses;
    }
}
