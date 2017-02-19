package com.robopoes.tools.motorhat4j;

public enum MotorDirection {
	FORWARD(1),
	BACKWARD(2),
	BRAKE(3),
	RELEASE(4);
	
	private int value;

	MotorDirection(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}