package com.cebbank.bdap.config;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Created by yilong on 2017/10/18.
 */
@Component
@RefreshScope
public class KbrConfig {
    private static String jassPath = "/home/mrest/jaas.conf";
    private static String authType = "kerberos";
    private static String keytabPath = "/home/mrest/http.dn2.keytab";
    private static String wholePrincipal = "HTTP/bdap-dn-2.cebbank.com@CEBBANK.COM";
    private static String shortName = "HTTP/bdap-dn-2.cebbank.com";
    private static String krb5Path = "/etc/krb5.conf";

    public static String getJassPath() {
        return jassPath;
    }

    public static void setJassPath(String jassPath) {
        KbrConfig.jassPath = jassPath;
    }

    public static String getAuthType() {
        return authType;
    }

    public static void setAuthType(String authType) {
        KbrConfig.authType = authType;
    }

    public static String getKeytabPath() {
        return keytabPath;
    }

    public static void setKeytabPath(String keytabPath) {
        KbrConfig.keytabPath = keytabPath;
    }

    public static String getWholePrincipal() {
        return wholePrincipal;
    }

    public static void setWholePrincipal(String wholePrincipal) {
        KbrConfig.wholePrincipal = wholePrincipal;
    }

    public static String getKrb5Path() {
        return krb5Path;
    }

    public static void setKrb5Path(String krb5Path) {
        KbrConfig.krb5Path = krb5Path;
    }

    public static String getShortName() {
        return shortName;
    }

    public static void setShortName(String shortName) {
        KbrConfig.shortName = shortName;
    }

}
