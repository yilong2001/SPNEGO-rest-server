package com.spengo.bdap.auth.kbr;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.security.Principal;
import java.security.PrivilegedAction;

/**
 * Created by yilong on 2017/4/26.
 */
public class KbrUtil {
    final static Logger logger = LoggerFactory.getLogger(KbrAuthHandler.class);

    public LoginContext kerbersInit(String loginConfPath, String kr5Path, String loginKey) throws LoginException {
        try {
            System.setProperty("hadoop.security.authentication", "Kerberos");

            System.setProperty("java.security.auth.login.config", loginConfPath);
            System.setProperty("java.security.krb5.conf", kr5Path);
            System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");

            LoginContext  loginCOntext = new LoginContext(loginKey);
            loginCOntext.login();
            return loginCOntext;
        } catch (LoginException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private HttpClient getHttpClient() {
        Credentials use_jaas_creds = new Credentials() {
            public String getPassword() {
                return null;
            }
            public Principal getUserPrincipal() {
                return null;
            }
        };

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(null, -1, null), use_jaas_creds);
        Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
                .register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true)).build();
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultAuthSchemeRegistry(authSchemeRegistry).setDefaultCredentialsProvider(credsProvider).build();
        return httpclient;
    }
}
