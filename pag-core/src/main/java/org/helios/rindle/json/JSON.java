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
package org.helios.rindle.json;

import org.helios.rindle.metric.IMetricDefinition;
import org.helios.rindle.metric.MetricSerialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * <p>Title: JSON</p>
 * <p>Description: Centralized JSON utilities</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.json.JSON</code></p>
 */

public class JSON {
	/** The pre-configured and shareable JSON Object Mapper */
	public static final ObjectMapper MAP = new ObjectMapper();
	
	/** The json node factory */
	public static final JsonNodeFactory FACTORY = new JsonNodeFactory(false);

	
	
	static {
		SimpleModule module = new SimpleModule();
		module.addSerializer(IMetricDefinition.class, new MetricSerialization.UnsafeMetricDefinitionSerializer());
		module.addDeserializer(IMetricDefinition.class, new MetricSerialization.UnsafeMetricDefinitionDeserializer());
		MAP.registerModule(module);
	}
	
	private JSON() {
	}

}
