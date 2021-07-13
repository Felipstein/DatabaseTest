package br.lois.databasetest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class Register {
	
	private int id;
	private UUID uuid;
	private String name;
	private double coins;
	private String tag;
	private char msgColor;
	private String address;
	
	public Register(UUID uuid, String name, double coins, String tag, char msgColor, String address) {
		this(-1, uuid, name, coins, tag, msgColor, address);
	}
	
	public Register(int id, UUID uuid, String name, double coins, String tag, char msgColor, String address) {
		this.id = id;
		this.uuid = uuid;
		this.name = name;
		this.coins = coins;
		this.tag = tag;
		this.msgColor = msgColor;
		this.address = address;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean hasId() {
		return id != -1;
	}
	
	public UUID getUuid() {
		return uuid;
	}
	
	public String getName() {
		return name;
	}
	
	public double getCoins() {
		return coins;
	}
	
	public String getTag() {
		return tag;
	}
	
	public char getMsgColor() {
		return msgColor;
	}
	
	public String getAddress() {
		return address;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder()
		.append(id == -1 ? "default," : id + ",")
		.append("'" + uuid.toString() + "','")
		.append(name + "','")
		.append(coins + "',")
		.append((tag == null ? "null" : "'" + tag + "'") + ",'")
		.append(String.valueOf(msgColor) + "','")
		.append(address + "'");
		return sb.toString();
	}
	
	public static Register getRegister(ResultSet result) throws SQLException {
		int id = result.getInt(1);
		UUID uuid = UUID.fromString(result.getString(2));
		String name = result.getString(3);
		double coins = result.getDouble(4);
		String tag = result.getString(5);
		char msgColor = result.getString(6) == null ? 'f' : result.getString(6).charAt(0);
		String address = result.getString(7);
		return new Register(id, uuid, name, coins, tag, msgColor, address);
	}

	public static Register generateRandomRegister() {
		Random r = new Random();
		char[] chars = "0123456789_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		String name = "";
		for(int i = 0; i < r.nextInt(12) + 4; ++i) {
			name += chars[r.nextInt(chars.length)];
		}
		return new Register(UUID.randomUUID(), name, r.nextInt(10000000), null, Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f', 'f').get(r.nextInt(24)), "127.0.0.1");
	}
	
}