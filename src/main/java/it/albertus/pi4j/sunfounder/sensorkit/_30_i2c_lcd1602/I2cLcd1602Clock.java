package it.albertus.pi4j.sunfounder.sensorkit._30_i2c_lcd1602;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

import com.pi4j.wiringpi.I2C;

public class I2cLcd1602Clock {

	private static final int LCD_HEIGHT = 2;
	private static final int LCD_WIDTH = 16;

	private static final int LCD_ADDRESS = 0x27;
	private static final int BLEN = 1;

	private static final long DELAY_COMMAND = (long) Math.ceil(0.612); // PCF8574: Eh_min=612 us, Eh_max=408 us
	private static final long DELAY_DATA = (long) Math.ceil(0.612); // PCF8574: Eh_min=612 us, Eh_max=408 us

	private static int fd;

	private static final DateFormat dfDate = DateFormat.getDateInstance();
	private static final DateFormat dfTime = DateFormat.getTimeInstance();

	private static final char matrix[][] = new char[LCD_HEIGHT][LCD_WIDTH];

	private static void writeWord(final int data) {
		int temp = data;
		if (BLEN == 1) {
			temp |= 0x08; // 0000 1000
		}
		else {
			temp &= 0xF7; // 1111 0111
		}

		// Debug
		final String bits = String.format("%8s", Integer.toBinaryString(temp)).replace(' ', '0');
		System.out.println("    " + bits);

		I2C.wiringPiI2CWrite(fd, temp);
	}

	private static synchronized void sendCommand(final int comm) {
		System.out.println("<command value=\"" + String.format("%8s", Integer.toBinaryString(comm)).replace(' ', '0') + "\" hex=\"0x" + String.format("%02X", comm) + "\">");
		int buf;
		// Send bit7-4 firstly
		buf = comm & 0xF0; // 1111 0000 (get the high nibble)
		buf |= 0x04; // RS = 0, RW = 0, EN = 1 // 0000 0100 (assert the E strobe)
		writeWord(buf);
		delay(DELAY_COMMAND);
		buf &= 0xFB; // Make EN = 0 // 1111 1011 (terminate the E strobe)
		writeWord(buf);

		// Send bit3-0 secondly
		buf = (comm & 0x0F) << 4; // 0000 1111 (get the low nibble and shift left)
		buf |= 0x04; // RS = 0, RW = 0, EN = 1 // 0000 0100 (assert the E strobe)
		writeWord(buf);
		delay(DELAY_COMMAND);
		buf &= 0xFB; // Make EN = 0 // 1111 1011 (terminate the E strobe)
		writeWord(buf);
		System.out.println("</command>");
	}

	private static synchronized void sendData(final int data) {
		System.out.println("<data value=\"" + String.format("%8s", Integer.toBinaryString(data)).replace(' ', '0') + "\" hex=\"0x" + String.format("%02X", data) + "\" char=\"" + String.format("%c", (char) data) + "\">");
		int buf;
		// Send bit7-4 firstly
		buf = data & 0xF0; // 1111 0000 (get the high nibble)
		buf |= 0x05; // RS = 1, RW = 0, EN = 1 // 0000 0101 (assert the E strobe)
		writeWord(buf);
		delay(DELAY_DATA);
		buf &= 0xFB; // Make EN = 0 // 1111 1011 (terminate the E strobe)
		writeWord(buf);

		// Send bit3-0 secondly
		buf = (data & 0x0F) << 4; // 0000 1111 (get the low nibble and shift left)
		buf |= 0x05; // RS = 1, RW = 0, EN = 1 // 0000 0101 (assert the E strobe)
		writeWord(buf);
		delay(DELAY_DATA);
		buf &= 0xFB; // Make EN = 0 // 1111 1011 (terminate the E strobe)
		writeWord(buf);
		System.out.println("</data>");
	}

	private static void init() {
		sendCommand(0x33); // Must initialize to 8-line mode at first // 0011 0011
		delay(5);
		sendCommand(0x32); // Then initialize to 4-line mode // 0011 0010
		delay(5);
		sendCommand(0x28); // 2 Lines & 5*7 dots // 0010 1000
		delay(5);
		sendCommand(0x0C); // Enable display without cursor // 0000 1100
		delay(5);
		clear(); // Clear Screen
		I2C.wiringPiI2CWrite(fd, 0x08); // 0000 1000
	}

	private static void clear() {
		sendCommand(0x01); //clear Screen
		delay(5);
	}

	private static void write(int x, int y, String data) {
		int addr, i;
		if (x < 0)
			x = 0;
		if (x > LCD_WIDTH - 1)
			x = LCD_WIDTH - 1;
		if (y < 0)
			y = 0;
		if (y > LCD_HEIGHT - 1)
			y = LCD_HEIGHT - 1;

		// Data reduction (ignore equal leading characters)
		int offsetX = x;
		boolean stop = false;
		String reducedData = new String(data);
		for (i = 0; i < data.length(); i++) {
			final char c = data.charAt(i);
			if (matrix[y][x + i] == c) {
				if (!stop) {
					offsetX++;
					reducedData = reducedData.substring(1);
				}
			}
			else {
				matrix[y][x + i] = c;
				stop = true;
			}
		}

		// Move cursor
		addr = 0x80 + 0x40 * y + offsetX;
		sendCommand(addr);

		for (i = 0; i < reducedData.length(); i++) {
			sendData(reducedData.charAt(i));
		}
	}

	public static void main(final String... args) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				clear();
			}
		});

		fd = I2C.wiringPiI2CSetup(LCD_ADDRESS);
		init();

		String oldDateStr = "";
		String oldTimeStr = "";
		while (true) {
			boolean log = false;
			final Date date = new Date();
			final String dateStr = dfDate.format(date);
			final String timeStr = dfTime.format(date);
			if (!dateStr.equals(oldDateStr)) {
				String toPrint = dateStr;
				if (oldDateStr.length() > dateStr.length()) {
					for (int i = dateStr.length(); i < oldDateStr.length(); i++) {
						toPrint += ' ';
					}
				}
				write(0, 0, toPrint);
				oldDateStr = dateStr;
				log = true;
			}
			if (!timeStr.equals(oldTimeStr)) {
				String toPrint = timeStr;
				if (oldTimeStr.length() > timeStr.length()) {
					for (int i = timeStr.length(); i < oldTimeStr.length(); i++) {
						toPrint += ' ';
					}
				}
				write(0, 1, toPrint);
				oldTimeStr = timeStr;
				log = true;
			}
			if (log) {
				System.out.println(Arrays.toString(matrix[0]).replace("\0", " ").replace(", ", ""));
				System.out.println(Arrays.toString(matrix[1]).replace("\0", " ").replace(", ", ""));
			}
			delay(50);
		}
	}

	private static void delay(final long millis) {
		if (millis > 0) {
			try {
				Thread.sleep(millis);
			}
			catch (final InterruptedException ie) {
				throw new RuntimeException(ie);
			}
		}
	}

}
