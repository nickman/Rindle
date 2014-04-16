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
package org.helios.rindle.metric;

import java.io.IOException;
import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.base64.Base64;
import org.jboss.netty.handler.codec.base64.Base64Dialect;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * <p>Title: MetricSerialization</p>
 * <p>Description: Container class for metric serialization/deserialization classes</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.metric.MetricSerialization</code></p>
 */

public class MetricSerialization {
	/** The metric global ID JSON key */
	public static final String ID_GID = "id";
	/** The metric creation timestamp JSON key */
	public static final String ID_TIMESTAMP = "ts";
	/** The metric name JSON key */
	public static final String ID_NAME = "n";
	/** The metric opaque key JSON key */
	public static final String ID_OPAQUE = "o";
	
	/**
	 * <p>Title: UnsafeMetricDefinitionSerializer</p>
	 * <p>Description: Jackson JSON serializer for {@link IMetricDefinition}s</p> 
	 * <p>Company: Helios Development Group LLC</p>
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * <p><code>org.helios.rindle.metric.MetricSerialization.UnsafeMetricDefinitionSerializer</code></p>
	 */
	public static class UnsafeMetricDefinitionSerializer extends JsonSerializer<IMetricDefinition> {
		/**
		 * {@inheritDoc}
		 * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		public void serialize(IMetricDefinition value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
			jgen.writeStartObject();
			jgen.writeNumberField(ID_GID, value.getId());
			jgen.writeNumberField(ID_TIMESTAMP, value.getCreatedTimestamp());
			String name = value.getName();
			byte[] okey = value.getOpaqueKey();
			if(name!=null) {
				jgen.writeStringField(ID_NAME, name);
			}
			if(okey!=null) {
				jgen.writeBinaryField(ID_OPAQUE, okey);
			}
			jgen.writeEndObject();
			jgen.flush();
		}		
	}
	
	/**
	 * Encodes the passed bytes into Base 64 and returns the new encoded array
	 * @param bytes The bytes to encode
	 * @return the encoded bytes
	 */
	public static byte[] base64Encode(byte[] bytes) {
		return Base64.encode(ChannelBuffers.wrappedBuffer(bytes), Base64Dialect.ORDERED).array();		
	}
	
	/**
	 * Encodes the passed bytes into Base 64 and returns the new encoded array as a string
	 * @param bytes The bytes to encode
	 * @return the encoded bytes as a string
	 */
	public static String base64EncodeToString(byte[] bytes) {
		return Base64.encode(ChannelBuffers.wrappedBuffer(bytes), Base64Dialect.ORDERED).toString(Charset.defaultCharset());
	}
	
	
	/**
	 * Decodes the passed bytes from Base 64 and returns the new decoded array
	 * @param bytes The bytes to decode
	 * @return the decoded bytes
	 */
	public static byte[] base64Decode(byte[] bytes) {
		return Base64.decode(ChannelBuffers.wrappedBuffer(bytes), Base64Dialect.ORDERED).array();		
	}
	
	/**
	 * Decodes the passed string from Base 64 and returns the new decoded array
	 * @param value The string to decode
	 * @param charset The charset to use to convert
	 * @return the decoded bytes
	 */
	public static byte[] base64Decode(String value, Charset charset) {
		if(value==null) throw new IllegalArgumentException("The passed string was null");

		return Base64.decode(ChannelBuffers.wrappedBuffer(value.getBytes(charset!=null ? charset : Charset.defaultCharset())), Base64Dialect.ORDERED).array();		
	}
	
	/**
	 * Decodes the passed string from Base 64 and returns the new decoded array
	 * @param value The string to decode
	 * @return the decoded bytes
	 */
	public static byte[] base64Decode(String value) {
		return base64Decode(value, Charset.defaultCharset());		
	}
	
	
	
	
	
	
	public static class UnsafeMetricDefinitionDeserializer extends JsonDeserializer<IMetricDefinition> {
		
		public static void log(String format, Object...args) {
			System.out.println(String.format(format, args));
		}
		
		
		@Override
		public IMetricDefinition deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			ObjectNode node = jp.readValueAsTree();
			JsonNode gidNode = node.get(ID_GID);
			if(gidNode==null || !gidNode.isNumber()) throw new IOException("Invalid or missing global ID node");
			long globalId = gidNode.longValue();
			JsonNode tsNode = node.get(ID_TIMESTAMP);
			if(tsNode==null || !gidNode.isNumber()) throw new IOException("Invalid or missing timestamp node");
			long ts = tsNode.longValue();
			JsonNode nameNode = node.get(ID_NAME);
			JsonNode opaqueNode = node.get(ID_OPAQUE);
			String name = null;
			byte[] opaqueKey = null;
			if(nameNode!=null) {
				if(!nameNode.isTextual()) throw new IOException("Invalid node type for name node [" + nameNode.getClass().getSimpleName() + "]");
				name = nameNode.textValue();
			}
			if(opaqueNode!=null) {
				opaqueKey = opaqueNode.binaryValue();
//				if(opaqueNode.isBinary()) {
//					opaqueKey = opaqueNode.binaryValue();
//				} else if(opaqueNode.isTextual()) {
//					opaqueKey =  base64Decode(opaqueNode.textValue());
//				} else {
//					throw new IOException("Invalid node type for opaque key node [" + opaqueNode.getClass().getSimpleName() + "]");
//				}
			}
			
			return new UnsafeMetricDefinition(globalId, ts, name, opaqueKey);
		}
		
	}
	
	private MetricSerialization() {
	}

}
