import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Recipient {

	private String hash;
	private String text;
	private String checksum;
	
	public Recipient() {
	}

	public void read(String algorithm) {
		text = readFile("email");

		switch (algorithm) {
			case "SHA-256":
				try {
					hash = sha256(text);
					checksum = readFile("checksum");
					validate(hash, checksum);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				break;
			case "MD5":
				try {
					hash = md5(text);
					checksum = readFile("checksum");
					validate(hash, checksum);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
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
		System.out.println("Checksum with SHA-256 (Recipient): " + strBuffer.toString());
		return strBuffer.toString();
	}
}
