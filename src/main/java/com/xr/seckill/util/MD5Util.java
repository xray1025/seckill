package com.xr.seckill.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {

    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    private static final String salt = "qust201809";

    public static String inputPassToFormPass(String inputPass){
        String str = "" + salt.charAt(1) + salt.charAt(3) + inputPass + salt.charAt(6) + salt.charAt(7);
        return  md5(str);
    }

    public static String formPassToDbPass(String formPass, String salt){
        String str = "" + salt.charAt(1) + salt.charAt(3) + formPass + salt.charAt(6) + salt.charAt(7);
        return  md5(str);
    }

    public static String inputPassToDbPass(String input, String saltDb){
        String formPass = inputPassToFormPass(input);
        String dbPass = formPassToDbPass(formPass, saltDb);
        return dbPass;
    }

    public static void main(String[] args){
        System.out.println(inputPassToFormPass("123456"));
        System.out.println(formPassToDbPass(inputPassToFormPass("123456"),"202107qust"));
        System.out.println(inputPassToDbPass("123456", "202107qust"));
    }

}
