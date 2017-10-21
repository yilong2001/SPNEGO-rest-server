package com.spengo.bdap.auth.kbr;

import javax.security.auth.callback.*;
import java.io.IOException;

/**
 * Created by yilong on 2017/4/9.
 */
public class DefaultKbrAuthCallback implements CallbackHandler {
    String username;
    char pw[] = {};

    public DefaultKbrAuthCallback(String un) {
        username = un;
    }

    public void handle(Callback[] callbacks) throws
            IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                NameCallback nc = (NameCallback)callbacks[i];
                nc.setName(username);
                System.out.println("NameCallback ... ");
            } else if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback)callbacks[i];
                pc.setPassword(pw);
                System.out.println("PasswordCallback ... ");
            } else {
                throw new UnsupportedCallbackException(callbacks[i],
                        "Unrecognized Callback");
            }
        }
    }

}
