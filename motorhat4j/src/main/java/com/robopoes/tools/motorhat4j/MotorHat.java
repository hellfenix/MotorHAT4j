package com.robopoes.tools.motorhat4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.robopoes.tools.motorhat4j.motor.DCMotor;

public class MotorHat implements Closeable {

	// Registers/etc.
	public static final int MODE1 = 0x00;
	public static final int MODE2 = 0x01;
	public static final int SUBADR1 = 0x02;
	public static final int SUBADR2 = 0x03;
	public static final int SUBADR3 = 0x04;
	public static final int PRESCALE = 0xFE;
	public static final int LED0_ON_L = 0x06;
	public static final int LED0_ON_H = 0x07;
	public static final int LED0_OFF_L = 0x08;
	public static final int LED0_OFF_H = 0x09;
	public static final int ALL_LED_ON_L = 0xFA;
	public static final int ALL_LED_ON_H = 0xFB;
	public static final int ALL_LED_OFF_L = 0xFC;
	public static final int ALL_LED_OFF_H = 0xFD;

	// Bits
	public static final int RESTART = 0x80;
	public static final int SLEEP = 0x10;
	public static final int ALLCALL = 0x01;
	public static final int INVRT = 0x10;
	public static final int OUTDRV = 0x04;

	private List<DCMotor> dcMotors;
	private I2CController controller;
	private int address;
	private int freq;

	public MotorHat() throws UnsupportedBusNumberException, IOException{
		this(0x60, 1600);
	}

	public MotorHat(int address, int freq) throws UnsupportedBusNumberException, IOException{
		this.address = address;
		this.freq = freq;

		dcMotors = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			dcMotors.add(new DCMotor(i));
		}

		controller = new I2CController(address);

		setPWMFreq(freq);
	}

	public void setMotorDirection(int motor, MotorDirection dir) throws IOException{
		switch(dir){
		case FORWARD:
			setMotorPin(dcMotors.get(motor).getIN1pin(), 1);
			setMotorPin(dcMotors.get(motor).getIN2pin(), 0);
			break;
		case BACKWARD:
			setMotorPin(dcMotors.get(motor).getIN1pin(), 0);
			setMotorPin(dcMotors.get(motor).getIN2pin(), 1);
			break;
		case RELEASE:
			setMotorPin(dcMotors.get(motor).getIN1pin(), 0);
			setMotorPin(dcMotors.get(motor).getIN2pin(), 0);
			break;
		case BRAKE:
			break;
		default:
			break;
		}
	}

	public void setMotorPin(int pin, int value) throws IOException{
		if (pin < 0 || pin > 15){
			System.out.println("PWM pin must be between 0 and 15 inclusive");
		}
		if (value != 0 && value != 1){
			System.out.println("Pin value must be 0 or 1!");
		}
		if (value == 0){
			setPWM(pin, 0, 4096);
		}
		if (value == 1){
			setPWM(pin, 4096, 0);
		}
	}

	public void setMotorSpeed(int motor, int speed) throws IOException{
		if (speed < 0){
			speed = 0;
		}

		if (speed > 255){
			speed = 255;
		}

		setPWM(dcMotors.get(motor).getPWMPin(), 0, speed*16);
	}

	public void setPWMFreq(int freq) throws IOException{
		// "Sets the PWM frequency"
		double prescaleval = 25000000.0;    // 25MHz
		prescaleval /= 4096.0;       // 12-bit
		prescaleval /= (float) freq;
		prescaleval -= 1.0;

		double prescale = Math.floor(prescaleval + 0.5);

		int oldmode = controller.readUnsignedByte(MODE1);
		int newmode = (oldmode & 0x7F) | 0x10;             // sleep
		controller.writeByteToRegister(MODE1, (byte) newmode);         // go to sleep
		controller.writeByteToRegister(PRESCALE, (byte) (Math.floor(prescale)));
		controller.writeByteToRegister(MODE1, (byte) oldmode);

		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		controller.writeByteToRegister(MODE1, (byte) (oldmode | 0x80));
	}

	private void setPWM(int channel, int on, int off) throws IOException{
		// "Sets a single PWM channel"
		controller.writeByteToRegister(LED0_ON_L + 4*channel, (byte) (on & 0xFF));
		controller.writeByteToRegister(LED0_ON_H + 4*channel, (byte) (on >> 8));
		controller.writeByteToRegister(LED0_OFF_L + 4*channel, (byte) (off & 0xFF));
		controller.writeByteToRegister(LED0_OFF_H + 4*channel, (byte) (off >> 8));
	}

	public void setAllPWM(int on, int off) throws IOException{
		// "Sets a all PWM channels"
		controller.writeByteToRegister(ALL_LED_ON_L, (byte) (on & 0xFF));
		controller.writeByteToRegister(ALL_LED_ON_H, (byte) (on >> 8));
		controller.writeByteToRegister(ALL_LED_OFF_L, (byte) (off & 0xFF));
		controller.writeByteToRegister(ALL_LED_OFF_H, (byte) (off >> 8));
	}

	public void stopAll() throws IOException {
		for(DCMotor m : dcMotors) {
			setMotorDirection(m.getMotorNum(), MotorDirection.RELEASE);
			setMotorSpeed(m.getMotorNum(), 0);
		}
	}
	
	public int getAddress(){
		return address;
	}

	public int getFreq(){
		return freq;
	}

	@Override
	public void close() throws IOException {
		controller.close();
	}
}
