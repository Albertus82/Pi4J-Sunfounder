package it.albertus.pi4j.sunfounder.sensorkit._30_i2c_lcd1602;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.pi4j.wiringpi.I2C;

public class I2cLcd1602Clock {

	private static final short LCD_HEIGHT = 2;
	private static final short LCD_WIDTH = 16;

	private static final byte LCD_ADDRESS = 0x27;
	private static final byte BLEN = 1;

	private static final short DELAY_DATA = (short) Math.ceil(0.612); // PCF8574: Eh_min=612 us, Eh_max=408 us
	private static final short DELAY_COMMAND_SHORT = (short) Math.ceil(0.612); // PCF8574: Eh_min=612 us, Eh_max=408 us
	private static final short DELAY_COMMAND_LONG = 5;

	private static int fd;

	private static final DateFormat dfDate = DateFormat.getDateInstance();
	private static final DateFormat dfTime = new SimpleDateFormat("HH:mm:ss.SSS");

	private static final char matrix[][] = new char[LCD_HEIGHT][LCD_WIDTH];

	private static final boolean LOG_ENABLED = true;

	private static void writeWord(final int data) {
		int temp = data;
		if (BLEN == 1) {
			temp |= Integer.parseInt("00001000", 2); // 0000 1000
		}
		else {
			temp &= Integer.parseInt("11110111", 2); // 1111 0111
		}

		// Debug
		final String bits = String.format("%8s", Integer.toBinaryString(temp)).replace(' ', '0');
		if (LOG_ENABLED) {
			System.out.println("\t\t" + bits);
		}

		I2C.wiringPiI2CWrite(fd, temp);
	}

	private static synchronized void sendCommand(final int comm) {
		if (LOG_ENABLED) {
			System.out.println("\t<command value=\"" + String.format("%8s", Integer.toBinaryString(comm)).replace(' ', '0') + "\" hex=\"0x" + String.format("%02X", comm) + "\">");
		}
		int buf;
		// Send bit7-4 firstly
		buf = comm & Integer.parseInt("11110000", 2); // 1111 0000 (get the high nibble)
		buf |= Integer.parseInt("00000100", 2); // RS = 0, RW = 0, EN = 1 // 0000 0100 (assert the E strobe)
		writeWord(buf);
		delay(DELAY_COMMAND_SHORT);
		buf &= Integer.parseInt("11111011", 2); // Make EN = 0 // 1111 1011 (terminate the E strobe)
		writeWord(buf);

		// Send bit3-0 secondly
		buf = (comm & Integer.parseInt("00001111", 2)) << 4; // 0000 1111 (get the low nibble and shift left)
		buf |= Integer.parseInt("00000100", 2); // RS = 0, RW = 0, EN = 1 // 0000 0100 (assert the E strobe)
		writeWord(buf);
		delay(DELAY_COMMAND_SHORT);
		buf &= Integer.parseInt("11111011", 2); // Make EN = 0 // 1111 1011 (terminate the E strobe)
		writeWord(buf);
		if (LOG_ENABLED) {
			System.out.println("\t</command>");
		}
	}

	private static synchronized void sendData(final int data) {
		if (LOG_ENABLED) {
			System.out.println("\t<data value=\"" + String.format("%8s", Integer.toBinaryString(data)).replace(' ', '0') + "\" hex=\"0x" + String.format("%02X", data) + "\" char=\"" + String.format("%c", (char) data) + "\">");
		}
		int buf;
		// Send bit7-4 firstly
		buf = data & Integer.parseInt("11110000", 2); // 1111 0000 (get the high nibble)
		buf |= Integer.parseInt("00000101", 2); // RS = 1, RW = 0, EN = 1 // 0000 0101 (assert the E strobe)
		writeWord(buf);
		delay(DELAY_DATA);
		buf &= Integer.parseInt("11111011", 2); // Make EN = 0 // 1111 1011 (terminate the E strobe)
		writeWord(buf);

		// Send bit3-0 secondly
		buf = (data & Integer.parseInt("00001111", 2)) << 4; // 0000 1111 (get the low nibble and shift left)
		buf |= Integer.parseInt("00000101", 2); // RS = 1, RW = 0, EN = 1 // 0000 0101 (assert the E strobe)
		writeWord(buf);
		delay(DELAY_DATA);
		buf &= Integer.parseInt("11111011", 2); // Make EN = 0 // 1111 1011 (terminate the E strobe)
		writeWord(buf);
		if (LOG_ENABLED) {
			System.out.println("\t</data>");
		}
	}

	private static void init() {
		sendCommand(Integer.parseInt("00110011", 2)); // Must initialize to 8-line mode at first // 0011 0011
		delay(DELAY_COMMAND_SHORT);
		sendCommand(Integer.parseInt("00110010", 2)); // Then initialize to 4-line mode // 0011 0010
		delay(DELAY_COMMAND_SHORT);
		sendCommand(Integer.parseInt("00101000", 2)); // 2 Lines & 5*7 dots // 0010 1000
		delay(DELAY_COMMAND_SHORT);
		sendCommand(Integer.parseInt("00001100", 2)); // Enable display without cursor // 0000 1100
		delay(DELAY_COMMAND_SHORT);
		clear(); // Clear Screen
		I2C.wiringPiI2CWrite(fd, Integer.parseInt("00001000", 2)); // 0000 1000
	}

	private static void clear() {
		sendCommand(Integer.parseInt("00000001", 2)); //clear Screen
		delay(DELAY_COMMAND_LONG);
	}

	private static void home() {
		sendCommand(Integer.parseInt("00000010", 2)); //clear Screen
		delay(DELAY_COMMAND_LONG);
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
		if (LOG_ENABLED) {
			System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			System.out.println("<!DOCTYPE clock>");
			System.out.println("<clock date=\"" + new Date() + "\">");
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				clear();
				System.out.println("</clock>");
			}
		});

		fd = I2C.wiringPiI2CSetup(LCD_ADDRESS);
		init();

		String oldDateStr = "";
		String oldTimeStr = "";
		while (true) {
			boolean logMatrix = false;
			final Date date = new Date();
			final String dateStr = dfDate.format(date);
			final String timeStr = dfTime.format(date).substring(0, 10);
			if (!dateStr.equals(oldDateStr)) {
				String toPrint = dateStr;
				if (oldDateStr.length() > dateStr.length()) {
					for (int i = dateStr.length(); i < oldDateStr.length(); i++) {
						toPrint += ' ';
					}
				}
				write(0, 0, toPrint);
				oldDateStr = dateStr;
				logMatrix = true;
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
				logMatrix = true;
			}
			if (LOG_ENABLED && logMatrix) {
				System.out.println("\t<matrix>");
				System.out.println("\t\t" + Arrays.toString(matrix[0]).replace("\0", " ").replace(", ", "").replace(' ', '~'));
				System.out.println("\t\t" + Arrays.toString(matrix[1]).replace("\0", " ").replace(", ", "").replace(' ', '~'));
				System.out.println("\t</matrix>");
			}
			delay(25);
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
