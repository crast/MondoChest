package us.crast.mondochest.command;

import us.crast.mondochest.security.MondoSecurity;
import us.crast.mondochest.security.PermissionChecker;

public class SubCommand {
	private String name;
	private boolean allow_console = false;
	private int minArgs = 0;
	private PermissionChecker checker;
	private SubHandler handler = null;
	private String description;
	private String usage = null;
	
	public SubCommand(String name, String permission) {
		this.name = name;
		this.checker = MondoSecurity.getChecker(permission);
	}

	public SubCommand(String name, String permission, SubHandler handler) {
		this(name, permission);
		this.handler = handler;
	}
	
	public SubCommand allowConsole() {
		this.allow_console = true;
		return this;
	}
	
	public boolean isConsoleAllowed() {
		return this.allow_console;
	}

	public int getMinArgs() {
		return minArgs;
	}

	public SubCommand setMinArgs(int minArgs) {
		this.minArgs = minArgs;
		return this;
	}

	public SubHandler getHandler() {
		return handler;
	}

	public SubCommand setHandler(SubHandler handler) {
		this.handler = handler;
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUsage() {
		return this.usage;
	}
	
	public SubCommand setUsage(String usage) {
		this.usage = usage;
		return this;
	}

	public PermissionChecker getChecker() {
		return checker;
	}

	public String getDescription() {
		return description;
	}

	public SubCommand setDescription(String description) {
		this.description = description;
		return this;
	}
	
}
