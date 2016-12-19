package it.albertus.pi4j.sunfounder.superkit._01_LED;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class Led {

	public static void main(final String... args) throws InterruptedException {
		final GpioController gpio = GpioFactory.getInstance();

		System.out.printf("linker LedPin : GPIO %d(wiringPi pin)\n", RaspiPin.GPIO_00.getAddress()); //when initialize wiring successfully,print message to screen

		final GpioPinDigitalOutput ledPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00);

		ledPin.setShutdownOptions(true, PinState.LOW, PinPullResistance.PULL_UP); // IN 0

		while (true) {
			ledPin.low(); //led on
			System.out.println("led on...");
			Thread.sleep(500);
			ledPin.high(); //led off
			System.out.println("...led off");
			Thread.sleep(500);
		}
	}

}
