package net.fe.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * An object whose purpose is to manage socket connections.
 * @author wellme
 */
public final class Server {
	
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
	
	private IDManager manager;
	
	public final FEServer feserver;
	
	public Server(int port, FEServer feserver, IDManager manager) {
		this.port = port;
		this.feserver = feserver;
		this.manager = manager;
	}
	
	public void start() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			logger.info("SERVER: Waiting for connections on port " + port);
			while(true) {
				Socket connectSocket = serverSocket.accept();
				int id = manager.newPlayerID();
				logger.info("SERVER: Connection #" + id + " accepted!");
				ServerListener listener = new ServerListener(feserver, connectSocket, id);
				feserver.addListener(listener);
				listener.start();
			}
		} catch (IOException e) {
			logger.throwing("Server", "start", e);
		}
	}
	
}
