package net.fe.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ConsoleCommandHandler {
	
	private FEServer server;
	private Command[] commands;
	
	private static final Comparator<Command> COMMAND_COMPARATOR = (a, b) -> a.getName().compareTo(b.getName());
	
	public ConsoleCommandHandler(FEServer server) {
		this.server = server;
		this.commands = getCommands();
		
	}
	
	public void run() {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
			while(true) {
				String[] command = parseCommand(in.readLine());
				int index = Arrays.binarySearch(commands, new NullCommand(command[0]), COMMAND_COMPARATOR);
				if(index >= 0)
					commands[index].run(command, server);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String[] parseCommand(String command) {
		ArrayList<StringBuilder> list = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		list.add(current);
		boolean inString = false;
		for(int i = 0; i < command.length(); i++) {
			if(command.charAt(i) == '\\')
				current.append(command.charAt(++i));
			else if(command.charAt(i) == '"') {
				inString = !inString;
			} else if(!inString && command.charAt(i) == ' ') {
				current = new StringBuilder();
				list.add(current);
			} else {
				current.append(command.charAt(i));
			}
		}

		String[] ans = new String[list.size()];
		for(int i = 0; i < ans.length; i++)
			ans[i] = list.get(i).toString();
		return ans;
	}
	

	private Command[] getCommands() {
		ArrayList<Command> commands = new ArrayList<>();
		commands.add(new BasicCommand("Provides the help of all available commands", "list", "", "", (args, server) -> {
			for(int i = 0; i < commands.size(); i++)
				System.out.println(commands.get(i).getName() + "\t" + commands.get(i).getDescription());
		}));
		commands.add(new BasicCommand("Provides help information about a command", "help", "[command]", "", (args, server) -> {
			for(int i = 0; i < commands.size(); i++)
				System.out.println(commands.get(i).getName() + "\t" + commands.get(i).getDescription());
		}));
		for(Command command : Commands.values())
			commands.add(command);
		commands.sort(COMMAND_COMPARATOR);
		return commands.toArray(new Command[0]);
	}
	
	private interface Command {
		public String getDescription();
		public String getName();
		public String getUsage();
		public String getLongDescription();
		public void run(String[] args, FEServer server);
	}
	
	private static class BasicCommand implements Command {
		
		private String description;
		private String name;
		private String usage;
		private String longDescription;
		private CommandRunnable action;
		
		public BasicCommand(String description, String name, String usage, String longDescription, CommandRunnable action) {
			this.description = description;
			this.name = name;
			this.usage = usage;
			this.longDescription = longDescription;
			this.action = action;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void run(String[] args, FEServer server) {
			action.run(args, server);
		}

		@Override
		public String getUsage() {
			return usage;
		}

		@Override
		public String getLongDescription() {
			return longDescription;
		}
	}
	
	private static class NullCommand extends BasicCommand {

		public NullCommand(String name) {
			super(null, name, null, null, null);
		}
		
	}
	
	private static enum Commands implements Command {
		STATS("Returns various stats about the server", "", (args, server) -> {
			System.out.println("Lobbies: " + server.lobbyCount());
			System.out.println("Players: " + server.getListenerCount());
			for(Thread thread : Thread.getAllStackTraces().keySet()) {
				System.out.println(thread.getName());
			}
		});
		
		
		private String description;
		private String name;
		private String usage;
		private CommandRunnable action;
		
		private Commands(String description, CommandRunnable action) {
			this(description, "", action);
		}
		private Commands(String description, String usage, CommandRunnable action) {
			this.description = description;
			this.name = name().toLowerCase();
			this.usage = usage;
			this.action = action;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void run(String[] args, FEServer server) {
			action.run(args, server);
		}
		
		@Override
		public String getUsage() {
			return usage;
		}
		@Override
		public String getLongDescription() {
			return null;
		}
	}
	
	
	private static interface CommandRunnable {
		public void run(String[] args, FEServer server);
	}
}
