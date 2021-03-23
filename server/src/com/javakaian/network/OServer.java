package com.javakaian.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.javakaian.network.messages.GameWorldMessage;
import com.javakaian.network.messages.LoginMessage;
import com.javakaian.network.messages.LogoutMessage;
import com.javakaian.network.messages.PlayerDied;
import com.javakaian.network.messages.PositionMessage;
import com.javakaian.network.messages.ShootMessage;
import com.javakaian.shooter.OMessageListener;

/**
 * @author oguz
 * 
 *         Server object which is responsible for creating kryo server and
 *         managing it.
 * 
 *         Every message received by server queued by this object and get
 *         processed 60 time per second. After processing messages, related
 *         methods will be invoked by this class.
 * 
 *
 */
public class OServer {

	/** Kyro server. */
	private Server server;

	private int TCP_PORT = 1234;
	private int UDP_PORT = 1235;

	private OMessageListener messageListener;

	/** Queue object to store messages. */
	private Queue<Object> messageQueue;
	/** Connection queue to store connections */
	private Queue<Connection> connectionQueue;

	public OServer(OMessageListener cmo) {

		this.messageListener = cmo;

		init();
	}

	private void init() {

		server = new Server();
		registerClasses();

		messageQueue = new LinkedList<Object>();
		connectionQueue = new LinkedList<Connection>();

		server.addListener(new Listener() {

			@Override
			public void received(Connection connection, Object object) {

				messageQueue.add(object);
				connectionQueue.add(connection);

			}
		});
		server.start();
		try {
			server.bind(TCP_PORT, UDP_PORT);
			System.out.println("Server has ben started on TCP_PORT: " + TCP_PORT + " UDP_PORT: " + UDP_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Gets messages from connection and message queues,parse them and invokes
	 * necessary methods.
	 * 
	 * If one those queues is empty, it returns.
	 * 
	 * This method will be called 60 per second.
	 */
	public void parseMessage() {

		if (connectionQueue.isEmpty() || messageQueue.isEmpty())
			return;

		for (int i = 0; i < messageQueue.size(); i++) {

			Connection con = connectionQueue.poll();
			Object message = messageQueue.poll();

			if (message instanceof LoginMessage) {

				LoginMessage m = (LoginMessage) message;
				messageListener.loginReceived(con, m);

			} else if (message instanceof LogoutMessage) {
				LogoutMessage m = (LogoutMessage) message;
				messageListener.logoutReceived(m);

			} else if (message instanceof PositionMessage) {
				PositionMessage m = (PositionMessage) message;
				messageListener.playerMovedReceived(m);

			} else if (message instanceof ShootMessage) {
				ShootMessage m = (ShootMessage) message;
				messageListener.shootMessageReceived(m);
			}

		}

	}

	/**
	 * This function register every class that will be sent back and forth between
	 * client and server.
	 */
	private void registerClasses() {
		// messages
		this.server.getKryo().register(LoginMessage.class);
		this.server.getKryo().register(LogoutMessage.class);
		this.server.getKryo().register(GameWorldMessage.class);
		this.server.getKryo().register(PositionMessage.class);
		this.server.getKryo().register(PositionMessage.DIRECTION.class);
		this.server.getKryo().register(ShootMessage.class);
		this.server.getKryo().register(PlayerDied.class);
		// primitive arrays
		this.server.getKryo().register(int[].class);
		this.server.getKryo().register(float[].class);
	}

	public Queue<Object> getMessageQueue() {
		return messageQueue;
	}

	public Queue<Connection> getConnectionQueue() {
		return connectionQueue;
	}

	public void sendToAllUDP(Object m) {
		server.sendToAllUDP(m);
	}

	public void sendToUDP(int id, Object m) {
		server.sendToUDP(id, m);
	}
}
