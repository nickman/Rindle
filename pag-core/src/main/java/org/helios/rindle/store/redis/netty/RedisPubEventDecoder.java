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

import static org.helios.rindle.store.redis.netty.CR.CR_BYTES;
import static org.helios.rindle.store.redis.netty.CR.CR_LENGTH;
import static org.helios.rindle.store.redis.netty.ProtocolByte.ASTERISK_BYTE;
import static org.helios.rindle.store.redis.netty.ProtocolByte.COLON_BYTE;
import static org.helios.rindle.store.redis.netty.ProtocolByte.DOLLAR_BYTE;
import static org.helios.rindle.store.redis.netty.ProtocolByte.MINUS_BYTE;
import static org.helios.rindle.store.redis.netty.ProtocolByte.PLUS_BYTE;
import static org.helios.rindle.store.redis.netty.RedisPubEvent.ARG_COUNT;
import static org.helios.rindle.store.redis.netty.RedisPubEvent.ERROR;
import static org.helios.rindle.store.redis.netty.RedisPubEvent.NEXT_MESSAGE;
import static org.helios.rindle.store.redis.netty.RedisPubEvent.NEXT_SIZE;
import static org.helios.rindle.store.redis.netty.RedisPubEvent.NEXT_SIZE_PREFIX;
import static org.helios.rindle.store.redis.netty.RedisPubEvent.TYPE;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;

/**
 * <p>Title: RedisPubEventDecoder</p>
 * <p>Description: A Replay decoder for Redis multibulk replies</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.redis.netty.RedisPubEventDecoder</code></p>
 * @param <T> 
 */
public class RedisPubEventDecoder<T> extends ReplayingDecoder<RedisPubEvent> {
	
	/** The default platform charset */
	public static final Charset CHARSET = Charset.defaultCharset();
	
	/**
	 * Renders the contents of the passed channel buffer using the default platform charset 
	 * @param cb the channel buffer to render
	 * @return the rendered content
	 */
	public String ts(ChannelBuffer cb) {
		if(cb==null) return "<null>";
		int readable = super.actualReadableBytes();
		if(readable<1) return "<empty>";
		byte[] bytes = new byte[readable];
		cb.getBytes(0, bytes);
		return new String(bytes, CHARSET);
	}

	/**
	 * Creates a new RedisPubEventDecoder
	 */
	public RedisPubEventDecoder() {
		super(RedisPubEvent.TYPE);
	}
	
	/**
	 * Drains the stream of the CR bytes between each redis line
	 * @param cb The channel buffer to read from
	 * @throws Exception thrown if the byte sequence cannot be fully read or is fullly read but does not contain the expected bytes.
	 */
	protected void  readCr(ChannelBuffer cb) throws Exception {
		byte[] bytes = new byte[CR_LENGTH];
		cb.readBytes(bytes);
		if(!Arrays.equals(CR_BYTES, bytes)) {
			throw new Exception("Unexpected byte sequence [" + new String(bytes) + "]. Expected [" + new String(CR_BYTES) + "]", new Throwable());
		}
	}
	
	/**
	 * Reads bytes from the passed channel buffer until a {@link CR#BYTE2} is found.
	 * @param cb The channel buffer to read from 
	 * @return thr read bytes
	 * @throws Exception
	 */
	protected byte[]  readUntilCr(ChannelBuffer cb) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(10);
		
		int progress = 0;
		byte b = -1;
		while(progress<2) {
			b = cb.readByte();
			if(b==CR.BYTE1 || b==CR.BYTE2) {
				if(progress==0) {
					if(b==CR.BYTE1) {
						progress++;
					}
				} else if(progress==1) {
					if(b==CR.BYTE2) {
						break;
					}
					progress = 1; 
				}				
			} else {
				baos.write(b);
			}
		}
		return baos.toByteArray();
	}
	

	/**
	 * Reads a byte array from the channel buffer and returns it
	 * @param cb The channel buffer to read from
	 * @param expectedBytes The expected number of bytes
	 * @return the read byte array
	 */
	protected byte[]  read(ChannelBuffer cb, int expectedBytes)  {
		byte[] bytes = new byte[expectedBytes];
		cb.readBytes(bytes);
		return bytes;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.jboss.netty.handler.codec.replay.ReplayingDecoder#decode(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel, org.jboss.netty.buffer.ChannelBuffer, java.lang.Enum)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer channelBuffer, RedisPubEvent state) throws Exception {
		switch (state) {
			case TYPE:
				byte typeByte = channelBuffer.readByte();
				if(typeByte==ASTERISK_BYTE.getByte()) {					
					checkpoint(ARG_COUNT);					
				} else if(typeByte==COLON_BYTE.getByte()) {
					byte[] confirmBytes = readUntilCr(channelBuffer);
					checkpoint(TYPE);		
					return Integer.parseInt(new String(confirmBytes));
				} else if(typeByte==MINUS_BYTE.getByte()) {
					checkpoint(ERROR);
				} else if(typeByte==PLUS_BYTE.getByte()) {
					byte[] confirmBytes = readUntilCr(channelBuffer);
					String msg = new String(confirmBytes);
					if("OK".equals(msg)) {
						return "OK";
					}
					if(msg.startsWith("OK")) {
						/* What to do here ? */
					}
					checkpoint(TYPE);							
				} else {
					throw new Exception("Unexpected byte character [" + (char)typeByte + "] Expected [" + ASTERISK_BYTE + "]", new Throwable());
				}
				break;
			case ERROR:
				Exception e = new Exception(new String(readUntilCr(channelBuffer)), new Throwable());
				checkpoint(TYPE);
				throw e;
			case ARG_COUNT:
				byte[] argsInBytes = readUntilCr(channelBuffer);
				int argCount = Integer.parseInt(new String(argsInBytes));
				ctx.setAttachment(new Object[] {new AtomicInteger(argCount), null, new ArrayList<byte[]>(argCount)});
				checkpoint(NEXT_SIZE_PREFIX);
			//$FALL-THROUGH$
			case NEXT_SIZE_PREFIX:
				byte sizePrefixByte = channelBuffer.readByte();
				if(sizePrefixByte==DOLLAR_BYTE.getByte()) {
					checkpoint(NEXT_SIZE);
				} else if(sizePrefixByte==COLON_BYTE.getByte()) {
					readUntilCr(channelBuffer);
					checkpoint(TYPE);
					return processFinal(ctx.getAttachment());
				} else {
					throw new Exception("Unexpected byte character [" + (char)sizePrefixByte + "] Expected [" + DOLLAR_BYTE + "]", new Throwable());
				}				
				break;			
			case END_OF_ARG:
				readUntilCr(channelBuffer);
				break;
			case NEXT_SIZE:
				byte[] nextSizeInBytes = readUntilCr(channelBuffer);				
				int nextSize = Integer.parseInt(new String(nextSizeInBytes));
				((Object[])ctx.getAttachment())[1] = nextSize;
				checkpoint(NEXT_MESSAGE);
				//$FALL-THROUGH$
			case NEXT_MESSAGE:
				Object[] channelState  = (Object[])ctx.getAttachment();
				nextSize = (Integer)channelState[1];
				((ArrayList<byte[]>)channelState[2]).add(read(channelBuffer, nextSize));
				readCr(channelBuffer);
				if(((AtomicInteger)channelState[0]).decrementAndGet()==0) {
					checkpoint(TYPE);
					return processFinal(ctx.getAttachment());
				}
				checkpoint(NEXT_SIZE_PREFIX);
				break;
		case CR:
			break;
		default:
			break;				
		}
		return null;
	}

	/**
	 * Processes the context attachment at the end of the decode
	 * @param attachment The context attachment
	 * @return the return value of the decoder
	 */
	@SuppressWarnings("unchecked")
	private Object processFinal(Object attachment) {
		ArrayList<byte[]> arrList = ((ArrayList<byte[]>)((Object[])attachment)[2]);
		int arrListSize = arrList.size();
		if(arrListSize<5 && arrListSize>2) {
			return MessageReply.create(arrList);
		} else if(arrListSize==2) {
			return SubscribeConfirm.create(arrList);
		} else {
			return null;
		}
	}

}
