import java.io.*;

public class IndexSegments {
	private static final int OFFSET_ROFF = 0x0004; // segment offset, relative to segment header
	private static final int SIZE_ROFF = 0x0008; // segment size, relative to segment header
	private static final int HASH_ROFF = 0x000c; // segment header hash, relative to segment header
	private static final int HASH_OFF = 0x03c0; // header hash offset

	private static final int[] SEG_OFF = { 0x0004, 0x0050, 0x0098, 0x00e0 }; // segment start offsets

	private static final int SIZE = 0x400; // expected size of the header
	private static final int HASH_SIZE = 20; // SHA-1 hash in bytes

	private static final int SEGMENTS = 4;

	private int[] segOffset = new int[SEGMENTS];
	private int[] segSize = new int[SEGMENTS];
	private int size;

	private byte[] rawData;

	public IndexSegments(InputStream stream) throws IOException {
		// header size
		byte[] buf = new byte[4];
		if (stream.read(buf, 0, 4) < 4) throw new IOException("failed to read segment header size");
		this.size = Binary.getInt32(buf, 0);
		if (this.size != SIZE) throw new IOException("unexpected header size " + this.size);

		// read the rest
		this.rawData = new byte[this.size];
		System.arraycopy(buf, 0, this.rawData, 0, 4);
		if (stream.read(this.rawData, 4, this.size - 4) < this.size - 4)
			throw new IOException("could not read the full header");

		// get the segment data
		for (int i = 0; i < SEGMENTS; i++) {
			this.segOffset[i] = Binary.getInt32(this.rawData, SEG_OFF[i] + OFFSET_ROFF);
			this.segSize[i] = Binary.getInt32(this.rawData, SEG_OFF[i] + SIZE_ROFF);
		}
	}

	public boolean isHashValid() {
		// only checks the header hash, not the segment hashes
		byte[] hash = Hash.getSHA1(this.rawData, 0, HASH_OFF);
		for (int i = 0; i < hash.length; i++) {
			if (hash[i] != this.rawData[HASH_OFF + i]) return false;
		}
		return true;
	}

	// todo - public boolean isSegmentHashValid(int segment)

	public int getSize() { return this.size; }
	public int getSegments() { return SEGMENTS; }
	public int getSegmentOffset(int n) { return (n >= 0 && n < SEGMENTS) ? this.segOffset[n] : -1; }
	public int getSegmentSize(int n) { return (n >= 0 && n < SEGMENTS) ? this.segSize[n] : -1; }

	public IndexFile getFile(FileInputStream s, String fn) throws IOException {
		return IndexFile.getFile(this, s, Hash.getFileHash(fn));
	}
}
