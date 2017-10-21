package com.cebbank.bdap.auth.kbr;

import com.cebbank.bdap.config.KbrConfig;
import com.cebbank.bdap.exception.RestException;
import org.apache.commons.codec.binary.Base64;
import org.ietf.jgss.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.hadoop.security.authentication.util.KerberosName;
import org.apache.hadoop.security.authentication.util.KerberosUtil;
import org.apache.hadoop.security.authentication.client.KerberosAuthenticator;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;

import static com.cebbank.bdap.exception.RestException.ERR_CODE_AUTHENTICATION_EXCEPTION;
import static com.cebbank.bdap.exception.RestException.ERR_CODE_BAD_HEADER;
import static com.cebbank.bdap.exception.RestException.ERR_CODE_INTERNAL_ERROR;

/**
 * Created by yilong on 2017/4/24.
 */
public class KbrAuthHandler {
    final static Logger logger = LoggerFactory.getLogger(KbrAuthHandler.class);

    private GSSManager gssManager;
    Subject serverSubject = null;
    LoginContext loginCtx = null;

    public KbrAuthHandler() {
        //
    }

    public synchronized String authenticate(final String serverName, final String authorizationHeader) throws RestException {
        logger.info("Authorization header : " + authorizationHeader);
        if (authorizationHeader == null) {
            throw new RestException(ERR_CODE_BAD_HEADER, "authorization head is null!");
        }

        try {
            //init();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RestException(ERR_CODE_AUTHENTICATION_EXCEPTION, e.getMessage());
        }

        return doAuthenticate(serverName, authorizationHeader);
    }

    private String doAuthenticate(final String serverName, final String authorizationHeader)
            throws RestException {

        if (authorizationHeader == null) {
            logger.error("authorizationHeader == null || kerbUtil.keytabPath == null || kerbUtil.kerbPrincipal == null");
            throw new RestException(ERR_CODE_BAD_HEADER, "Bad 'Authorization' header or no kerberos config.");
        }

        if (!authorizationHeader.trim().toLowerCase(Locale.ENGLISH).startsWith("negotiate ")) {
            logger.info("token : "+"Bad 'Authorization' header");
            throw new RestException(ERR_CODE_BAD_HEADER, "Bad 'Authorization' header");
        }

        //final Path keyTabPath = env.configFile().resolve(KbrConfig.getKeytabPath());

        String token = null;

        try {
            logger.info("-----------------------------------------");
            if (loginCtx == null) {
                throw new RestException(ERR_CODE_AUTHENTICATION_EXCEPTION, "loginCtx == null");
            }

            token = Subject.doAs(loginCtx.getSubject(),
                    new ServerAction(serverName, authorizationHeader));
        } catch (PrivilegedActionException ex) {
            logger.error(ex.getMessage(), ex);
            if (ex.getException() instanceof IOException) {
                throw new RestException(ERR_CODE_INTERNAL_ERROR, ex.getMessage());
            } else {
                throw new RestException(ERR_CODE_AUTHENTICATION_EXCEPTION, ex.getMessage());
            }
        }

        return token;
    }

    public class ServerAction implements PrivilegedExceptionAction<String> {
        private final String serverName;
        private final String authorization;

        public ServerAction(String serverName,
                            final String authorization) {
            this.serverName = serverName;
            this.authorization = authorization;
        }

        public String run() throws RestException {
            GSSContext gssContext = null;
            GSSCredential gssCreds = null;

            String auth = authorization.substring(KerberosAuthenticator.NEGOTIATE.length()).trim();
            final Base64 base64 = new Base64(0);
            final byte[] clientToken = base64.decode(auth);

            try {
                gssCreds = gssManager.createCredential(
                        gssManager.createName(
                                KerberosUtil.getServicePrincipal("HTTP", serverName),
                                KerberosUtil.getOidInstance("NT_GSS_KRB5_PRINCIPAL")),
                        GSSCredential.INDEFINITE_LIFETIME,
                        new Oid[]{
                                KerberosUtil.getOidInstance("GSS_SPNEGO_MECH_OID"),
                                KerberosUtil.getOidInstance("GSS_KRB5_MECH_OID")},
                        GSSCredential.ACCEPT_ONLY);
                gssContext = gssManager.createContext(gssCreds);
                byte[] serverToken = gssContext.acceptSecContext(clientToken, 0, clientToken.length);

                return new String(serverToken);

            } catch (GSSException e) {
                logger.error(e.getMessage(), e);
                throw new RestException(ERR_CODE_AUTHENTICATION_EXCEPTION, e.getMessage());
            } catch (IllegalAccessException e) {
                logger.error(e.getMessage(), e);
                throw new RestException(ERR_CODE_AUTHENTICATION_EXCEPTION, e.getMessage());
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
                throw new RestException(ERR_CODE_AUTHENTICATION_EXCEPTION, e.getMessage());
            } catch (NoSuchFieldException e) {
                logger.error(e.getMessage(), e);
                throw new RestException(ERR_CODE_AUTHENTICATION_EXCEPTION, e.getMessage());
            } catch (UnknownHostException e) {
                logger.error(e.getMessage(), e);
                throw new RestException(ERR_CODE_AUTHENTICATION_EXCEPTION, e.getMessage());
            } finally {
                if (gssContext != null) {
                    try {
                        gssContext.dispose();
                    } catch (GSSException e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                if (gssCreds != null) {
                    try {
                        gssCreds.dispose();
                    } catch (GSSException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    public synchronized void init() throws Exception {
        String jaas = KbrConfig.getJassPath();
        String keytab = KbrConfig.getKeytabPath();
        String krb5 = KbrConfig.getKrb5Path();
        System.setProperty( "sun.security.krb5.debug", "true");
        System.setProperty("java.security.auth.login.config", jaas);
        System.setProperty("java.security.krb5.conf", krb5);
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        //System.setProperty("java.security.policy", "/Users/yilong/setup/keytab/spring/client/krb5.policy");

        try {
            loginCtx = new LoginContext("HTTPLogin");
            loginCtx.login();
            //CallbackHandler handler = new DefaultKbrAuthCallback("HTTPLogin");
            //loginCtx = new LoginContext( "HTTPLogin", new Subject(), handler);
            //loginCtx.login();
            logger.info("login context login over ... ");
            serverSubject = loginCtx.getSubject();
            //logger.info("the subject is : " + serverSubject.toString());
        } catch (LoginException e) {
            logger.error(e.getMessage(), e);
            throw new RestException(ERR_CODE_AUTHENTICATION_EXCEPTION, e.getMessage());
        }

        try {
            gssManager = Subject.doAs(serverSubject,
                    new PrivilegedExceptionAction<GSSManager>() {
                        @Override
                        public GSSManager run() throws Exception {
                            return GSSManager.getInstance();
                        }
                    });
        } catch (PrivilegedActionException ex) {
            logger.error(ex.getMessage(), ex);
            throw new RestException(ERR_CODE_AUTHENTICATION_EXCEPTION, ex.getException().getMessage());
        }
    }

}
