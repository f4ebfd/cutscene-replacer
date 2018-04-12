import java.io.*;

// Block table for binary files
public class BlockTable2 {
	private static final int DATA_SIZE = 8;
	private int blocks;
	private int[] offTable;
	private int[] sizeTable;
	private int[] dataSizeTable;

	public BlockTable2(byte[] buf, int off, int count) {
		this.blocks = count;
		this.offTable = new int[this.blocks];
		this.sizeTable = new int[this.blocks];
		this.dataSizeTable = new int[this.blocks];

		for (int i = 0; i < this.blocks; i++) {
			this.offTable[i] = Binary.getInt32(buf, off + i * DATA_SIZE);
			this.sizeTable[i] = Binary.getInt16(buf, off + i * DATA_SIZE + 4);
			this.dataSizeTable[i] = Binary.getInt16(buf, off + i * DATA_SIZE + 6);
		}
	}

	public int getBlocks() { return this.blocks; }
	public int getOffset(int i) { return this.offTable[i]; }
	public int getSize(int i) { return this.sizeTable[i]; }
	public int getDataSize(int i) { return this.dataSizeTable[i]; }

	public static int tableSize(int count) { return count * DATA_SIZE; }

	public static byte[] buildBlock(int off, int size, int dataSize) {
		byte[] data = new byte[DATA_SIZE];
		data[0] = (byte)(off & 0xff);
		data[1] = (byte)(off >> 8 & 0xff);
		data[2] = (byte)(off >> 16 & 0xff);
		data[3] = (byte)(off >> 24 & 0xff);
		data[4] = (byte)(size & 0xff);
		data[5] = (byte)(size >> 8 & 0xff);
		data[6] = (byte)(dataSize & 0xff);
		data[7] = (byte)(dataSize >> 8 & 0xff);
		return data;
	}
}
