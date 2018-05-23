/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/
package sernet.verinice.bpm.rcp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.log4j.Logger;

import com.sun.xml.messaging.saaj.packaging.mime.util.BASE64DecoderStream;
import com.sun.xml.messaging.saaj.packaging.mime.util.BASE64EncoderStream;

/**
 * Helper class to work with base64-encoded objects
 */
public final class Base64Utils {

    private static final Logger logger = Logger.getLogger(Base64Utils.class);

    /** Write the object to a Base64 string. */
    public static String encode(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return new String(BASE64EncoderStream.encode(baos.toByteArray()));
    }

    public static Object decode(String s) {
        Object o = null;
        try {
            byte[] data = BASE64DecoderStream.decode(s.getBytes());
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            o = ois.readObject();
            ois.close();
        } catch (Exception e) {
            logger.error("Error while deserializing.", e);
        }
        return o;
    }

    private Base64Utils() {

    }
}
