package models;

import java.util.Base64;

public class PasswordEncryptDecrypt {

    public String getEncryptedPassword(String pswrd) {
       return Base64.getEncoder().encodeToString(pswrd.getBytes());
    }

    public String decryptPassword(String pswrd) {
        byte[] bytes = Base64.getMimeDecoder().decode(pswrd);
        return new String(bytes);
    }
}
