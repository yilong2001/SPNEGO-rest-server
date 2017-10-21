package com.spengo.rest.client;

import com.spengo.rest.auth.kbr.KbrUtil;
import org.apache.commons.codec.binary.Base64;
import org.ietf.jgss.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PrivilegedExceptionAction;


/**
 * Created by yilong on 2017/10/20.
 */
public class Client {
    final static Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws Exception {
        final KbrUtil kbrUtil = new KbrUtil();
        final String httpUrl = "http://ip:port/testtable/regions";

        LoginContext context = kbrUtil.kerbersInit("clientjaas.conf",
                "krb5.conf",
                "KrbLogin");

        byte[] token = Subject.doAs(context.getSubject(), new ClientAction());
        final Base64 base64 = new Base64(0);
        final String base64Str = "Negotiate " + base64.encodeToString(token);

        URL url = new URL(httpUrl);
        HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
        urlConn.setRequestProperty("Authorization", "Negotiate " + base64Str);

        InputStream inputStream = null;
        try {
            System.setProperty( "sun.security.krb5.debug", "true");
            inputStream = Subject.doAs(context.getSubject(), new PrivilegedGetInputStream(urlConn));

            byte[] inbytes = new byte[]{};
            inputStream.read(inbytes);
            System.out.print(new String(inbytes));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    static class ClientAction implements PrivilegedExceptionAction<byte[]>  {
        public byte[] run() throws GSSException {
            try {
                Oid KRB5_MECH_OID = new Oid("1.2.840.113554.1.2.2");
                Oid SPNEGO_MECH_OID = new Oid("1.3.6.1.5.5.2");

                GSSManager manager = GSSManager.getInstance();
                GSSName clientName =
                        manager.createName("clientPrincipal", GSSName.NT_USER_NAME);

                GSSCredential clientCreds =
                        manager.createCredential(clientName,
                                8 * 3600,
                                SPNEGO_MECH_OID,
                                GSSCredential.INITIATE_ONLY);

                GSSName peerName =
                        manager.createName("HTTP@realm",
                                GSSName.NT_HOSTBASED_SERVICE);

                GSSContext secContext =
                        manager.createContext(peerName,
                                SPNEGO_MECH_OID,
                                clientCreds,
                                GSSContext.DEFAULT_LIFETIME);
                secContext.requestMutualAuth(true);
                secContext.requestConf(false);
                secContext.requestInteg(true);

                byte[] inToken = new byte[0];
                return secContext.initSecContext(inToken, 0, inToken.length);
            } catch (GSSException e) {
                logger.error(e.getMessage(), e);
                throw e;
            }
        }
    }

    static class PrivilegedGetInputStream implements PrivilegedExceptionAction<InputStream> {
        final private HttpURLConnection urlConn;
        public PrivilegedGetInputStream(final HttpURLConnection urlConn) {
            this.urlConn = urlConn;
        }

        @Override
        public InputStream run() throws Exception {
            urlConn.connect();
            return urlConn.getInputStream();
        }
    }
}
