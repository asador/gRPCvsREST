# 1. Generate CA's private key and self-signed certificate
openssl req -x509 -newkey rsa:4096 -days 1000 -nodes -keyout ca-key.pem -out ca-cert.pem -subj "/C=CA/ST=Ontario/L=Toronto/O=RnD/OU=Education/CN=CA-rndorg.net/emailAddress=fake-ca-email@nowhere.com"

echo "CA's self-signed certificate"
openssl x509 -in ca-cert.pem -noout -text

# 2. Generate server's private key and certificate signing request (CSR)
openssl req -newkey rsa:4096 -nodes -keyout server-key.pem -out server-req.pem -subj "/C=CA/ST=ON/L=Toronto/O=RnD/OU=POC/CN=*.myrndorg.net/emailAddress=fakeemail@nowhere.com"

# 3. Use CA's private key to sign server's CSR and get back the signed certificate
openssl x509 -req -in server-req.pem -days 365 -CA ca-cert.pem -CAkey ca-key.pem -CAcreateserial -out server-cert.pem -extfile server-ext.conf

#echo "Server's signed certificate"
openssl x509 -in server-cert.pem -noout -text
