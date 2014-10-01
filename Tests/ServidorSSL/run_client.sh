#!bin/bash
cd ServidorSSL/src;
java -Djavax.net.ssl.trustStore=mySrvKeystore -Djavax.net.ssl.trustStorePaword=123456 servidorssl.ClienteSSL

