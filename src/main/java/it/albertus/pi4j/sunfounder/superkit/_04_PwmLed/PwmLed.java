package it.albertus.pi4j.sunfounder.superkit._04_PwmLed;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.RaspiPin;

public class PwmLed {

	public static void main(final String... args) throws InterruptedException {
		final GpioController gpio = GpioFactory.getInstance();

		final GpioPinPwmOutput ledPin = gpio.provisionPwmOutputPin(RaspiPin.GPIO_01); //pwm output mode

		while (true) {
			for (int i = 0; i < 1024; i++) {
				ledPin.setPwm(i);
				Thread.sleep(2);
			}
			Thread.sleep(1000);
			for (int i = 1023; i >= 0; i--) {
				ledPin.setPwm(i);
				Thread.sleep(2);
			}
		}
	}

}
