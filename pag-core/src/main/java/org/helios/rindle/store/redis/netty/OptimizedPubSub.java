/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2007, Helios Development Group and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org. 
 *
 */
package org.helios.rindle.store.redis.netty;

import java.io.Closeable;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusData;
import org.apache.logging.log4j.status.StatusListener;
import org.apache.logging.log4j.status.StatusLogger;
import org.helios.rindle.store.redis.ClientInfo;
import org.helios.rindle.store.redis.ClientInfoProvider;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

/**
 * <p>Title: OptimizedPubSub</p>
 * <p>Description: A Netty NIO based pub sub subscriber.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.redis.netty.OptimizedPubSub</code></p>
 */

public class OptimizedPubSub extends SimpleChannelUpstreamHandler implements PubSub, Closeable, ChannelFutureListener, ClientInfoProvider, StatusListener {
	/** The redis host or IP Address */
	protected final String host;
	/** The redis listening port */
	protected final int port;
	/** The timeout in ms. */
	protected final long timeout;
	/** This connection's ClientInfo instance */
	protected final ClientInfo clientInfo;
	
	/** The redis auth */
	protected final String auth;
	/** The comm channel for subbing */
	protected final Channel subChannel;
	/** The comm channel for pubbing */
	protected volatile Channel pubChannel;
	/** A set of registered redis event listeners */
	protected final Set<SubListener> listeners = new CopyOnWriteArraySet<SubListener>();	
	/** A set of registered redis connectivity listeners */
	protected final Set<ConnectionListener> connectionListeners = new CopyOnWriteArraySet<ConnectionListener>();	
	/** Indicates if this pubSub is connected */
	protected final AtomicBoolean connected = new AtomicBoolean(false);
	/** Flag set when a close is requested to distinguish between a deliberate close and an error */
	protected final AtomicBoolean closeRequested = new AtomicBoolean(false);
	
	/** Instance logger */
	protected final Logger log = LogManager.getLogger(getClass());
	
	/**
	 * Returns an OptimizedPubSub for the passed host and port
	 * @param host The redis host
	 * @param port The redis port
	 * @param auth The redis auth password
	 * @param timeout The timeout in ms.
	 * @return An {@link OptimizedPubSub} instance
	 */
	
	public static OptimizedPubSub getInstance(String host, int port, String auth, long timeout) {
		return new OptimizedPubSub(host, port, auth, timeout);
	}
	
	/**
	 * Returns an OptimizedPubSub for the passed host and port
	 * @param host The redis host
	 * @param port The redis port
	 * @param timeout The timeout in ms.
	 * @return An {@link OptimizedPubSub} instance
	 */
	
	public static OptimizedPubSub getInstance(String host, int port, long timeout) {
		return getInstance(host, port, null, timeout);
	}
	
	/**
	 * Returns an OptimizedPubSub for the passed host and port
	 * @param host The redis host
	 * @param port The redis port
	 * @return An {@link OptimizedPubSub} instance
	 */
	
	public static OptimizedPubSub getInstance(String host, int port) {
		return getInstance(host, port, null, 2000);
	}
	
	
	
	/** The JVM runtime name */
	public static final String RUNTIME_NAME = ManagementFactory.getRuntimeMXBean().getName();
	
	/** The default Redis connection client name  */
	public static final String DEFAULT_REDIS_CLIENT_NAME = String.format("RindlePubSubListener:%s", RUNTIME_NAME);

	
	/**
	 * Creates a new OptimizedPubSub
	 * @param host The redis host
	 * @param port The redis port
	 * @param auth The redis auth password
	 * @param timeout The timeout in ms.
	 */
	private OptimizedPubSub (String host, int port, String auth, long timeout) {
		this.host = host;
		this.port = port;
		this.auth = auth;
		this.timeout = timeout;
		subChannel = OptimizedPubSubFactory.getInstance().newChannelSynch(host, port, timeout);
		subChannel.getPipeline().addLast("SubListener", this);		
		clientName(DEFAULT_REDIS_CLIENT_NAME);
		SocketAddress localAddress = subChannel.getLocalAddress();
		String addressKey = null;
		if(localAddress instanceof InetSocketAddress) {
			InetSocketAddress localInet = (InetSocketAddress)localAddress;
			addressKey = String.format("%s:%s", localInet.getAddress().getHostAddress(), localInet.getPort());
		} else {
			addressKey = localAddress.toString();
		}		
		clientInfo = new ClientInfo(DEFAULT_REDIS_CLIENT_NAME, addressKey, this);
		
		connected.set(true);
		fireConnected();
		StatusLogger.getLogger().registerListener(this);
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.redis.ClientInfoProvider#getClientInfo()
	 */
	@Override
	public ClientInfo getClientInfo() {
		return clientInfo;
	}
	
	/**
	 * Indicates if this pubSub is connected.
	 * @return true if this pubSub is connected, false otherwise
	 */
	public boolean isConnected() {
		return connected.get();
	}
	
	/**
	 * Fired when subChannel closes.
	 * {@inheritDoc}
	 * @see org.jboss.netty.channel.ChannelFutureListener#operationComplete(org.jboss.netty.channel.ChannelFuture)
	 */
	@Override
	public void operationComplete(ChannelFuture future) throws Exception {		
		if(pubChannel.isConnected()) {
			pubChannel.close();
		}
		connected.set(false);
		if(closeRequested.get()) {
			fireClose(null);
		} else {
			Throwable t = future.getCause();
			fireClose(t!=null ? t : new Throwable());
		}
		closeRequested.set(false);
	}

	
	/**
	 * Returns a pipelining version of this pubsub instance
	 * @return a pipelining version of this pubsub instance
	 */
	public PipelinedOptimizedPubSub getPipelinedPubSub() {
		return new PipelinedOptimizedPubSub(host, port, auth, timeout);
	}
	
	/**
	 * <p>Title: PipelinedOptimizedPubSub</p>
	 * <p>Description: An OptimizedPubSub extension that buffers all calls until a flush occurs, providing a redis pipelined command set.</p> 
	 * <p>Company: Helios Development Group LLC</p>
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * <p><code>org.helios.rindle.store.redis.netty.OptimizedPubSub.PipelinedOptimizedPubSub</code></p>
	 */
	public static class PipelinedOptimizedPubSub extends OptimizedPubSub {
		/** The subscriber channel pipeline buffer */
		private final ConfirmingBufferedWriteHandler subBufferingHandler;
		/** The sub pipeline queue */
		private final Queue<MessageEvent> subQueue = QueueFactory.createQueue(MessageEvent.class);
		/** The publisher channel pipeline buffer */
		private volatile ConfirmingBufferedWriteHandler pubBufferingHandler;
		/** The pub pipeline queue */
		private  Queue<MessageEvent> pubQueue = null;
		
		/**
		 * Flushes the publisher pipeline
		 */
		public void flushPub() {
			if(pubChannel!=null) {
				if(pubQueue.size()>0) {
					pubBufferingHandler.flush(true);
				}
			}
		}
		
		/**
		 * Flushes the subscriber pipeline
		 */
		public void flushSub() {
			if(subQueue.size()>0) {
				subBufferingHandler.flush(true);
			}
		}
		
		/**
		 * Flushes the subscriber and publisher pipelines
		 */
		public void flush() {
			flushSub();
			flushPub();
		}
		
		
		
		/**
		 * Initializes a non-pipelined pub channel
		 */
		@Override
		protected void initPublishChannel() {
			if(pubChannel==null) {
				synchronized(this) {
					if(pubChannel==null) {
						pubChannel =  OptimizedPubSubFactory.getInstance().newChannelSynch(host, port, timeout);
						pubChannel.getPipeline().addLast("PubListener", this);
						pubQueue = QueueFactory.createQueue(MessageEvent.class);
						pubBufferingHandler = new ConfirmingBufferedWriteHandler(pubQueue, false);
						pubChannel.getPipeline().addAfter(OptimizedPubSubFactory.REQ_ENCODER_NAME, "pubPipelineBuffer", UnidirectionalChannelHandlerFactory.delegate(pubBufferingHandler, false));						
					}
				}
			}
		}
		
		
		/**
		 * Creates a new PipelinedOptimizedPubSub
		 * @param host The redis host
		 * @param port The redis port
		 * @param auth The redis auth password
		 * @param timeout The timeout in ms.
		 */
		protected PipelinedOptimizedPubSub(String host, int port, String auth, long timeout) {
			super(host, port, auth, timeout);
			subBufferingHandler = new ConfirmingBufferedWriteHandler(subQueue, false);
			subChannel.getPipeline().addAfter(OptimizedPubSubFactory.REQ_ENCODER_NAME, "subPipelineBuffer", UnidirectionalChannelHandlerFactory.delegate(subBufferingHandler, false));
		}



	}
	
	
	/**
	 * {@inheritDoc}
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		Object msg = e.getMessage();
		
		if(msg instanceof MessageReply) {
			MessageReply mr = (MessageReply)msg;
			mr.publish(listeners);
		} else if(msg instanceof SubscribeConfirm) {
			log(msg);
		} else if(msg instanceof Integer) {
			//log("[" + msg + "] Clients Received Published Message");
		}
	}
	
	/**
	 * Closes this PubSub
	 */
	@Override
	public void close()  {
		closeRequested.set(true);
		subChannel.close();
		if(pubChannel!=null && pubChannel.isConnected()) {
			pubChannel.close();
		}		
	}
	
	/**
	 * Fires a clean close event on registered connection listeners
	 * @param t The disconnect cause or null if the close was requested
	 */
	protected void fireClose(Throwable t) {
		for(ConnectionListener listener: connectionListeners) {
			listener.onDisconnect(this, t);
		}
	}

	/**
	 * Fires a connect event on registered connection listeners
	 */
	protected void fireConnected() {
		for(ConnectionListener listener: connectionListeners) {
			listener.onConnect(this);
		}
	}
	
	/**
	 * Creates a new OptimizedPubSub
	 * @param host The redis host
	 * @param port The redis port
	 * @param timeout The timeout in ms.
	 */
	private OptimizedPubSub (String host, int port, int timeout) {
		this(host, port, null, timeout);
	}
	
	
	/**
	 * Flushes a pipelined request batch
	 * @param channel The channel to flush
	 * @return the ChannelFuture of this write
	 */
	protected ChannelFuture  pipelinedFlush(Channel channel) {
		ChannelFuture cf = channel.write(true);
		return cf;		
	}
	
	/**
	 * Cancels a pipelined request batch
	 * @param channel The channel to cancel a pipelined request in
	 * @return the ChannelFuture of this cancel
	 */
	protected ChannelFuture  pipelinedCancel(Channel channel) {
		ChannelFuture cf = channel.write(false);
		return cf;		
	}	

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.redis.netty.PubSub#subscribe(java.lang.String[])
	 */
	@Override
	public ChannelFuture subscribe(String... channels) {
		return subChannel.write(PubSubRequest.newRequest(PubSubCommand.SUBSCRIBE, channels));
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.redis.netty.PubSub#unsubscribe(java.lang.String[])
	 */
	@Override
	public ChannelFuture unsubscribe(String... channels) {
		return subChannel.write(PubSubRequest.newRequest(PubSubCommand.UNSUBSCRIBE, channels));
		
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.redis.netty.PubSub#psubscribe(java.lang.String[])
	 */
	@Override
	public ChannelFuture psubscribe(String... patterns) {
		return subChannel.write(PubSubRequest.newRequest(PubSubCommand.PSUBSCRIBE, patterns));
		
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.redis.netty.PubSub#clientList()
	 */
	@Override
	public ChannelFuture clientList() {
		return subChannel.write(PubSubRequest.newRequest(PubSubCommand.CLIENT, "LIST"));		
	}
	
	
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.redis.netty.PubSub#clientName(java.lang.String)
	 */
	@Override
	public ChannelFuture clientName(String name) {		
		return subChannel.write(PubSubRequest.newClientRequest(PubSubCommand.CLIENT, "SETNAME", name));
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.redis.netty.PubSub#punsubscribe(java.lang.String[])
	 */
	@Override
	public ChannelFuture punsubscribe(String... patterns) {
		return subChannel.write(PubSubRequest.newRequest(PubSubCommand.PUNSUBSCRIBE, patterns));		
	}
	
	/**
	 * Initializes a non-pipelined pub channel
	 */
	protected void initPublishChannel() {
		if(pubChannel==null) {
			synchronized(this) {
				if(pubChannel==null) {
					pubChannel =  OptimizedPubSubFactory.getInstance().newChannelSynch(host, port, timeout);
					pubChannel.getPipeline().addLast("PubListener", this);
				}
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.redis.netty.PubSub#publish(java.lang.String, java.lang.String[])
	 */
	@Override
	public ChannelFuture publish(String channel, String...messages) {
		initPublishChannel();		
		ChannelFuture cf = null;
		if(messages!=null && messages.length>0) {
			if(messages.length !=0) {
				for(String message: messages) {
					if(message==null) continue;
					message = message.trim();
					if(message.length()<1) continue;
					cf = pubChannel.write(PubSubRequest.newRequest(PubSubCommand.PUBLISH, channel, message));
				}				
			}
		}
		return cf;
	}
	
	
	
	
	public static void main(String[] args) {
		log("OPubSub Test");
		OptimizedPubSub pubsub = OptimizedPubSub.getInstance("dashku", 6379);
		pubsub.subscribe("foo.bar");
		pubsub.psubscribe("foo*").awaitUninterruptibly();
		final Set<String> execThreadNames = new HashSet<String>();
		pubsub.registerListener(new SubListener(){
			
			@Override
			public void onChannelMessage(String channel, String message) {
				execThreadNames.add(Thread.currentThread().getName());
				log("[" + Thread.currentThread().getName() + "] Channel Message\n\tChannel:" + channel + "\n\tMessage:" + message);
			}
			@Override
			public void onPatternMessage(String pattern, String channel, String message) {
				execThreadNames.add(Thread.currentThread().getName());
				log("[" + Thread.currentThread().getName() + "]  Pattern Message\n\tPattern:" + pattern + "\n\tChannel:" + channel + "\n\tMessage:" + message);
			}
			
		});
		pubsub.publish("foo.bar", "Hello Venus");
		String[] props = System.getProperties().stringPropertyNames().toArray(new String[0]);		
		long start = System.currentTimeMillis();
		pubsub.publish("foo.bar", props).awaitUninterruptibly();
		long elapsed = System.currentTimeMillis()-start;
		log("\n\t======================================\n\t[" + props.length + "] Messages Sent In [" + elapsed + "] ms.\n\t======================================\n");
		
		PipelinedOptimizedPubSub pipePubSub = pubsub.getPipelinedPubSub();
		for(int i = 0; i < 100; i++) {
			boolean pipeline = i%2==0;
			start = System.currentTimeMillis();
			if(pipeline) {
				ChannelFuture cf = pipePubSub.publish("foo.bar", props);
				while(true) { if(cf.isDone()) break; else Thread.yield(); }
				pipePubSub.flushPub();				
			} else {
				pubsub.publish("foo.bar", props).awaitUninterruptibly();
			}
			elapsed = System.currentTimeMillis()-start;
			if(pipeline) {
				log("\n\t======================================\n\t[" + props.length + "] Pipelined Messages Sent In [" + elapsed + "] ms.\n\t======================================\n");
			} else {
				log("\n\t======================================\n\t[" + props.length + "] Messages Sent In [" + elapsed + "] ms.\n\t======================================\n");
			}
			try { Thread.currentThread().join(1000); } catch (Exception ex) {}
		}
		
		try {
			Thread.currentThread().join(2000);
			log(execThreadNames);
			Thread.currentThread().join();
			pubsub.close();
			pipePubSub.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	public static void log(Object msg) {
		System.out.println(msg);
	}
	


	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.redis.netty.PubSub#registerListener(org.helios.rindle.store.redis.netty.SubListener)
	 */
	@Override
	public void registerListener(SubListener listener) {
		if(listener!=null) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Registers a connection listener
	 * @param listener The listener to register
	 */
	public void registerConnectionListener(ConnectionListener listener) {
		if(listener!=null) {
			connectionListeners.add(listener);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.redis.netty.PubSub#unregisterListener(org.helios.rindle.store.redis.netty.SubListener)
	 */
	@Override
	public void unregisterListener(SubListener listener) {
		if(listener!=null) {
			listeners.remove(listener);
		}		
	}

	/**
	 * Unregisters a connection listener
	 * @param listener The listener to unregister
	 */
	public void unregisterConnectionListener(ConnectionListener listener) {
		if(listener!=null) {
			connectionListeners.remove(listener);
		}
	}

	/**
	 * Returns the host this pubsub is connected to 
	 * @return the host this pubsub is connected to 
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Returns the port this pubsub is connected to 
	 * @return the port this pubsub is connected to
	 */
	public int getPort() {
		return port;
	}

	@Override
	public void log(StatusData data) {
		log.info("Logging StatusData: {}", data);
		
	}

	@Override
	public Level getStatusLevel() {
		
		return Level.INFO;
	}


	
}
