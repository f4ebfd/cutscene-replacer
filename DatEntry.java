import java.io.*;

public class DatEntry {
	private static final int SIZE_OFF = 0x0000;
	private static final int TYPE_OFF = 0x0004;
	private static final int FILE_SIZE_OFF = 0x0008;
	private static final int BUFFER_SIZE_OFF = 0x0010;
	private static final int BLOCKS_OFF = 0x0014;
	private static final int TABLE_OFF = 0x0018;
	private static final int MIN_SIZE = 0x0080;

	private static final int BINARY_TYPE = 2;

	private int size;
	private int type;
	private int fileSize;
	private int bufferSize;
	private int blocks;
	private int datOffset;

	private byte[] rawData;
	private BlockTable2 blockTable2;

	public DatEntry(FileInputStream s, int pos)
	  throws IOException {
		this.datOffset = pos;

		s.getChannel().position(pos);

		// size comes first
		byte[] buf = new byte[4];
		s.read(buf, 0, 4);
		this.size = Binary.getInt32(buf, 0);
		if (this.size < MIN_SIZE) throw new IOException("too small Dat Entry " + this.size);

		// read the whole block
		this.rawData = new byte[this.size];
		System.arraycopy(buf, 0, this.rawData, 0, 4);
		s.read(this.rawData, 4, this.size - 4);

		// assign values
		this.type = Binary.getInt32(this.rawData, TYPE_OFF);
		this.fileSize = Binary.getInt32(this.rawData, FILE_SIZE_OFF);
		this.bufferSize = Binary.getInt32(this.rawData, BUFFER_SIZE_OFF);
		this.blocks = Binary.getInt32(this.rawData, BLOCKS_OFF);

		// read block table if binary file
		if (this.type == BINARY_TYPE) {
			this.blockTable2 = new BlockTable2(this.rawData, TABLE_OFF, this.blocks);
		} else this.blockTable2 = null;
	}

	public int getHeaderSize() { return this.size; }
	public int getType() { return this.type; }
	public int getFileSize() { return this.fileSize; }
	public int getBufferSize() { return this.bufferSize; }
	public int getBlockCount() { return this.blocks; }
	public BlockTable2 getBlockTable2() { return this.blockTable2; }

	public byte[] readBlock(FileInputStream s, int i) throws IOException {
		if (i < 0 || i >= this.blocks) throw new IOException("bad block " + i + " > " + this.blocks);

		// header
		byte[] blockHeader = new byte[16];
		s.getChannel().position(this.datOffset + this.size + this.blockTable2.getOffset(i));
		s.read(blockHeader, 0, 16);

		int header = Binary.getInt32(blockHeader, 0);
		if (header != 16) throw new IOException("bad block header size " + header);
		int compressed = Binary.getInt32(blockHeader, 8);
		int decompressed = Binary.getInt32(blockHeader, 12);

		// data
		byte[] data = new byte[compressed];
		s.read(data, 0, compressed);

		return data;
	}

	public void writeBlock(RandomAccessFile s, int i, byte[] block, int size) throws IOException {
		String us = this.datOffset + ": ";
		if (i < 0 || i >= this.blocks) throw new IOException(us + "bad block " + i + " > " + this.blocks);
		if (block.length > this.blockTable2.getSize(i))
			throw new IOException(us + "too big block " + block.length + " > " + this.blockTable2.getSize(i));
		if (size > 16000) throw new IOException(us + "too much data " + size + " > 16000");

		// rewrite the block data
		byte[] blockData = BlockTable2.buildBlock(this.blockTable2.getOffset(i), block.length, size);
		// the offset and block size remain same, don't change them
		s.seek(this.datOffset + TABLE_OFF + i * 8 + 6);
		s.write(blockData, 6, 2);

		// write the block header
		s.seek(this.datOffset + this.size + this.blockTable2.getOffset(i) + 8);
		s.write(blockData, 4, 2);
		s.write(0);
		s.write(0);
		s.write(blockData, 6, 2);
		s.write(0);
		s.write(0);

		// write the block
		s.write(block);

		// pad with zeroes
		int left = this.blockTable2.getSize(i) - block.length - 0x10;
		while (left-- > 0) s.write(0);
	}
}
