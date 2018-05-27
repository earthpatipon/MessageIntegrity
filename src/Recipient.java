import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Recipient {

	private String hash;
	private String text;
	private String checksum;
	private KeyPair key;
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	public Recipient() throws NoSuchAlgorithmException {
		key = KeyPairGenerator.getInstance("RSA").generateKeyPair();
		privateKey = key.getPrivate();
		publicKey = key.getPublic();
	}

	public void read(String algorithm, PublicKey senderPublicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, SignatureException, InvalidAlgorithmParameterException {
		
		text = readFile("email");
		
		//Radix-64 conversion
		byte[] decodedBytes = Base64.getDecoder().decode(text);
		String message = new String(decodedBytes);
		
		/*
		 *  message = sessionKeyComponent + dsComponent + cipher 	
		 */
		
		String[] component = message.split(",");
		System.out.println(component.length);
		
		/*
		 * 
		 *  component.length = 3
		 *  
		 *  component[0] = session key
		 *  component[1] = digital signature
		 *  component[2] = cipher text
		 *  
		 */
		
		if (component.length == 3) {
			
//			String[] sessionKeyComponent = component[0].split(",");
//			String[] dsComponent = component[1].split(",");
			text = component[2];
			
			System.out.println("\n"+component[0]);
			System.out.println(component[1]);
			System.out.println(text);
			
			// Decrypt session key using private key of the recipient
			Cipher sessionKey = Cipher.getInstance("RSA");
			sessionKey.init(Cipher.DECRYPT_MODE, privateKey);
			sessionKey.doFinal(component[0].getBytes());
			
			// Verify Digital Signature
			Signature sign = Signature.getInstance("SHA1withRSA");
			sign.initVerify(senderPublicKey);
			sign.update(text.getBytes());
			
			if (sign.verify(component[1].getBytes())) {
				
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.DECRYPT_MODE, (Key) sessionKey, cipher.getParameters());
				byte[] sentTextBytes = cipher.doFinal(text.getBytes());
				String sentText = new String(sentTextBytes);
				
				switch (algorithm) {
					case "SHA-256":
						try {
							hash = sha256(sentText);
							checksum = readFile("checksum");
							validate(hash, checksum);
						} catch (NoSuchAlgorithmException e) {
							e.printStackTrace();
						}
						break;
					case "MD5":
						try {
							hash = md5(sentText);
							checksum = readFile("checksum");
							validate(hash, checksum);
						} catch (NoSuchAlgorithmException e) {
							e.printStackTrace();
						}
						break;
					case "SHA-1":
						try {
							hash = sha1(sentText);
							checksum = readFile("checksum");
							validate(hash, checksum);
						} catch (NoSuchAlgorithmException e) {
							e.printStackTrace();
						}
					default:
						break;
					}
			}
			else {
				System.out.println("\ndigital signature not match\n");
			}
			
		}
		else {
			System.out.println("Error!");
		}
	}

	public String readFile(String filename) {
		BufferedReader br = null;
		FileReader fr = null;
		StringBuilder sb = null;
		String currentLine = "";

		try {
			fr = new FileReader("inbox/" + filename + ".txt");
			br = new BufferedReader(fr);
			sb = new StringBuilder();
			
			while ((currentLine = br.readLine()) != null) {
				sb.append(currentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public void validate(String hashText, String checksumValue) {
		if(hashText.equals(checksumValue)) {
			System.out.println("Correct. This email message is secured.");
		}
		else {
			System.out.println("Incorrect. This email message isn't secured.");
		}
	}

	public String sha256(String text) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(text.getBytes());

		// convert byte to hex
		byte byteData[] = md.digest();
		StringBuffer strBuffer = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			String hex = Integer.toHexString(0xff & byteData[i]);
			if (hex.length() == 1)
				strBuffer.append('0');
			strBuffer.append(hex);
		}
		System.out.println("Text in email: " + text);
		System.out.println("Checksum with SHA-256 (Receiver): " + strBuffer.toString());
		return strBuffer.toString();
	}
	
	public String md5 (String text) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(text.getBytes());
		
		//convert byte to hex
		byte[] mdBytes = md.digest();
		StringBuffer strBuffer = new StringBuffer();
		for (int i = 0; i < mdBytes.length; i++) {
			// set strBuffer 
			strBuffer.append(Integer.toString((mdBytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		System.out.println("Text in email: " + text);
		System.out.println("Checksum with MD5 (Receiver): " + strBuffer.toString());
		return strBuffer.toString();
	}
	
	public String sha1 (String text) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(text.getBytes());
		
		//convert byte to hex
		byte[] mdBytes = md.digest();
		StringBuffer strBuffer = new StringBuffer();
		for (int i = 0; i < mdBytes.length; i++) {
			// set strBuffer 
			strBuffer.append(Integer.toString((mdBytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		System.out.println("Text in email: " + text);
		System.out.println("Checksum with SHA-1 (Receiver): " + strBuffer.toString());
		return strBuffer.toString();
	}
	
	public String getText() {
		return this.text;
	}
	
	public PublicKey getPublicKey () {
		return publicKey;
	}
}
