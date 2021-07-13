package br.lois.databasetest;

import static java.lang.String.valueOf;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class Main extends JavaPlugin {
	
	private static Main instance;
	
	private Database database;
	
	@Override
	public void onEnable() {
		instance = this;
		if(!new File("config.yml").exists()) {
			this.saveDefaultConfig();
		}
		FileConfiguration f = getConfig();
		this.database = new Database(f.getString("host"), f.getInt("port"), f.getString("database"), f.getString("user"), f.getString("password"), f.getBoolean("use-ssl"));
	}
	
	@Override
	public void onDisable() {
		try {
			this.database.getConnection().close();
			info("Conexão finalizada.");
		} catch (SQLException e) {
			error(e);
		}
	}
	
	/*
	 * Alguns comandos não possuem verificação de tipos de argumentos, cuidado!
	 */
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if(!(sender instanceof Player)) {
			return false;
		}
		Player p = (Player) sender;
		if(args.length == 0) {
			p.sendMessage("§6» §a/db list §fLista todos os registros existentes.");
			p.sendMessage("§6» §a/db info §fExibe informações da tabela de registro.");
			p.sendMessage("§6» §a/db total §fExibe o total de registros no banco de dados.");
			p.sendMessage("§6» §a/db add §fAdiciona seu registro no banco de dados.");
			p.sendMessage("§6» §a/db delete §fRetira seu registro do banco de dados.");
			p.sendMessage("§6» §a/db me §fExibe informações do seu registro.");
			p.sendMessage("§6» §a/db setcoins <coins> §fAltera seu coins.");
			p.sendMessage("§6» §a/db settag <tag> §fAltera sua tag.");
			p.sendMessage("§6» §a/db setcor <cor> §fAltera sua cor.");
			p.sendMessage("§6» §a/db setip <ip> §fAltera seu ip.");
			p.sendMessage("§6» §a/db ranking [limite] §fLista o ranking money.");
			p.sendMessage("§6» §a/db random [total] §fGera um (ou vários) registro(s) aleatório(s).");
			p.sendMessage("§6» §e/db dmlsql <sql> §fExecuta uma SQL do tipo DML.");
			p.sendMessage("§6» §e/db ddlsql <sql> §fExecuta uma SQL do tipo DDL.");
			return true;
		}
		if(args[0].equals("list")) {
			ResultSet result = database.executeQuery("select * from registers");
			try {
				p.sendMessage("§2Exibindo todos os registros:");
				int total = 0;
				while(result.next()) {
					Register register = Register.getRegister(result);
					StringBuilder sb = new StringBuilder();
					sb.append("\nID: " + register.getId() + "\n");
					sb.append("UUID: " + register.getUuid().toString() + "\n");
					sb.append("Nome: " + register.getName() + "\n");
					sb.append("Coins: " + new DecimalFormat("###,###.##").format(register.getCoins()) + "\n");
					sb.append("TAG: " + (register.getTag() == null ? "Não possui" : register.getTag()) + "§f\n");
					sb.append("Cor: " + register.getMsgColor() + "\n");
					sb.append("IP: " + register.getAddress() + "\n");
					Component comp = Component.text("» ").color(NamedTextColor.GOLD)
					.append(Component.text(register.getName()).color(NamedTextColor.GRAY)
					.hoverEvent(HoverEvent.showText(Component.text(sb.toString()))));
					p.sendMessage(comp);
					++total;
				}
				p.sendMessage(total == 0 ? "§cNenhum registro encontrado." : " ");
			} catch (SQLException e) {
				error(e);
			}
		} else if(args[0].equals("info")) {
			ResultSet result = database.executeQuery("desc registers");
			try {
				p.sendMessage("§bNome  §7-  §6Tipo  §7-  §aNulo  §7-  §cChave  §7-  §fPadrão  §7-  §eExtra");
				int total = 0;
				while(result.next()) {
					p.sendMessage("§b" + result.getString(1) + "  §7-  §6" + result.getString(2) + "  §7-  §a" + result.getString(3) + "  §7-  §c" + result.getString(4) + "  §7-  §f" + result.getString(5) + "  §7-  §e" + result.getString(6));
					++total;
				}
				p.sendMessage(total == 0 ? "§cNenhuma coluna criada." : " ");
			} catch (SQLException e) {
				error(e);
			}
		} else if(args[0].equals("total")) {
			ResultSet result = database.executeQuery("select count(*) from registers");
			try {
				result.next();
				p.sendMessage("§aTotal de registros: §e" + result.getInt(1) + "§a.");
			} catch (SQLException e) {
				error(e);
			}
		} else if(args[0].equals("add")) {
			ResultSet result = database.executeQuery("select count(*) from registers where uuid = '" + p.getUniqueId().toString() + "'");
			try {
				result.next();
				if(result.getInt(1) > 0) {
					p.sendMessage("§cVocê já possui um registro no banco de dados.");
					return false;
				}
			} catch (SQLException e) {
				error(e);
			}
			this.database.executeUpdate("insert into registers values (default, '" + p.getUniqueId().toString() + "', '" + p.getName() + "', default, null, null, '" + p.getAddress().getAddress().getHostAddress() + "')");
			p.sendMessage("§aRegistro adicionado com êxito.");
		} else if(args[0].equals("delete")) {
			ResultSet result = database.executeQuery("select id, count(*) from registers where uuid = '" + p.getUniqueId().toString() + "'");
			int id;
			try {
				result.next();
				if(result.getInt(2) == 0) {
					p.sendMessage("§cVocê não possui um registro no banco de dados.");
					return false;
				}
				id = result.getInt(1);
			} catch (SQLException e) {
				error(e);
				return false;
			}
			this.database.executeUpdate("delete from registers where id = " + id);
			p.sendMessage("§aSeu registro foi deletado com êxito.");
		} else if(args[0].equals("me")) {
			ResultSet result = database.executeQuery("select *, count(*) from registers where uuid = '" + p.getUniqueId().toString() + "'");
			try {
				result.next();
				if(result.getInt(8) == 0) {
					p.sendMessage("§cVocê não possui um registro no banco de dados.");
					return false;
				}
				Register register = Register.getRegister(result);
				p.sendMessage("§2Exibindo seu registro:");
				p.sendMessage("§aID: §b" + register.getId());
				p.sendMessage("§aUUID: §f" + register.getUuid().toString());
				p.sendMessage("§aNome: §e" + register.getName());
				p.sendMessage("§aCoins: §e" + new DecimalFormat("###,###.##").format(register.getCoins()));
				p.sendMessage("§aTAG: §r" + (register.getTag() == null ? "§cNão possui" : register.getTag()));
				p.sendMessage("§aCor: §e&" + register.getMsgColor());
				p.sendMessage("§aIP: §e" + register.getAddress());
				p.sendMessage(" ");
			} catch(SQLException e) {
				error(e);
				return false;
			}		
		} else if(args[0].equals("setcoins")) {
			if(args.length == 1) {
				p.sendMessage("§cFalta informar quantos coins!");
				return false;
			}
			int id;
			ResultSet result = database.executeQuery("select id, count(*) from registers where uuid = '" + p.getUniqueId().toString() + "'");
			try {
				result.next();
				if(result.getInt(2) == 0) {
					p.sendMessage("§cVocê não possui um registro no banco de dados.");
					return false;
				}
				id = result.getInt(1);
			} catch (SQLException e) {
				error(e);
				return false;
			}
			double coins = Double.parseDouble(args[1]);
			this.database.executeUpdate("update registers set coins = " + coins + " where id = " + id + "");
			p.sendMessage("§aCoins alterado para §f" + new DecimalFormat("###,###.##").format(coins) + "§a.");
		} else if(args[0].equals("settag")) {
			if(args.length == 1) {
				p.sendMessage("§cFalta informar a tag!");
				return false;
			}
			int id;
			ResultSet result = database.executeQuery("select id, count(*) from registers where uuid = '" + p.getUniqueId().toString() + "'");
			try {
				result.next();
				if(result.getInt(2) == 0) {
					p.sendMessage("§cVocê não possui um registro no banco de dados.");
					return false;
				}
				id = result.getInt(1);
			} catch (SQLException e) {
				error(e);
				return false;
			}
			String tag;
			if(args[1].equals("null")) {
				tag = null;
			} else {
				tag = "'" + args[1].replace('&', '§') + "'";
			}
			this.database.executeUpdate("update registers set tag = " + tag + " where id = " + id + "");
			p.sendMessage("§aTAG alterada para \"§f" + tag + "§a\".");
		} else if(args[0].equals("setcor")) {
			if(args.length == 1) {
				p.sendMessage("§cFalta informar a cor!");
				return false;
			}
			int id;
			ResultSet result = database.executeQuery("select id, count(*) from registers where uuid = '" + p.getUniqueId().toString() + "'");
			try {
				result.next();
				if(result.getInt(2) == 0) {
					p.sendMessage("§cVocê não possui um registro no banco de dados.");
					return false;
				}
				id = result.getInt(1);
			} catch (SQLException e) {
				error(e);
				return false;
			}
			char colorMsg;
			if(!args[1].equals("null")) {
				colorMsg = args[1].charAt(0);
			} else {
				colorMsg = 'f';
			}
			this.database.executeUpdate("update registers set msgcolor = '" + colorMsg + "' where id = " + id + "");
			p.sendMessage("§aCor alterada para §f&" + colorMsg + "§a.");
		} else if(args[0].equals("setip")) {
			if(args.length == 1) {
				p.sendMessage("§cFalta informar o ip!");
				return false;
			}
			int id;
			ResultSet result = database.executeQuery("select id, count(*) from registers where uuid = '" + p.getUniqueId().toString() + "'");
			try {
				result.next();
				if(result.getInt(2) == 0) {
					p.sendMessage("§cVocê não possui um registro no banco de dados.");
					return false;
				}
				id = result.getInt(1);
			} catch (SQLException e) {
				error(e);
				return false;
			}
			String address = args[1];
			this.database.executeUpdate("update registers set address = '" + address + "' where id = " + id + "");
			p.sendMessage("§aIP alterado para §f" + address + "§a.");
		} else if(args[0].equals("ranking")) {
			int limit = 10;
			if(args.length > 1) {
				limit = Integer.parseInt(args[1]);
			}
			ResultSet result = database.executeQuery("select name, coins from registers order by coins desc limit " + limit);
			try {
				p.sendMessage("§2Exibindo §6" + limit + " §2jogadores do ranking money:");
				int index = 1;
				while(result.next()) {
					String name = result.getString(1);
					double coins = result.getDouble(2);
					String color = "§7";
					if(index == 1) {
						color = "§e";
					} else if(index == 2) {
						color = "§c";
					} else if(index == 3) {
						color = "§6";
					}
					p.sendMessage("§a» " + color + index + ". §8- §f" + name + " §8- §f$" + new DecimalFormat("###,###.##").format(coins));
					++index;
				}
				p.sendMessage(" ");
			} catch (SQLException e) {
				error(e);
			}
		} else if(args[0].equals("random")) {
			int total = 1;
			if(args.length > 1) {
				total = Integer.parseInt(args[1]);
			}
			p.sendMessage("§aGerando registros...");
			for(int i = 0; i < total; ++i) {
				this.database.executeUpdate("insert into registers values (" + Register.generateRandomRegister() + ")");
			}
			p.sendMessage("§aRegistros gerados com êxito.");
		} else if(args[0].equals("dmlsql")) {
			if(args.length == 1) {
				p.sendMessage("§cFalta informar o comando SQL DML.");
				return false;
			}
			String sql = "";
			for(int i = 1; i < args.length; ++i) {
				sql += " " + args[i];
			}
			try {
				this.database.createStatementUpdatable().executeUpdate(sql.trim());
			} catch (SQLException e) {
				p.sendMessage("§4Ocorreu um erro ao efetuar a transação SQL DML:");
				p.sendMessage("§c" + e.getClass().getName().replace(" class", "")+ ": " + e.getMessage());
				return false;
			}
			p.sendMessage("§aSíntaxe \"§f" + sql + "§a\" enviada com êxito.");
		} else if(args[0].equals("ddlsql")) {
			if(args.length == 1) {
				p.sendMessage("§cFalta informar o comando SQL DDL.");
				return false;
			}
			String sql = "";
			for(int i = 1; i < args.length; ++i) {
				sql += " " + args[i];
			}
			try {
				this.database.createStatement().execute(sql.trim());
			} catch (SQLException e) {
				p.sendMessage("§4Ocorreu um erro ao efetuar a transação SQL DDL:");
				p.sendMessage("§c" + e.getClass().getName().replace(" class", "") + ": " + e.getMessage());
				return false;
			}
			p.sendMessage("§aSíntaxe \"§f" + sql + "§a\" enviada com êxito.");
		} else {
			p.sendMessage("§cArgumento \"" + args[0] + "\" inexistente.");
			return false;
		}
		return true;
	}
	
	public Database getDatabase() {
		return database;
	}
	
	public static void error(Throwable e) {
		Main.log(Level.SEVERE, "Falha na transação InnoDB:", e);
		Main.sendMessageToOps("§cOcorreu uma falha ao processar alguma transação remotamente no banco de dados, os erros foram reportados no console interativo do servidor.");
	}
	
	public static void sendMessageToOps(Object x) {
		instance.getServer().getOnlinePlayers().stream().filter(Player::isOp).forEach(player -> player.sendMessage(valueOf(x)));
	}
	
	public static void info(Object x) {
		instance.getLogger().log(Level.INFO, valueOf(x));
	}
	
	public static void log(Level level, Object x) {
		instance.getLogger().log(level, valueOf(x));
	}
	
	public static void log(Level level, Object x, Throwable t) {
		instance.getLogger().log(level, valueOf(x), t);
	}
	
	public static Main getInstance() {
		return instance;
	}
	
}