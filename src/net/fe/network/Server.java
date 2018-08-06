package net.fe.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import net.fe.Session;

// TODO: Auto-generated Javadoc
/**
 * The Class Server.
 */
public final class Server implements MessageDestination {
	
	/** a logger */
	private static final Logger logger = Logger.getLogger("net.fe.network.Server");
	static {
		logger.setLevel(java.util.logging.Level.FINER);
		logger.addHandler(new java.util.logging.ConsoleHandler());
		try {
			java.nio.file.Files.createDirectories(new java.io.File("logs").toPath());
			String file = "logs/server_log_" + LocalDateTime.now().toString().replace("T", "@").replace(":", "-") + ".log";
			java.util.logging.Handler h = new java.util.logging.FileHandler(file);
			h.setFormatter(new java.util.logging.SimpleFormatter());
			logger.addHandler(h);
		} catch (IOException e) {
			logger.throwing("net.fe.network.Client", "logging initializing", e);
		}
	}
	
	public static final int DEFAULT_PORT = 21255;
	private int port = DEFAULT_PORT;
	
	/** The clients. */
	final CopyOnWriteArrayList<ServerListener> clients;
	
	/** The messages. Should only operate on if the monitor to messagesLock is held */
	public final ArrayList<Message> messages;
	
	/** A lock which should be waited upon or notified for changes to messages */
	public final Object messagesLock;
	
	/** The session. */
	private final Session session;
	
	/** Contains the next playerId to be used when a player joins the server */
	private int nextPlayerId = 1;
	
	/**
	 * Instantiates a new server.
	 */
	public Server(Session s, int port) {
		messages = new ArrayList<Message>();
		messagesLock = new Object();
		clients = new CopyOnWriteArrayList<ServerListener>();
		session = s;
		this.port = port;
	}
	
	/**
	 * Start.
	 *
	 * @param port the port
	 */
	public void start() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			logger.info("SERVER: Waiting for connections...");
			while(true) {
				Socket connectSocket = serverSocket.accept();
				logger.info("SERVER: Connection #"+nextPlayerId+" accepted!");
				ServerListener listener = new ServerListener(this, connectSocket, nextPlayerId);
				clients.add(listener);
				listener.start();
				nextPlayerId++;
			}
		} catch (IOException e) {
			logger.throwing("Server", "start", e);
		}
	}
	
	/**
	 * Sends a message to all clients.
	 *
	 * @param message the message
	 */
	public void broadcastMessage(Message message) {
		logger.finer("[SEND]" + message);
		for(ServerListener out : clients) {
			out.sendMessage(message);
		}
	}
	
	/**
	 * Gets the session.
	 *
	 * @return the session
	 */
	public Session getSession() {
		return session;
	}

	@Override
	public void addMessage(Message message) {
		synchronized(messagesLock) {
			messages.add(message);
			messagesLock.notifyAll();
		}
	}
}
