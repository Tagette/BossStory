cd "$(dirname "$0")"
export CLASSPATH=./*
java -Xmx1536m -Dwzpath=wz -Djavax.net.ssl.keyStore=filename.keystore -Djavax.net.ssl.keyStorePassword=passwd -Djavax.net.ssl.trustStore=filename.keystore -Djavax.net.ssl.trustStorePassword=passwd Main