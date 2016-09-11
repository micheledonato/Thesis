//package com.devmicheledonato.thesis;
//
//
//import android.content.Context;
//
//import java.io.BufferedInputStream;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.security.KeyStore;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.cert.Certificate;
//import java.security.cert.CertificateException;
//import java.security.cert.CertificateFactory;
//import java.security.cert.X509Certificate;
//
//public class SecuritySelfSigned {
//
//    private final Context context;
//    private KeyStore keyStore;
//
//    public SecuritySelfSigned(Context context) {
//        this.context = context;
//    }
//
//    private void loadCertificate() {
//        // Load CAs from an InputStream
//        // (could be from a resource or ByteArrayInputStream or ...)
//        CertificateFactory cf = null;
//        try {
//            cf = CertificateFactory.getInstance("X.509");
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        }
//        // From https://www.washington.edu/itconnect/security/ca/load-der.crt
//        InputStream caInput;
////        caInput = new BufferedInputStream(new FileInputStream("load-der.crt"));
//        try {
//            caInput = context.getAssets().open("my_cert.crt");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        Certificate ca;
//        try {
//            ca = cf.generateCertificate(caInput);
//            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
//        } finally {
//            try {
//                caInput.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void createKeyStore() {
//        // Create a KeyStore containing our trusted CAs
//        String keyStoreType = KeyStore.getDefaultType();
//        keyStore = null;
//        try {
//            keyStore = KeyStore.getInstance(keyStoreType);
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        }
//        try {
//            keyStore.load(null, null);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        }
//        keyStore.setCertificateEntry("ca", ca);
//    }
//
//}
