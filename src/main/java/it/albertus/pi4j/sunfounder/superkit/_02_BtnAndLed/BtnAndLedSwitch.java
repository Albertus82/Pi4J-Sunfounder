package it.albertus.pi4j.sunfounder.superkit._02_BtnAndLed;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class BtnAndLedSwitch {

	public static void main(final String... args) throws InterruptedException {
		final GpioController gpio = GpioFactory.getInstance();

		final GpioPinDigitalOutput ledPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, PinState.HIGH);
		final GpioPinDigitalInput buttonPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_01);

		while (true) {
			if (buttonPin.isLow()) { //indicate that button has pressed down
				ledPin.setState(PinState.getInverseState(ledPin.getState()));
				while (buttonPin.isLow()) {
					Thread.sleep(50);
				}
			}
			Thread.sleep(50);
		}
	}

}
