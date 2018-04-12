import java.io.*;
import java.util.zip.*;
import javax.swing.JOptionPane;

public class ReplaceCutscenes {
	private static byte[] readFile(String fn) throws IOException {
		FileInputStream in = new FileInputStream(fn);

		byte[] temp = new byte[512*1024];
		int size = in.read(temp);
		byte[] buffer = new byte[size];
		System.arraycopy(temp, 0, buffer, 0, buffer.length);

		in.close();
		return buffer;
	}

	public static void main(String[] args) {
		String datBase = "0a0000.win32";
		String cutsceneFile = "cutscene_0.exd";

		String helpMsg = "<html><body>Are you sure you want to do this, Dave?<br>" +
			"More info at: https://github.com/f4ebfd/cutscene-replacer <body></html>";
		String titleMsg = "Cutscene Replacer";

		int r = JOptionPane.showConfirmDialog(null, helpMsg, titleMsg, JOptionPane.YES_NO_OPTION);
		if (r != JOptionPane.YES_OPTION) {
			System.out.println("Cancelled...");
			return;
		}

		try {
			//
			// Read the cutscene file from the .dat
			//
			FileInputStream s = new FileInputStream(datBase + ".index");
			FileInputStream ds = new FileInputStream(datBase + ".dat0");
			IndexHeader header = new IndexHeader(s);
			IndexSegments segments = new IndexSegments(s);

			// find the cutscene file index
			IndexFile f0 = segments.getFile(s, cutsceneFile);
			if (f0 == null)
				throw new Exception(cutsceneFile + " not found");
			DatEntry e = f0.getDatEntry(ds);

			// write the file to disk
			FileOutputStream out = new FileOutputStream(cutsceneFile);
			Inflater inf = new Inflater(true);
			byte[] infOut = new byte[16000];

			for (int i = 0; i < e.getBlockCount(); i++) {
				byte[] buf = e.readBlock(ds, i);
				inf.reset();
				inf.setInput(buf);
				int bytes = inf.inflate(infOut);
				out.write(infOut, 0, bytes);
			}

			out.close();


			//
			// Replace the MSQ cutscenes with random skippable cutscene
			//
			String replace = "ffxiv/vygest/vygest00010/vygest00010";
			String match = "ffxiv/";
			int replen = replace.length();

			// replace the MSQ cutscenes in the exd file
			byte[] buf = readFile(cutsceneFile);
			int size = buf.length;
			for (int i = 0; i < size - replen; i++) {
				int mlen = 0;
				while (mlen < match.length() && buf[i+mlen] == (byte)match.charAt(mlen)) mlen++;
				if (mlen < match.length()) continue;

				int slen = 0;
				for (; slen < replen && buf[i+slen] != 0; slen++) ;
				if (slen != replen || buf[i+slen] != 0) continue;

				for (int j = 0; j < slen; j++) buf[i+j] = (byte)replace.charAt(j);
			}

			// DEBUG - write the modified file contents to another file
			FileOutputStream debugOut = new FileOutputStream("cutscene_0.new");
			debugOut.write(buf, 0, size);
			debugOut.close();

			// Recompress the modified data and try to overwrite the .dat file
			e = f0.getDatEntry(ds);

			s.close(); // .index and .dat0 files no longer needed for reading
			ds.close();

			RandomAccessFile rout = new RandomAccessFile(datBase + ".dat0", "rw");
			Deflater def = new Deflater(Deflater.BEST_COMPRESSION, true);
			byte[] defBuf = new byte[16000];
			int block = 0;
			for (int i = 0; i < size; block++) {
				int max = size - i;
				if (max > 16000) max = 16000;

				def.reset();
				def.setInput(buf, i, max);
				def.finish();
				int bytes = def.deflate(defBuf, 0, defBuf.length);
				byte[] newBuf = new byte[bytes];
				System.arraycopy(defBuf, 0, newBuf, 0, bytes);
				e.writeBlock(rout, block, newBuf, max);

				i += max;
			}
			rout.close();
		} catch (Exception e) {
			String msg = e.getClass().getName() + ": " + e.getMessage();
			JOptionPane.showMessageDialog(null, "<html>Something failed:<br>" + msg + "</html>", "Oops!", JOptionPane.ERROR_MESSAGE);
			System.err.println("fail: " + msg);
			e.printStackTrace(System.err);
			return;
		}

		JOptionPane.showMessageDialog(null, "Everything seems to have worked...", "Success!", JOptionPane.INFORMATION_MESSAGE);
	}
}
