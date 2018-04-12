import java.io.*;

public class IndexFile {
	private static final int FILE_OFF = 0x0000;
	private static final int PATH_OFF = 0x0004;
	private static final int SIZE_OFF = 0x0008;
	public static final int DATA_SIZE = 16;
	private static final int MAX_READ = 1024; // read 1024 entries at a time at most

	private long fileHash;
	private long pathHash;
	private long offset;
	private int datFile;

	private IndexFile() { }
	private IndexFile(byte[] data, int off) {
		// don't check the buffer, just let it throw an exception if it's out of bounds
		this.fileHash = (long)Binary.getInt32(data, off + FILE_OFF) & 0xffffffffl;
		this.pathHash = (long)Binary.getInt32(data, off + PATH_OFF) & 0xffffffffl;

		int s = Binary.getInt32(data, off + SIZE_OFF);
		this.offset = (s >> 4) * 128;
		this.datFile = (s & 15) >> 1;
	}

	public long getFileHash() { return this.fileHash; }
	public long getPathHash() { return this.pathHash; }
	@Deprecated
	public long getFileOffset() { return this.offset; } /* FIXME remove this, use getDatOffset */
	@Deprecated
	public long getOffset() { return this.offset; }
	public long getDatOffset() { return this.offset; }
	public int getDatFile() { return this.datFile; }

	public static int getFileIndex(IndexSegments seg, int offset) {
		if (offset < seg.getSegmentOffset(0)) throw new IndexOutOfBoundsException("bad offset");
		return (offset - seg.getSegmentOffset(0)) / DATA_SIZE;
	}

	public static int getFileCount(IndexSegments seg) { return seg.getSegmentSize(0) / DATA_SIZE; }
	public static IndexFile getFile(IndexSegments seg, FileInputStream s, int i)
	  throws IOException {
		return getFiles(seg, s, i, 1)[0];
	}

	public static IndexFile getFile(IndexSegments seg, FileInputStream stream, long hash)
	  throws IOException {
		stream.getChannel().position(seg.getSegmentOffset(0));

		byte[] buf = new byte[MAX_READ * DATA_SIZE];
		int left = getFileCount(seg);
		while (left > 0) {
			int count = (left < MAX_READ) ? left : MAX_READ;
			stream.read(buf, 0, count * DATA_SIZE);

			for (int i = 0; i < count; i++) {
				IndexFile file = new IndexFile(buf, i * DATA_SIZE);
				if (file.getFileHash() == hash) return file;
			}

			left -= count;
		}

		return null;
	}

	public static IndexFile[] getFiles(IndexSegments seg, FileInputStream stream, int i, int num)
	  throws IOException {
		// make sure the index has enough files
		if (getFileCount(seg) < i + num)
			throw new IndexOutOfBoundsException("IndexFile out of bounds");

		// seek the file stream
		stream.getChannel().position(seg.getSegmentOffset(0) + i * DATA_SIZE);

		// prepare buffers
		IndexFile[] files = new IndexFile[num];
		byte[] buf = new byte[MAX_READ * DATA_SIZE];

		// read the file entries
		int left = num;
		int pos = 0;
		while (left > 0) {
			int count = (left < MAX_READ) ? left : MAX_READ;
			stream.read(buf, 0, count * DATA_SIZE);

			for (int j = 0; j < count; j++) {
				files[pos++] = new IndexFile(buf, j * DATA_SIZE);
			}

			left -= count;
		}

		return files;
	}

	public DatEntry getDatEntry(FileInputStream s) throws IOException {
		return new DatEntry(s, (int)this.offset);
	}
}
