/*
 * Copyright (c) 2016, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

//
// Please run in othervm mode.  SunJSSE does not support dynamic system
// properties, no way to re-use system properties in samevm/agentvm mode.
//

/*
 * @test
 * @bug 8148421 8193683
 * @summary Transport Layer Security (TLS) Session Hash and Extended
 *     Master Secret Extension
 * @summary Increase the number of clones in the CloneableDigest
 * @library /javax/net/ssl/templates
 * @compile DigestBase.java
 * @run main/othervm HandshakeHashCloneExhaustion
 *     TLSv1.2 TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
 * @run main/othervm HandshakeHashCloneExhaustion
 *     TLSv1.1 TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import javax.net.ssl.SSLSocket;

public class HandshakeHashCloneExhaustion extends SSLSocketTemplate {

    private static String[] protocol;
    private static String[] ciphersuite;

    /*
     * ==================
     * Run the test case.
     */
    public static void main(String[] args) throws Exception {
        // Add in a non-cloneable MD5/SHA1/SHA-256 implementation
        Security.insertProviderAt(new MyProvider(), 1);

        if (args.length != 2) {
            throw new Exception(
                    "Usage: HandshakeHashCloneExhaustion protocol ciphersuite");
        }

        System.out.println("Testing:  " + args[0] + " " + args[1]);
        protocol = new String [] { args[0] };
        ciphersuite = new String[] { args[1] };

        (new HandshakeHashCloneExhaustion()).run();
    }

    @Override
    protected void runServerApplication(SSLSocket socket) throws Exception {
        socket.setNeedClientAuth(true);
        socket.setEnabledProtocols(protocol);
        socket.setEnabledCipherSuites(ciphersuite);

        // here comes the test logic
        InputStream sslIS = socket.getInputStream();
        OutputStream sslOS = socket.getOutputStream();

        sslIS.read();
        sslOS.write(85);
        sslOS.flush();
    }

    @Override
    protected void runClientApplication(SSLSocket socket) throws Exception {
        InputStream sslIS = socket.getInputStream();
        OutputStream sslOS = socket.getOutputStream();

        sslOS.write(280);
        sslOS.flush();
        sslIS.read();
    }
}