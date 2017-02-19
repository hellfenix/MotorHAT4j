package com.robopoes.tools.motorhat4j.motor;


public class DCMotor {

	private int motorNum;
	private int PWMpin;
	private int IN1pin;
	private int IN2pin;

	public DCMotor(int num){
		this.motorNum = num;

		int pwm, in1, in2;
		pwm = in1 = in2 = 0;

		if (num == 0){
			pwm = 8;
			in2 = 9;
			in1 = 10;
		}
		else if (num == 1){
			pwm = 13;
			in2 = 12;
			in1 = 11;
		}
		else if (num == 2){
			pwm = 2;
			in2 = 3;
			in1 = 4;
		}
		else if (num == 3){
			pwm = 7;
			in2 = 6;
			in1 = 5;
		} else {
			throw new RuntimeException("MotorHAT Motor must be between 0 and 3 inclusive");
		}

		this.PWMpin = pwm;
		this.IN1pin = in1;
		this.IN2pin = in2;
	}
	
	public int getMotorNum(){
		return motorNum;
	}
	
	public int getPWMPin(){
		return PWMpin;
	}
	
	public int getIN1pin(){
		return IN1pin;
	}
	
	public int getIN2pin(){
		return IN2pin;
	}
}
