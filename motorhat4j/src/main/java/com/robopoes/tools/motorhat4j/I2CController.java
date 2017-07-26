package com.robopoes.tools.motorhat4j;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class I2CController implements Closeable {

	private int address;
	private I2CBus bus;
	private I2CDevice device;

	public I2CController(int address) throws UnsupportedBusNumberException, IOException{
		this(address, getPiRevision() > 1 ? I2CBus.BUS_1: I2CBus.BUS_0);
	}

	public I2CController (int address, int busNum) throws UnsupportedBusNumberException, IOException{
		this.address = address;
		this.bus = I2CFactory.getInstance(busNum);
		this.device = this.bus.getDevice(address);
	}

	private static int getPiRevision(){
		try {
			File f = new File("/proc/cpuinfo");
			List<String> lines = Files.readAllLines(f.toPath());
			for (String line : lines) {
				if(line.startsWith("Revision")){
					String rev = line.split(":")[1].trim(); 
					return rev.equals("0000") || rev.equals("0002") || rev.equals("0003") ? 1 : 2;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public void writeByteToRegister(int reg, byte value) throws IOException{
		//"Writes an 8-bit value to the specified register/address"
		device.write(reg, value);
	}

	public void writeByteToBus(byte value) throws IOException{
		//"Writes an 8-bit value to the specified register/address"
		device.write(value);
	}

	public int readUnsignedByte(int reg) throws IOException{
		//"Read an unsigned byte from the I2C device"
		return device.read(reg);
	}

	public int getAddress() {
		return address;
	}

	@Override
	public void close() throws IOException {
		bus.close();
	}

}
