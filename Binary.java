// Helper functions to convert raw binary data to readable formats and vice versa?
public class Binary {
	public static int shift(byte b, int bits) { return (((int)b) & 0xff) << bits; }

	public static int getInt32(byte[] raw, int off) {
		return shift(raw[off], 0) + shift(raw[off+1], 8) + shift(raw[off+2], 16) + shift(raw[off+3], 24);
	}

	public static int getInt32BE(byte[] raw, int off) {
		return shift(raw[off], 24) + shift(raw[off+1], 16) + shift(raw[off+2], 8) + shift(raw[off+3], 0);
	}

	public static int getInt16(byte[] raw, int off) {
		return shift(raw[off], 0) + shift(raw[off+1], 8);
	}

	public static int getInt16BE(byte[] raw, int off) {
		return shift(raw[off], 8) + shift(raw[off+1], 0);
	}

	public static String byteToHex(byte b) {
		char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		return hex[(b >> 4) & 15] + "" + hex[b & 15];
	}

	public static String bytesToHex(byte[] buf) {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < buf.length; i++) s.append(byteToHex(buf[i]));
		return s.toString();
	}

	public static String bytesToHex(byte[] buf, boolean reverse) {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < buf.length; i++) s.append(byteToHex(buf[reverse ? (buf.length - i - 1) : i]));
		return s.toString();
	}

	public static String longToHex(long num) {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < 8; i++) {
			s.append(byteToHex((byte)(num >> ((7-i)*8))));
		}
		return s.toString();
	}

	public static String uintToHex(long num) {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < 4; i++) {
			s.append(byteToHex((byte)(num >> ((3-i)*8))));
		}
		return s.toString();
	}
}
