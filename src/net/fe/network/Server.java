package net.fe.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import net.fe.network.message.KickMessage;
import net.fe.network.message.RejoinMessage;

// TODO: Auto-generated Javadoc
/**
 * The Class Server.
 */
public final class Server {
	
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
	public static final long TIMEOUT = 30000; // 30 seconds
	
	
	/** The clients. */
	private final CopyOnWriteArrayList<ServerListener> clients;
	private final TreeMap<Long, ServerListener> pastClients;
	
	/** The messages. Should only operate on if the monitor to messagesLock is held */
	private final ArrayList<Message> messages;
	
	/** A lock which should be waited upon or notified for changes to messages */
	private final Object messagesLock;
	
	private IDManager manager;
	
	private ArrayList<Message> broadcastedMessages = new ArrayList<>();
	public final FEServer feserver;
	
	/**
	 * Instantiates a new server.
	 */
	public Server(int port, FEServer feserver, IDManager manager) {
		messages = new ArrayList<Message>();
		messagesLock = new Object();
		clients = new CopyOnWriteArrayList<ServerListener>();
		pastClients = new TreeMap<>();
		manager = new IDManager();
		this.port = port;
		this.feserver = feserver;
		this.manager = manager;
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
				int id = manager.newPlayerID();
				logger.info("SERVER: Connection #" + id + " accepted!");
				ServerListener listener = new ServerListener(this, connectSocket, id);
				clients.add(listener);
				feserver.addListener(listener);
				listener.start();
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
		message.setTimestamp(System.currentTimeMillis());
		broadcastedMessages.add(message);
		for(ServerListener out : clients) {
			out.sendMessage(message);
		}
	}
	
	public ServerListener getClient(int id) {
		for(int i = 0; i < clients.size(); i++)
			if(clients.get(i).getId() == id)
				return clients.get(i);
		return null;
	}
	
	public void timeoutClients() {
		long minTimestamp = System.currentTimeMillis() - TIMEOUT;
		synchronized(pastClients) {
			while(!pastClients.isEmpty() && pastClients.firstKey() <= minTimestamp) {
				ServerListener listener = pastClients.pollFirstEntry().getValue();
				KickMessage kick = new KickMessage(0, listener.getId(), "Timed out");
				broadcastMessage(kick);
				synchronized(messagesLock) {
					messages.add(kick);
				}
			}
		}
	}

	public boolean validateRejoinRequest(RejoinMessage message) {
		timeoutClients();
		Iterator<Entry<Long, ServerListener>> iterator = pastClients.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<Long, ServerListener> entry = iterator.next();
			if(entry.getValue().getId() == message.origin && entry.getValue().getToken() == message.getToken()) {
				pastClients.remove(entry.getKey());
				return true;
			}
		}
		return false;
	}
	
	public Message[] getBroadcastedMessages() {
		return broadcastedMessages.toArray(new Message[0]);
	}

	public void removeListener(boolean allowReconnection, ServerListener listener) {
		synchronized (clients) {
			clients.remove(listener);
		}
		if(allowReconnection) {
			synchronized (pastClients) {
				pastClients.put(System.currentTimeMillis(), listener);
			}
		}
	}
}
