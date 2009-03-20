I've been going key crazy over the last several days working on [digital signing in PHP](howto-using-sha1withrsa-signing-in-php), and now Java.  It's not hard, but what's really confusing is all of the key generation and manipulation stuff before you even get to the code (the point where you want to actually use the key to sign some data).

* PHP wants a plain RSA key in DER format.
* Java seems to prefer a PKCS#8 base64 encoded RSA key in DER format with no password.

This stuff is all over the map.

This post is an attempt to document what worked for me.  There are definitely other signing methods out there, but I finally got **PKCS#8 with an RSA key in DER format** to work in Java.  Here's how:

First, generate a new RSA key using `openssl`:

```
openssl genrsa -out key.pem 1024
```

Now, **for Java**, you need to convert the RSA key into a PKCS#8 encoded key in DER format:

```
openssl pkcs8 -topk8 -in key.pem -nocrypt -outform DER -out key.pkcs8
```

Now that you've got a **PKCS#8 encoded key**, you can easily use the [PKCS8EncodedKeySpec](http://java.sun.com/javase/6/docs/api/java/security/spec/PKCS8EncodedKeySpec.html) class to parse the key for signing.  Here's my somewhat hackish code that signs a message (a String) using SHA1withRSA from Sun's JSSE ([Java's Secure Socket Extension](http://java.sun.com/j2se/1.4.2/docs/guide/security/jsse/JSSERefGuide.html)) framework:

```java
package org.kolich.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Signs a byte array (a message) using a PKCS#8 encoded
 * RSA private key.
 * @author kolichko Mark S. Kolich
 *
 */
public class PKCS8RSASigner {

    private static final long MAX_KEY_SIZE_BYTES = 8192L;

    private static final String UTF_8 = "UTF-8";

    private static final String SHA1_WITH_RSA = "SHA1withRSA";
    private static final String SUN_JSSE = "SunJSSE";
    private static final String RSA = "RSA";

    private static final char HEX_DIGIT [] = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    private File pkcsKeyFile_;
    private byte [] keyFileBytes_;
    private Signature dsa_;
    private KeyFactory keyFactory_;
    private PrivateKey privateKey_;

    public PKCS8RSASigner ( File pkcsKeyFile ) {

        try {
            this.pkcsKeyFile_ = pkcsKeyFile;
            this.dsa_ = Signature.getInstance(SHA1_WITH_RSA, SUN_JSSE);
            this.keyFactory_ = KeyFactory.getInstance(RSA, SUN_JSSE);

            this.init();
        } catch ( Exception e ) {
            // Wrap it, so every where that you use PKCS8RSASigner
            // you don't have to wrap the constructor in a try/catch.
            // But the caller should catch Error's though.
            throw new Error(e);
        }

    }

    /**
     * Given a message, generate a signature based on this
     * PKCS#8 private key.
     * @param message
     * @return
     * @throws Exception
     */
    public byte [] getSignature ( byte [] message ) throws Exception {

        this.dsa_.update( message );
        return this.dsa_.sign();

    }

    /**
     * Setup this PKCS8RSASigner.  Load the key file into
     * memory, and init the key factory accordingly.
     * @throws IOException
     */
    private void init ( ) throws Exception {

        FileInputStream is = null;

        if ( !this.pkcsKeyFile_.exists() ) {
            throw new FileNotFoundException( "RSA key file not found!" );
        }

        // Get the size, in bytes, of the key file.
        final long length = this.pkcsKeyFile_.length();

        if ( length > MAX_KEY_SIZE_BYTES ) {
            throw new IOException( "Key file is too big!" );
        }

        try {

            is = new FileInputStream( this.pkcsKeyFile_ );

            int offset = 0;
            int read = 0;
            this.keyFileBytes_ = new byte[(int)length];
            while ( offset < this.keyFileBytes_.length
                        && (read=is.read(this.keyFileBytes_, offset,
                        this.keyFileBytes_.length-offset)) >= 0 ) {
                offset += read;
            }

        } catch ( IOException ioe ) {
            throw ioe;
        } finally {
            try {
                if ( is != null ) {
                    is.close();
                }
            } catch ( IOException ioe ) {
                throw new Exception("Error, couldn't close FileInputStream", ioe);
            }
        }

        PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(
          this.keyFileBytes_ );

        // Get the private key from the key factory.
        this.privateKey_ = keyFactory_.generatePrivate( privKeySpec );

        // Init the signature from the private key.
        this.dsa_.initSign( this.privateKey_ );

    }

    /**
     * Convert a byte array into its hex String equivalent.
     * @param bytes
     * @return
     */
    public static String toHex ( byte [] bytes ) {

        if ( bytes == null ) {
            return null;
        }

        StringBuilder buffer = new StringBuilder(bytes.length*2);
        for ( byte thisByte : bytes ) {
            buffer.append(byteToHex(thisByte));
        }

        return buffer.toString();

    }

    /**
     * Convert a single byte into its hex String
     * equivalent.
     * @param b
     * @return
     */
    private static String byteToHex ( byte b ) {
        char [] array = { HEX_DIGIT[(b >> 4) & 0x0f], HEX_DIGIT[b & 0x0f] };
        return new String(array);
    }

    public static void main ( String [] args ) {

        // A bunch of sample messages to digitally sign
        // using your PKCS#8 encoded private key.
        String [] toSign = {
            "some string",
            "http://kolich.com",
            "bleh bleh bleh"
        };

        // Create a new PKCS8RSASigner using the specified
        // PKCS#8 encoded RSA private key.
        PKCS8RSASigner signer = new PKCS8RSASigner(new File("key.pkcs8"));

        for ( String s : toSign ) {
            try {
                System.out.println(
                        toHex( signer.getSignature(
                                s.getBytes( UTF_8 ) )
                            ).toUpperCase()
                        );
            } catch ( Exception e ) {
                e.printStackTrace( System.err );
            }

        }

    }

}
```

Good luck.