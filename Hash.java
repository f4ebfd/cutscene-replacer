import java.security.*;
import java.util.zip.CRC32;

// Hashing helper functi0ns
public class Hash {
	// FIXME - exception handling? what exception handling?
	public static byte[] getSHA1(byte[] data, int off, int len) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(data, off, len);
			return md.digest();
		} catch (Exception e) {
			throw new RuntimeException(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	// calculates a dat file compatible file/path hash from a file name
	public static long getFileHash(String fn) {
		CRC32 crc = new CRC32();
		fn = fn.toLowerCase();
		crc.update(fn.getBytes());
		return crc.getValue() ^ 0xffffffffl;
	}
}
