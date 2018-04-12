import java.io.*;

public class IndexHeader {
	private static final byte[] MAGIC = { 'S', 'q', 'P', 'a', 'c', 'k', 0, 0, 0, 0, 0, 0 };
	private static final int SIZE_OFF = 0x000c; // byte offset to size
	private static final int TYPE_OFF = 0x0014; // byte offset to type
	private static final int HASH_OFF = 0x03c0; // byte offset to SHA-1 hash of header
	private static final int SIZE = 0x400; // expected size of the header
	private static final int TYPE_INDEX = 2; // type 2 = index

	private static final int HASH_SIZE = 20; // SHA-1 hash in bytes

	private int size;
	private int type;
	private byte[] rawData;

	public IndexHeader(InputStream stream) throws IOException {
		// check the header magic bytes
		int peek = 16;
		byte[] buf = new byte[peek];
		if (stream.read(buf) < peek) throw new IOException("not an SqPack file");

		// get header size
		this.size = Binary.getInt32(buf, SIZE_OFF);
		if (this.size != SIZE) throw new IOException("unexpected header size " + this.size);

		// header okay, read the data
		this.rawData = new byte[this.size];
		System.arraycopy(buf, 0, this.rawData, 0, peek);
		if (stream.read(rawData, peek, this.size - peek) < this.size - peek)
			throw new IOException("could not read the full header");

		this.type = Binary.getInt32(this.rawData, TYPE_OFF);
		if (this.type != 2) throw new IOException("not an index");

		// TODO perhaps do hash check here?
	}

	public boolean isHashValid() {
		byte[] hash = Hash.getSHA1(this.rawData, 0, HASH_OFF);
		for (int i = 0; i < hash.length; i++) {
			if (hash[i] != this.rawData[HASH_OFF + i]) return false;
		}
		return true;
	}

	public int getSize() { return this.size; }
	public int getType() { return this.type; }

	public byte[] getHash() {
		byte[] hash = new byte[HASH_SIZE];
		System.arraycopy(this.rawData, HASH_OFF, hash, 0, HASH_SIZE);
		return hash;
	}

	public byte[] getData() {
		// make a copy of the data, so caller can't modify the original
		byte[] copy = new byte[this.rawData.length];
		System.arraycopy(this.rawData, 0, copy, 0, this.rawData.length);
		return copy;
	}
}
