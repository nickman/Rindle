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
package org.helios.rindle.period.impl;

import java.util.concurrent.ConcurrentHashMap;

import org.helios.rindle.Constants;
import org.helios.rindle.period.IPeriod;
import org.helios.rindle.util.ConfigurationHelper;

/**
 * <p>Title: PeriodImpl</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.period.impl.PeriodImpl</code></p>
 */

public class PeriodImpl implements IPeriod {
	/** A cache of all the created periods */
	private static final ConcurrentHashMap<Integer, IPeriod> PERIODS = new ConcurrentHashMap<Integer, IPeriod>(128); 	
	/** The period in s. */
	public final int period;
	
	/** The minimum period granularity */
	private static final int periodGranularity;
	/** The maximum allowed period */
	private static final int maxPeriod;
	
	static {
		periodGranularity = ConfigurationHelper.getIntSystemThenEnvProperty(Constants.PERIOD_MIN_GRANULARITY, Constants.DEFAULT_PERIOD_MIN_GRANULARITY);
		maxPeriod = ConfigurationHelper.getIntSystemThenEnvProperty(Constants.PERIOD_MAX, Constants.DEFAULT_PERIOD_MAX);
	}
	
	/**
	 * Returns the {@link IPeriod} for the passed period value, possibly adjusted for the system period granularity minimums.
	 * @param period The number of seconds to get the period for
	 * @return the {@link IPeriod}
	 */
	public static IPeriod getPeriod(int period) {
		int _period = round(period);
		IPeriod iperiod = PERIODS.get(_period);
		if(iperiod==null) {
			synchronized(PERIODS) {
				iperiod = PERIODS.get(_period);
				if(iperiod==null) {
					iperiod = new PeriodImpl(_period);
					PERIODS.put(_period, iperiod);
				}
			}
		}
		return iperiod;
	}
	
	/**
	 * Rounds the passed period to the next highest period granularity
	 * @param period The period to round
	 * @return the rounded up period
	 */
	public static int round(int period) {
		if(period < 1) throw new IllegalArgumentException("Invalid period [" + period + "]. Period must be at least 1 (and rounded to " + periodGranularity + ")");
		if(period > maxPeriod) throw new IllegalArgumentException("Invalid period [" + period + "]. Period must be equal to or less than " + maxPeriod);		
		int mod = period%periodGranularity;
		if(mod==0) return period;
		return periodGranularity-mod + period;
	}
	
	/**
	 * Creates a new PeriodImpl
	 * @param period the period in s.
	 */
	private PeriodImpl(int period) {
		this.period = period;
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.period.IPeriod#getPeriod()
	 */
	@Override
	public int getPeriod() {		
		return period;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.period.IPeriod#getPeriodGranularity()
	 */
	@Override
	public int getPeriodGranularity() {
		return periodGranularity;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.period.IPeriod#getMaxPeriod()
	 */
	@Override
	public int getMaxPeriod() {
		return maxPeriod;
	}


}
