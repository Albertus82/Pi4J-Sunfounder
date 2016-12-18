package it.albertus.pi4j.sunfounder.superkit._05_RGB;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.RaspiPin;

public class Rgb {

	private static GpioController gpio;
	private static GpioPinPwmOutput ledPinRed;
	private static GpioPinPwmOutput ledPinGreen;
	private static GpioPinPwmOutput ledPinBlue;

	private static void ledInit() {
		ledPinRed = gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_00, 0);
		ledPinGreen = gpio.provisionPwmOutputPin(RaspiPin.GPIO_01, 0);
		ledPinBlue = gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_02, 0);
	}

	private static void ledColorSet(final int rVal, final int gVal, final int bVal) {
		ledPinRed.setPwm(rVal);
		ledPinGreen.setPwm(gVal);
		ledPinBlue.setPwm(bVal);
	}

	public static void main(final String... args) throws InterruptedException {
		gpio = GpioFactory.getInstance();

		ledInit();

		while (true) {
			ledColorSet(0xff, 0x00, 0x00); //red	
			Thread.sleep(500);
			ledColorSet(0x00, 0xff, 0x00); //green
			Thread.sleep(500);
			ledColorSet(0x00, 0x00, 0xff); //blue
			Thread.sleep(500);

			ledColorSet(0xff, 0xff, 0x00); //yellow
			Thread.sleep(500);
			ledColorSet(0xff, 0x00, 0xff); //pick
			Thread.sleep(500);
			ledColorSet(0xc0, 0xff, 0x3e);
			Thread.sleep(500);

			ledColorSet(0x94, 0x00, 0xd3);
			Thread.sleep(500);
			ledColorSet(0x76, 0xee, 0x00);
			Thread.sleep(500);
			ledColorSet(0x00, 0xc5, 0xcd);
			Thread.sleep(500);
		}
	}

}
