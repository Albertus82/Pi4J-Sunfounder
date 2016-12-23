package it.albertus.pi4j.sunfounder.sensorkit._30_i2c_lcd1602;

import com.pi4j.wiringpi.I2C;

public class I2cLcd1602 {

	private static final int LCDAddr = 0x27;
	private static final int BLEN = 1;
	private static int fd;

	private static void write_word(int data) {
		int temp = data;
		if (BLEN == 1)
			temp |= 0x08;
		else
			temp &= 0xF7;
		I2C.wiringPiI2CWrite(fd, temp);
	}

	private static void send_command(final int comm) {
		int buf;
		// Send bit7-4 firstly
		buf = comm & 0xF0;
		buf |= 0x04; // RS = 0, RW = 0, EN = 1
		write_word(buf);
		delay(2);
		buf &= 0xFB; // Make EN = 0
		write_word(buf);

		// Send bit3-0 secondly
		buf = (comm & 0x0F) << 4;
		buf |= 0x04; // RS = 0, RW = 0, EN = 1
		write_word(buf);
		delay(2);
		buf &= 0xFB; // Make EN = 0
		write_word(buf);
	}

	private static void send_data(final int data) {
		int buf;
		// Send bit7-4 firstly
		buf = data & 0xF0;
		buf |= 0x05; // RS = 1, RW = 0, EN = 1
		write_word(buf);
		delay(2);
		buf &= 0xFB; // Make EN = 0
		write_word(buf);

		// Send bit3-0 secondly
		buf = (data & 0x0F) << 4;
		buf |= 0x05; // RS = 1, RW = 0, EN = 1
		write_word(buf);
		delay(2);
		buf &= 0xFB; // Make EN = 0
		write_word(buf);
	}

	private static void init() {
		send_command(0x33); // Must initialize to 8-line mode at first
		delay(5);
		send_command(0x32); // Then initialize to 4-line mode
		delay(5);
		send_command(0x28); // 2 Lines & 5*7 dots
		delay(5);
		send_command(0x0C); // Enable display without cursor
		delay(5);
		send_command(0x01); // Clear Screen
		I2C.wiringPiI2CWrite(fd, 0x08);
	}

	private static void clear() {
		send_command(0x01); //clear Screen
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
		send_command(addr);

		tmp = data.length();
		for (i = 0; i < tmp; i++) {
			send_data(data.charAt(i));
		}
	}

	public static void main(final String... args) {
		fd = I2C.wiringPiI2CSetup(LCDAddr);
		init();
		write(0, 0, "Greetings!");
		write(1, 1, "From SunFounder");
		delay(2000);
		clear();
	}

	private static void delay(final int i) {
		try {
			Thread.sleep(i);
		}
		catch (final InterruptedException ie) {
			throw new RuntimeException(ie);
		}
	}

}
