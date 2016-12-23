package it.albertus.pi4j.sunfounder.sensorkit._30_i2c_lcd1602;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import com.pi4j.wiringpi.I2C;

public class I2cLcd1602Console extends OutputStream {

	protected static final PrintStream defaultSysOut = System.out;
	protected static final PrintStream defaultSysErr = System.err;
	protected static final String newLine = System.getProperty("line.separator");

	private final int width;
	private final int height;
	private final int lcdAddress;
	private final int bLen;
	private final boolean redirectSystemStream;
	private final boolean clearOnExit;
	private final int fd;

	private StringBuilder buffer = new StringBuilder();
	private int lineNumber;

	public I2cLcd1602Console(final int lcdAddress, final int bLen, final int width, final int height, final boolean redirectSystemStream, final boolean clearOnExit) {
		this.width = width;
		this.height = height;
		this.lcdAddress = lcdAddress;
		this.bLen = bLen;
		this.clearOnExit = clearOnExit;
		this.redirectSystemStream = redirectSystemStream;
		if (redirectSystemStream) {
			redirectStreams();
		}
		if (clearOnExit) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					delay(100);
					clear();
				}
			});
		}
		fd = I2C.wiringPiI2CSetup(lcdAddress);
		init();
		clear();
	}

	@Override
	public void write(final int b) throws IOException {
		ensureOpen();
		buffer.append((char) b);
		if (b == newLine.charAt(newLine.length() - 1)) {
			flush();
		}
	}

	@Override
	public void flush() throws IOException {
		ensureOpen();
		if (buffer.length() != 0) {
			print(buffer);
			buffer = new StringBuilder();
		}
	}

	private void print(final StringBuilder buffer) {
		if (buffer.indexOf(newLine) != -1) {
			buffer.replace(buffer.indexOf(newLine), buffer.indexOf(newLine) + newLine.length(), "");
		}
		for (int i = buffer.length(); i < width; i++) {
			buffer.append(' ');
		}
		write(0, lineNumber % height, buffer.toString());
		if (++lineNumber >= height) {
			lineNumber = 0;
		}
	}

	@Override
	public void close() {
		try {
			flush();
		}
		catch (final Exception e) {
			return; // Already closed
		}
		if (redirectSystemStream) {
			resetStreams();
		}
		buffer = null;
	}

	private void ensureOpen() throws IOException {
		if (buffer == null) {
			throw new IOException("Stream closed");
		}
	}

	protected void redirectStreams() {
		final PrintStream ps = new PrintStream(this);
		try {
			System.setOut(ps);
			System.setErr(ps);
		}
		catch (final RuntimeException re) {
			re.printStackTrace();
		}
	}

	protected void resetStreams() {
		try {
			System.setOut(defaultSysOut);
			System.setErr(defaultSysErr);
		}
		catch (final RuntimeException re) {
			re.printStackTrace();
		}
	}

	private void writeWord(final int data) {
		int temp = data;
		if (bLen == 1)
			temp |= 0x08;
		else
			temp &= 0xF7;
		I2C.wiringPiI2CWrite(fd, temp);
	}

	private void sendCommand(final int comm) {
		int buf;
		// Send bit7-4 firstly
		buf = comm & 0xF0;
		buf |= 0x04; // RS = 0, RW = 0, EN = 1
		writeWord(buf);
		//		delay(2);
		buf &= 0xFB; // Make EN = 0
		writeWord(buf);

		// Send bit3-0 secondly
		buf = (comm & 0x0F) << 4;
		buf |= 0x04; // RS = 0, RW = 0, EN = 1
		writeWord(buf);
		//		delay(2);
		buf &= 0xFB; // Make EN = 0
		writeWord(buf);
	}

	private void sendData(final int data) {
		int buf;
		// Send bit7-4 firstly
		buf = data & 0xF0;
		buf |= 0x05; // RS = 1, RW = 0, EN = 1
		writeWord(buf);
		//		delay(2);
		buf &= 0xFB; // Make EN = 0
		writeWord(buf);

		// Send bit3-0 secondly
		buf = (data & 0x0F) << 4;
		buf |= 0x05; // RS = 1, RW = 0, EN = 1
		writeWord(buf);
		//		delay(2);
		buf &= 0xFB; // Make EN = 0
		writeWord(buf);
	}

	private void init() {
		sendCommand(0x33); // Must initialize to 8-line mode at first
		delay(5);
		sendCommand(0x32); // Then initialize to 4-line mode
		delay(5);
		sendCommand(0x28); // 2 Lines & 5*7 dots
		delay(5);
		sendCommand(0x0C); // Enable display without cursor
		delay(5);
		sendCommand(0x01); // Clear Screen
		I2C.wiringPiI2CWrite(fd, 0x08);
	}

	private void clear() {
		sendCommand(0x01); //clear Screen
	}

	private void write(int x, int y, final String data) {
		int addr, i;
		int tmp;
		if (x < 0)
			x = 0;
		if (x > width - 1)
			x = width - 1;
		if (y < 0)
			y = 0;
		if (y > height - 1)
			y = height - 1;

		// Move cursor
		addr = 0x80 + 0x40 * y + x;
		sendCommand(addr);

		tmp = data.length();
		for (i = 0; i < tmp; i++) {
			sendData(data.charAt(i));
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getLcdAddress() {
		return lcdAddress;
	}

	public int getbLen() {
		return bLen;
	}

	public int getFd() {
		return fd;
	}

	private static void delay(final long millis) {
		try {
			Thread.sleep(millis);
		}
		catch (final InterruptedException ie) {
			throw new RuntimeException(ie);
		}
	}

	public static void main(final String... args) throws IOException {
		final OutputStream os = new I2cLcd1602Console(0x27, 1, 16, 2, true, true);

		for (final String arg : args) {
			System.out.println(arg);
			delay(500);
		}

		os.close();
	}

}
