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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * <p>Title: UnsafeMetricDefinitionSerializer</p>
 * <p>Description: Jackson serializer for a {@link UnsafeMetricDefinition}</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.metric.UnsafeMetricDefinitionSerializer</code></p>
 */

public class UnsafeMetricDefinitionSerializer extends JsonSerializer<IMetricDefinition> {

	/**
	 * Creates a new UnsafeMetricDefinitionSerializer
	 */
	public UnsafeMetricDefinitionSerializer() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * {@inheritDoc}
	 * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
	 */
	@Override
	public void serialize(IMetricDefinition value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		/*
		 * 			jsonGen.writeStartObject();
			jsonGen.writeNumberField("id", id);
			jsonGen.writeNumberField("rerid", reRequestId);
			jsonGen.writeStringField("t", type);
			if(openedAsMap) {
				jsonGen.writeObjectFieldStart("msg");
			} else {
				jsonGen.writeArrayFieldStart("msg");
			}

		 */
		
		
		jgen.writeNumberField("id", value.getId());
		jgen.writeNumberField("ts", value.getCreatedTimestamp());
		String name = value.getName();
		byte[] okey = value.getOpaqueKey();
		if(name!=null) {
			jgen.writeStringField("n", name);
		}
		if(okey!=null) {
			jgen.writeBinaryField("o", Base64.encode(ChannelBuffers.wrappedBuffer(okey)).array());
		}
		jgen.writeEndObject();
		jgen.flush();
	}
	
	public static void main(String[] args) {
		log("Test Metric Ser");
		IMetricDefinition[] metrics = {
				new UnsafeMetricDefinition(54, "FooBar", "FooBar".getBytes()),
				new UnsafeMetricDefinition(77, "MeToo", "Yoyo".getBytes())
		};
		
		ObjectMapper jsonMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(IMetricDefinition.class, new UnsafeMetricDefinitionSerializer());
		module.addDeserializer(IMetricDefinition.class, new MetricSerialization.UnsafeMetricDefinitionDeserializer());
		jsonMapper.registerModule(module);
		try {
//			String s = jsonMapper.writeValueAsString(metrics);
//			log(s);
			String text = "[{\"id\":54,\"ts\":1397644799993,\"n\":\"FooBar\",\"o\":\"Um05dlFtRnk=\"},{\"id\":77,\"ts\":1397644799993,\"n\":\"MeToo\",\"o\":\"V1c5NWJ3PT0A\"}]";
			jsonMapper.readValue(text, IMetricDefinition[].class);
		} catch(Exception x) {
			x.printStackTrace(System.err);
		}
//		jsonMapper.registerModule(mod);
	}
	public static void log(Object msg) {
		System.out.println(msg);
	}

/*
 	REDIS JSON:
 	[
 		{"id":3,"ts":"1397644709256","o":"XYX"},
 		{"id":4,"ts":"1397644709259","o":"FU","n":"SNA"}
 	]
 	
 	Jackson JSON
	[{\"id\":54,\"ts\":1397644799993,\"n\":\"FooBar\",\"o\":\"Um05dlFtRnk=\"},{\"id\":77,\"ts\":1397644799993,\"n\":\"MeToo\",\"o\":\"V1c5NWJ3PT0A\"}]
 	
 	
 */
	
	// from REDIS: JSON:  [{"id":3,"ts":"1397644709256","o":"XYX"},{"id":4,"ts":"1397644709259","o":"FU","n":"SNA"}]
	
	// {"3":{"id":3,"o":"XYX"},"4":{"id":4,"o":"FU","n":"SNA"}}
	
	// from Jackson:  {"id":54,"ts":1397602986240,"n":"FooBar","o":"Um05dlFtRnk="}
//	public long getId();
//	public long getCreatedTimestamp();
//	public String getName();
//	public byte[] getOpaqueKey();
	
	
}
