/**
* Helios Development Group LLC, 2013. 
 *
 */
package org.helios.rindle.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;


/**
 * <p>Title: ConfigurationHelper</p>
 * <p>Description: Configuration helper utilities</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.util.ConfigurationHelper</code></p>
 */
public class ConfigurationHelper {
	/** Empty String aray const */
	public static final String[] EMPTY_STR_ARR = {};
	/** Empty int aray const */
	public static final int[] EMPTY_INT_ARR = {};
	
	/** Comma splitter regex const */
	public static final Pattern COMMA_SPLITTER = Pattern.compile(",");
	
	/** If property names start with this, system properties and environment variables should be ignored. */
	public static final String NOSYSENV = "tsd.";

	/**
	 * Merges the passed properties
	 * @param properties The properties to merge
	 * @return the merged properties
	 */
	public static Properties mergeProperties(Properties...properties) {
		Properties allProps = new Properties(System.getProperties());
		for(int i = properties.length-1; i>=0; i--) {
			if(properties[i] != null && properties[i].size() >0) {
				allProps.putAll(properties[i]);
			}
		}
		return allProps;
	}
	
	
	/**
	 * Looks up a property, first in the environment, then the system properties. 
	 * If not found in either, returns the supplied default.
	 * @param name The name of the key to look up.
	 * @param defaultValue The default to return if the name is not found.
	 * @param properties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return The located value or the default if it was not found.
	 */
	public static String getEnvThenSystemProperty(String name, String defaultValue, Properties...properties) {
		
		String value = System.getenv(name.replace('.', '_'));
		if(value==null) {			
			value = mergeProperties(properties).getProperty(name);
		}
		if(value==null) {
			value=defaultValue;
		}
		return value;
	}
	
	/**
	 * Looks up a property, first in the system properties, then the environment. 
	 * If not found in either, returns the supplied default.
	 * @param name The name of the key to look up.
	 * @param defaultValue The default to return if the name is not found.
	 * @param properties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return The located value or the default if it was not found.
	 */
	public static String getSystemThenEnvProperty(String name, String defaultValue, Properties...properties) {
		if(name==null || name.trim().isEmpty()) throw new IllegalArgumentException("The passed property name was null or empty");
		if(name.trim().toLowerCase().startsWith(NOSYSENV)) {
			if(properties==null || properties.length==0 || properties[0]==null) return defaultValue;
			return properties[0].getProperty(name.trim(), defaultValue);
		}
		String value = mergeProperties(properties).getProperty(name);
		if(value==null) {
			value = System.getenv(name.replace('.', '_'));
		}
		if(value==null) {
			value=defaultValue;
		}
		return value;
	}
	
	/** The default value passed for an empty array */
	public static final String EMPTY_ARRAY_TOKEN = "_org_helios_empty_array_";
	
	/**
	 * Looks up a property and converts to a string array, first in the system properties, then the environment. 
	 * If not found in either, returns the supplied default.
	 * @param name The name of the key to look up.
	 * @param defaultValue The default to return if the name is not found. Expected as a comma separated list of strings
	 * @param properties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return The located value or the default if it was not found.
	 */
	public static String[] getSystemThenEnvPropertyArray(String name, String defaultValue, Properties...properties) {
		if(defaultValue.isEmpty()) defaultValue = EMPTY_ARRAY_TOKEN;
		String raw = getSystemThenEnvProperty(name, defaultValue, properties);
		if(EMPTY_ARRAY_TOKEN.equals(raw)) return EMPTY_STR_ARR; 
		List<String> values = new ArrayList<String>();
		for(String s: COMMA_SPLITTER.split(raw.trim())) {
			if(s.trim().isEmpty()) continue;
			values.add(s.trim());
		}
		return values.toArray(new String[0]);
	}

	/**
	 * Looks up a property and converts to an int array, first in the system properties, then the environment. 
	 * If not found in either, returns the supplied default.
	 * @param name The name of the key to look up.
	 * @param defaultValue The default to return if the name is not found. Expected as a comma separated list of strings
	 * @param properties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return The located value or the default if it was not found.
	 */
	public static int[] getIntSystemThenEnvPropertyArray(String name, String defaultValue, Properties...properties) {
		String raw = getSystemThenEnvProperty(name, defaultValue, properties);
		if(raw==null || raw.trim().isEmpty()) return EMPTY_INT_ARR;
		List<Integer> values = new ArrayList<Integer>();
		for(String s: COMMA_SPLITTER.split(raw.trim())) {
			if(s.trim().isEmpty()) continue;
			try { values.add(new Integer(s.trim())); } catch (Exception ex) {}
		}		
		if(values.isEmpty()) return EMPTY_INT_ARR;
		int[] ints = new int[values.size()];
		for(int i = 0; i < values.size(); i++) {
			ints[i] = values.get(i);
		}
		return ints;
	}
	
	
	/**
	 * Determines if a name has been defined in the environment or system properties.
	 * @param name the name of the property to check for.
	 * @param properties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return true if the name is defined in the environment or system properties.
	 */
	public static boolean isDefined(String name, Properties...properties) {
		if(System.getenv(name) != null) return true;
		if(mergeProperties(properties).getProperty(name) != null) return true;
		return false;		
	}
	
	/**
	 * Determines if a name has been defined as a valid int in the environment or system properties.
	 * @param name the name of the property to check for.
	 * @param properties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return true if the name is defined as a valid int in the environment or system properties.
	 */
	public static boolean isIntDefined(String name, Properties...properties) {
		String tmp = getEnvThenSystemProperty(name, null, properties);
		if(tmp==null) return false;
		try {
			Integer.parseInt(tmp);
			return true;
		} catch (Exception e) {
			return false;
		}				
	}
	
	/**
	 * Determines if a name has been defined as a valid boolean in the environment or system properties.
	 * @param name the name of the property to check for.
	 * @param properties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return true if the name is defined as a valid boolean in the environment or system properties.
	 */
	public static boolean isBooleanDefined(String name, Properties...properties) {
		String tmp = getEnvThenSystemProperty(name, null, properties);
		if(tmp==null) return false;
		try {
			tmp = tmp.toUpperCase();
			if(
					tmp.equalsIgnoreCase("TRUE") || tmp.equalsIgnoreCase("Y") || tmp.equalsIgnoreCase("YES") ||
					tmp.equalsIgnoreCase("FALSE") || tmp.equalsIgnoreCase("N") || tmp.equalsIgnoreCase("NO")
			) return true;
			return false;
		} catch (Exception e) {
			return false;
		}				
	}	
	
	/**
	 * Determines if a name has been defined as a valid long in the environment or system properties.
	 * @param name the name of the property to check for.
	 * @param properties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return true if the name is defined as a valid long in the environment or system properties.
	 */
	public static boolean isLongDefined(String name, Properties...properties) {
		String tmp = getEnvThenSystemProperty(name, null, properties);
		if(tmp==null) return false;
		try {
			Long.parseLong(tmp);
			return true;
		} catch (Exception e) {
			return false;
		}				
	}
	
	/**
	 * Returns the value defined as an Integer looked up from the Environment, then System properties.
	 * @param name The name of the key to lookup.
	 * @param defaultValue The default value to return if the name is not defined or the value is not a valid int.
	 * @param properties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return The located integer or the passed default value.
	 */
	public static Integer getIntSystemThenEnvProperty(String name, Integer defaultValue, Properties...properties) {
		String tmp = getSystemThenEnvProperty(name, null, properties);
		try {
			return Integer.parseInt(tmp);
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	/**
	 * Returns the value defined as an Float looked up from the Environment, then System properties.
	 * @param name The name of the key to lookup.
	 * @param defaultValue The default value to return if the name is not defined or the value is not a valid int.
	 * @param properties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return The located float or the passed default value.
	 */
	public static Float getFloatSystemThenEnvProperty(String name, Float defaultValue, Properties...properties) {
		String tmp = getSystemThenEnvProperty(name, null, properties);
		try {
			return Float.parseFloat(tmp);
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	
	/**
	 * Returns the value defined as a Long looked up from the Environment, then System properties.
	 * @param name The name of the key to lookup.
	 * @param defaultValue The default value to return if the name is not defined or the value is not a valid long.
	 * @param properties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return The located long or the passed default value.
	 */
	public static Long getLongSystemThenEnvProperty(String name, Long defaultValue, Properties...properties) {
		String tmp = getSystemThenEnvProperty(name, null, properties);
		try {
			return Long.parseLong(tmp);
		} catch (Exception e) {
			return defaultValue;
		}
	}	
	
	/**
	 * Returns the value defined as a Boolean looked up from the Environment, then System properties.
	 * @param name The name of the key to lookup.
	 * @param defaultValue The default value to return if the name is not defined or the value is not a valid boolean.
	 * @param properties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return The located boolean or the passed default value.
	 */
	public static Boolean getBooleanSystemThenEnvProperty(String name, Boolean defaultValue, Properties...properties) {
		String tmp = getSystemThenEnvProperty(name, null, properties);
		if(tmp==null) return defaultValue;
		tmp = tmp.toUpperCase();
		if(tmp.equalsIgnoreCase("TRUE") || tmp.equalsIgnoreCase("Y") || tmp.equalsIgnoreCase("YES")) return true;
		if(tmp.equalsIgnoreCase("FALSE") || tmp.equalsIgnoreCase("N") || tmp.equalsIgnoreCase("NO")) return false;
		return defaultValue;
	}
	
	/**
	 * Attempts to create an instance of the passed class using one of:<ol>
	 * 	<li>Attempts to find a Constructor with the passed signature</li>
	 * 	<li>Attempts to find a static factory method called <b><code>getInstance</code></b> with the passed signature</li>
	 * 	<li>Attempts to find a static factory method called <b><code>newInstance</code></b> with the passed signature</li>
	 * </ol>
	 * @param clazz The class to create an instance of
	 * @param sig The signature of the constructor or static factory method
	 * @param args The arguments to the constructor or static factory method
	 * @return The created instance
	 * @throws Exception thrown on any error
	 */
	public static <T> T inst(Class<T> clazz, Class<?>[] sig, Object...args) throws Exception {
		Constructor<T> ctor = null;
		try {
			ctor = clazz.getDeclaredConstructor(sig);
			return ctor.newInstance(args);
		} catch (Exception e) {
			Method method = null;
			try { method = clazz.getDeclaredMethod("getInstance", sig); 
				if(!Modifier.isStatic(method.getModifiers())) throw new Exception();
			} catch (Exception ex) {}
			if(method==null) {
				try { method = clazz.getDeclaredMethod("newInstance", sig); } catch (Exception ex) {}
			}
			if(method==null) throw new Exception("Failed to find Constructor or Static Factory Method for [" + clazz.getName() + "]");
			if(!Modifier.isStatic(method.getModifiers())) throw new Exception("Factory Method [" + method.toGenericString() + "] is not static");
			return (T)method.invoke(null, args);
		}
	}
	
	/** Empty class signature const */
	public static final Class<?>[] EMPTY_SIG = {};
	/** Empty arg const */
	public static final Object[] EMPTY_ARGS = {};
	
	/**
	 * Attempts to create an instance of the passed class using one of:<ol>
	 * 	<li>Attempts to find a Constructor</li>
	 * 	<li>Attempts to find a static factory method called <b><code>getInstance</code></b></li>
	 * 	<li>Attempts to find a static factory method called <b><code>newInstance</code></b></li>
	 * </ol>
	 * @param clazz The class to create an instance of
	 * @return The created instance
	 * @throws Exception thrown on any error
	 */
	public static <T> T inst(Class<T> clazz) throws Exception {
		return inst(clazz, EMPTY_SIG, EMPTY_ARGS);
	}

}
