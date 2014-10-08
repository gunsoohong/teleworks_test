package com.teleworks.coffee_machine_nfctag;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UtilHex {

	private static final char hexchar[] = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static byte toByte(char c) {
		switch (c) {
		case '0':
			return 0;
		case '1':
			return 1;
		case '2':
			return 2;
		case '3':
			return 3;
		case '4':
			return 4;
		case '5':
			return 5;
		case '6':
			return 6;
		case '7':
			return 7;
		case '8':
			return 8;
		case '9':
			return 9;
		case 'A':
			return 10;
		case 'B':
			return 11;
		case 'C':
			return 12;
		case 'D':
			return 13;
		case 'E':
			return 14;
		case 'F':
			return 15;
		}
		return 0;
	}

	/**
	 * byte[]을 Hex String로 변환
	 * 
	 * @param byte[]
	 * @return hexStr
	 */
	public static String byteArrayToHex(byte[] ba) {
		if (ba == null || ba.length == 0) {
			return null;
		}

		StringBuffer sb = new StringBuffer(ba.length * 2);
		String hexNumber;
		for (int x = 0; x < ba.length; x++) {
			hexNumber = "0" + Integer.toHexString(0xff & ba[x]);

			sb.append(hexNumber.substring(hexNumber.length() - 2));
		}
		return sb.toString();
	}

	// gunsoo
	public static String hexToString(byte[] bytebuf) {
		String str = "";
		for (int i = 0; i < bytebuf.length; i++) {
			str += String.format("%02X", bytebuf[i]);
		}
		return str;
	}

	public static int Byte2ToInt(byte Byte_H, byte Byte_L) {
		byte[] byteV = { 0, 0, Byte_H, Byte_L };
		ByteBuffer buff = ByteBuffer.allocate(4);
		buff = ByteBuffer.wrap(byteV);
		buff.order(ByteOrder.BIG_ENDIAN);
		return buff.getInt();
	}

	/**
	 * Hex String을 byte로 변환
	 * 
	 * @param hexStr
	 * @return byte
	 */
	public static byte toByte(String hexStr) {
		byte result = 0;
		String hex = hexStr.toUpperCase();

		for (int i = 0; i < hex.length(); i++) {
			char c = hex.charAt(hex.length() - i - 1);
			byte b = toByte(c);
			result |= (b & 0x0f) << (i * 4);
		}

		return result;
	}

	/**
	 * Hex String을 byte[]로 변환
	 * 
	 * @param hexStr
	 * @return byte[]
	 */
	public static byte[] toByteArray(String hexStr) {
		String hex = hexStr.toUpperCase();
		byte[] result = new byte[hex.length() / 2];

		for (int i = 0; i < hex.length(); i = i + 2) {
			String str = hex.substring(i, i + 2);
			byte b = toByte(str);
			result[i / 2] = b;
		}

		return result;
	}

	public static String toHexString(byte b) {
		StringBuffer buffer = new StringBuffer();

		buffer.append(hexchar[(b >>> 4) & 0xf]);
		buffer.append(hexchar[b & 0xf]);

		return buffer.toString();
	}

	/**
	 * int 데이터를 Hex String을 변환
	 * 
	 * @param i
	 *            Hex String으로 변환할 int 데이터
	 * @return HexString
	 */
	public static String toHexString(int i) {
		StringBuffer buffer = new StringBuffer();

		buffer.append(hexchar[(i >>> 28) & 0xf]);
		buffer.append(hexchar[(i >>> 24) & 0xf]);
		buffer.append(hexchar[(i >>> 20) & 0xf]);
		buffer.append(hexchar[(i >>> 16) & 0xf]);
		buffer.append(hexchar[(i >>> 12) & 0xf]);
		buffer.append(hexchar[(i >>> 8) & 0xf]);
		buffer.append(hexchar[(i >>> 4) & 0xf]);
		buffer.append(hexchar[i & 0xf]);

		return buffer.toString();
	}

	public static String byteArrayToHexRange(byte[] ba, int from, int len) {
		if (ba == null || ba.length == 0) {
			return null;
		}

		if (ba.length < (from + len))
			return null;

		int initInd = from;
		StringBuffer sb = new StringBuffer(len * 2);
		String hexNumber;
		for (int x = 0; x < len; x++, initInd++) {
			hexNumber = "0" + Integer.toHexString(0xff & ba[initInd]);

			sb.append(hexNumber.substring(hexNumber.length() - 2));
		}
		return sb.toString();
	}

	// crc check-16을 반환하는 byte배열
	public byte[] addCRC(byte[] bb) {
		byte[] barray = new byte[2];
		int Carry;
		int CRC = 0xFFFF;
		for (int i = 0; i < bb.length; i++) {
			int temp = bb[i];
			if (temp < 0)
				temp = ((temp * (-1)) ^ 0xff) + 1;
			CRC = CRC ^ temp;

			for (int j = 0; j < 8; j++) {
				Carry = CRC & 0x0001;
				CRC = (CRC >> 1);
				if (Carry == 1)
					CRC = CRC ^ 0xA001;
			}
		}
		barray[0] = (byte) (CRC & 0x00FF);
		barray[1] = (byte) ((CRC & 0xFF00) >> 8);
		return barray;
	}

	/* CRC polynomial 0xA001 */
	public static int crc16Tab[] = { 0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301,
			0x03C0, 0x0280, 0xC241, 0xC601, 0x06C0, 0x0780, 0xC741, 0x0500,
			0xC5C1, 0xC481, 0x0440, 0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00,
			0xCFC1, 0xCE81, 0x0E40, 0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901,
			0x09C0, 0x0880, 0xC841, 0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00,
			0xDBC1, 0xDA81, 0x1A40, 0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01,
			0x1DC0, 0x1C80, 0xDC41, 0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701,
			0x17C0, 0x1680, 0xD641, 0xD201, 0x12C0, 0x1380, 0xD341, 0x1100,
			0xD1C1, 0xD081, 0x1040, 0xF001, 0x30C0, 0x3180, 0xF141, 0x3300,
			0xF3C1, 0xF281, 0x3240, 0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501,
			0x35C0, 0x3480, 0xF441, 0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01,
			0x3FC0, 0x3E80, 0xFE41, 0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900,
			0xF9C1, 0xF881, 0x3840, 0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01,
			0x2BC0, 0x2A80, 0xEA41, 0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00,
			0xEDC1, 0xEC81, 0x2C40, 0xE401, 0x24C0, 0x2580, 0xE541, 0x2700,
			0xE7C1, 0xE681, 0x2640, 0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101,
			0x21C0, 0x2080, 0xE041, 0xA001, 0x60C0, 0x6180, 0xA141, 0x6300,
			0xA3C1, 0xA281, 0x6240, 0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501,
			0x65C0, 0x6480, 0xA441, 0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01,
			0x6FC0, 0x6E80, 0xAE41, 0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900,
			0xA9C1, 0xA881, 0x6840, 0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01,
			0x7BC0, 0x7A80, 0xBA41, 0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00,
			0xBDC1, 0xBC81, 0x7C40, 0xB401, 0x74C0, 0x7580, 0xB541, 0x7700,
			0xB7C1, 0xB681, 0x7640, 0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101,
			0x71C0, 0x7080, 0xB041, 0x5000, 0x90C1, 0x9181, 0x5140, 0x9301,
			0x53C0, 0x5280, 0x9241, 0x9601, 0x56C0, 0x5780, 0x9741, 0x5500,
			0x95C1, 0x9481, 0x5440, 0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00,
			0x9FC1, 0x9E81, 0x5E40, 0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901,
			0x59C0, 0x5880, 0x9841, 0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00,
			0x8BC1, 0x8A81, 0x4A40, 0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01,
			0x4DC0, 0x4C80, 0x8C41, 0x4400, 0x84C1, 0x8581, 0x4540, 0x8701,
			0x47C0, 0x4680, 0x8641, 0x8201, 0x42C0, 0x4380, 0x8341, 0x4100,
			0x81C1, 0x8081, 0x4040, };

	public static int crc16_Bigendian(byte[] bufIn, int charCount) {
		int crc16 = crc16Check(bufIn, charCount);

		return ((crc16 << 8) & 0xFF00) | ((crc16 >> 8) & 0x00FF);
	}

	public static int crc16(byte[] bufIn, int charCount) {
		return crc16Check(bufIn, charCount);
	}

	public static int crc16Check(byte[] bufIn, int charCount) {
		int crc16;

		int i = 0;

		for (crc16 = 0; charCount > 0; charCount--) {
			crc16 = crc16Tab[(crc16 ^ bufIn[i++]) & 0xff] ^ (crc16 >> 0x0008);
		}

		return crc16;
	}
}
