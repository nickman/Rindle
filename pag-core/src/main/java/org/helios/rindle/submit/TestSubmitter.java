/**
 * 
 */
package org.helios.rindle.submit;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.helios.rindle.control.RindleMain;
import org.helios.rindle.util.SystemClock;

/**
 * <p>Title: TestSubmitter</p>
 * <p>Description: Test command line submitter</p>
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><b><code>org.helios.rindle.submit.TestSubmitter</code></b>
 */

public class TestSubmitter {
	public static final Logger log = LogManager.getLogger(TestSubmitter.class);
	
	public static void main(String[] args) {
		log.info("TestSubmitter Starting...");
		RindleMain rm = RindleMain.getInstance();
		rm.getIstore().purge();
		ISubmit sub = rm.getSubmitter();
		String[] sampleKeys = new String[]{"webserver-", "total-cpu#"};
		Random r = new Random(System.currentTimeMillis());
		int each = 10;
		while(true) {
			for(int i = 0; i < each; i++) {
				StringBuilder b = new StringBuilder(sampleKeys[0]).append(i);
				int length = b.length();
				for(int x = 0; x < each; x++) {
					b.append("/").append(sampleKeys[1]).append(x);
					sub.submit(b.toString(), Math.abs(r.nextInt(100)));
					log.info("Submitted for [{}]", b.toString());
					b.setLength(length);
				}
				
				
			}
			SystemClock.sleep(3000);
		}
	}
}
