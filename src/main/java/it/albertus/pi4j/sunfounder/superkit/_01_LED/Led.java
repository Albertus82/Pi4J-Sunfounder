package it.albertus.pi4j.sunfounder.superkit._01_LED;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class Led {

	private static final Pin ledPin = RaspiPin.GPIO_00;

	public static void main(final String... args) throws InterruptedException {
		final GpioController gpio = GpioFactory.getInstance();

		System.out.printf("linker LedPin : GPIO %d(wiringPi pin)\n", ledPin.getAddress());

		final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(ledPin, PinState.LOW);

		while (true) {
			pin.low();
			System.out.println("led on...");
			Thread.sleep(500);
			pin.high();
			System.out.println("...led off");
			Thread.sleep(500);
		}
	}

}
