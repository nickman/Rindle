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

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.base64.Base64;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

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
				jgen.writeBinaryField(ID_OPAQUE, Base64.encode(ChannelBuffers.wrappedBuffer(okey)).array());
			}
			jgen.writeEndObject();
			jgen.flush();
		}		
	}
	
	
	
	public static class UnsafeMetricDefinitionDeserializer extends JsonDeserializer<IMetricDefinition> {
		
		public static void log(String format, Object...args) {
			System.out.println(String.format(format, args));
		}
		
		
		@Override
		public IMetricDefinition deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			// public UnsafeMetricDefinition(long globalId, long timestamp, String name, byte[] opaqueKey) {
			JsonToken t = jp.getCurrentToken();
			do {
				switch(t) {
				case END_ARRAY:
					log("]");
					break;
				case END_OBJECT:
					log("}");
					break;
				case FIELD_NAME:
					log("fn:", jp.readValueAs(String.class));
					break;
				case NOT_AVAILABLE:
					log("NA");
					break;
				case START_ARRAY:
					log("[");
					break;
				case START_OBJECT:
					log("{");
					break;
				case VALUE_EMBEDDED_OBJECT:
					log("EMB");
					break;
				case VALUE_FALSE:
					log("false");
					break;
				case VALUE_NULL:
					log("NULL");
					break;
				case VALUE_NUMBER_FLOAT:
					log("Float:", jp.getFloatValue());
					break;
				case VALUE_NUMBER_INT:
					log("Int:", jp.getIntValue());
					break;
				case VALUE_STRING:
					log("Str:", jp.getText());
					break;
				case VALUE_TRUE:
					log("true:");
					break;
				default:
					break;
				
				}
				t = jp.nextToken();				
			} while(t!=null);
			return null;
		}
		
	}
	
	private MetricSerialization() {
	}

}
