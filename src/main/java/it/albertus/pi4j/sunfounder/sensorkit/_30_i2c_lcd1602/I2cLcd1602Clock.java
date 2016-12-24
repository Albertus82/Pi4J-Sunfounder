package it.albertus.pi4j.sunfounder.sensorkit._30_i2c_lcd1602;

import java.text.DateFormat;
import java.util.Date;

import com.pi4j.wiringpi.I2C;

public class I2cLcd1602Clock {

	private static final int LCD_ADDR = 0x27;
	private static final int BLEN = 1;

	private static final long DELAY_COMMAND = 1;
	private static final long DELAY_DATA = 1;

	private static int fd;

	private static final DateFormat dfDate = DateFormat.getDateInstance();
	private static final DateFormat dfTime = DateFormat.getTimeInstance();

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
		System.out.println("<command value=\"" + String.format("%8s", Integer.toBinaryString(comm)).replace(' ', '0') + "\" hex=\"0x" + String.format("%X", comm) + "\">");
		int buf;
		// Send bit7-4 firstly
		buf = comm & 0xF0; // 1111 0000
		buf |= 0x04; // RS = 0, RW = 0, EN = 1 // 0000 0100
		writeWord(buf);
		delay(DELAY_COMMAND);
		buf &= 0xFB; // Make EN = 0 // 1111 1011
		writeWord(buf);

		// Send bit3-0 secondly
		buf = (comm & 0x0F) << 4; // 0000 1111
		buf |= 0x04; // RS = 0, RW = 0, EN = 1 // 0000 0100
		writeWord(buf);
		delay(DELAY_COMMAND);
		buf &= 0xFB; // Make EN = 0 // 1111 1011
		writeWord(buf);
		System.out.println("</command>");
	}

	private static synchronized void sendData(final int data) {
		System.out.println("<data value=\"" + String.format("%8s", Integer.toBinaryString(data)).replace(' ', '0') + "\" hex=\"0x" + String.format("%X", data) + "\" char=\"" + String.format("%c", (char) data) + "\">");
		int buf;
		// Send bit7-4 firstly
		buf = data & 0xF0; // 1111 0000
		buf |= 0x05; // RS = 1, RW = 0, EN = 1 // 0000 0101
		writeWord(buf);
		delay(DELAY_DATA);
		buf &= 0xFB; // Make EN = 0 // 1111 1011
		writeWord(buf);

		// Send bit3-0 secondly
		buf = (data & 0x0F) << 4; // 0000 1111
		buf |= 0x05; // RS = 1, RW = 0, EN = 1 // 0000 0101
		writeWord(buf);
		delay(DELAY_DATA);
		buf &= 0xFB; // Make EN = 0 // 1111 1011
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
	}

	private static void write(int x, int y, String data) {
		int addr, i;
		int tmp;
		if (x < 0)
			x = 0;
		if (x > 15)
			x = 15;
		if (y < 0)
			y = 0;
		if (y > 1)
			y = 1;

		// Move cursor
		addr = 0x80 + 0x40 * y + x;
		sendCommand(addr);

		tmp = data.length();
		for (i = 0; i < tmp; i++) {
			sendData(data.charAt(i));
		}
	}

	public static void main(final String... args) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Clear");
				delay(100);
				clear();
			}
		});

		fd = I2C.wiringPiI2CSetup(LCD_ADDR);
		init();

		String oldDateStr = "";
		String oldTimeStr = "";
		while (true) {
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
				System.out.println(dateStr);
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
				System.out.println(timeStr);
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
