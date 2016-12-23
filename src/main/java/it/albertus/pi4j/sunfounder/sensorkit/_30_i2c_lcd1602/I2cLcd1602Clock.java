package it.albertus.pi4j.sunfounder.sensorkit._30_i2c_lcd1602;

import java.text.DateFormat;
import java.util.Date;

import com.pi4j.wiringpi.I2C;

public class I2cLcd1602Clock {

	private static final int LCD_ADDR = 0x27;
	private static final int BLEN = 1;
	private static int fd;

	private static final DateFormat dfDate = DateFormat.getDateInstance();
	private static final DateFormat dfTime = DateFormat.getTimeInstance();

	private static void writeWord(final int data) {
		int temp = data;
		if (BLEN == 1)
			temp |= 0x08;
		else
			temp &= 0xF7;
		I2C.wiringPiI2CWrite(fd, temp);
	}

	private static void sendCommand(final int comm) {
		int buf;
		// Send bit7-4 firstly
		buf = comm & 0xF0;
		buf |= 0x04; // RS = 0, RW = 0, EN = 1
		writeWord(buf);
		delay(2);
		buf &= 0xFB; // Make EN = 0
		writeWord(buf);

		// Send bit3-0 secondly
		buf = (comm & 0x0F) << 4;
		buf |= 0x04; // RS = 0, RW = 0, EN = 1
		writeWord(buf);
		delay(2);
		buf &= 0xFB; // Make EN = 0
		writeWord(buf);
	}

	private static void sendData(final int data) {
		int buf;
		// Send bit7-4 firstly
		buf = data & 0xF0;
		buf |= 0x05; // RS = 1, RW = 0, EN = 1
		writeWord(buf);
		delay(2);
		buf &= 0xFB; // Make EN = 0
		writeWord(buf);

		// Send bit3-0 secondly
		buf = (data & 0x0F) << 4;
		buf |= 0x05; // RS = 1, RW = 0, EN = 1
		writeWord(buf);
		delay(2);
		buf &= 0xFB; // Make EN = 0
		writeWord(buf);
	}

	private static void init() {
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

		String dateStr2 = null;
		String timeStr2 = null;
		while (true) {
			final Date d = new Date();
			final String dateStr = dfDate.format(d);
			final String timeStr = dfTime.format(d);
			if (!dateStr.equals(dateStr2) || !timeStr.equals(timeStr2)) {
				write(0, 0, dateStr);
				write(0, 1, timeStr);
				System.out.println(dateStr);
				System.out.println(timeStr);
				dateStr2 = dateStr;
				timeStr2 = timeStr;
			}
			delay(50);
		}
	}

	private static void delay(final long millis) {
		try {
			Thread.sleep(millis);
		}
		catch (final InterruptedException ie) {
			throw new RuntimeException(ie);
		}
	}

}
