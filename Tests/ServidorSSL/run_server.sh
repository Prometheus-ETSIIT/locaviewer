#!bin/bash
cd ServidorSSL/src;
java -Djavax.net.ssl.keyStore=mySrvKeystore -Djavax.net.ssl.keyStorePassword=123456 servidorssl.ServidorSSL;
