package fr.tse.lt2c.satin;

import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * Unit Tests for the class DataWorkers
 * @author Antoine Lavignotte
 *
 */
public class DataWorkersTest extends TestCase {

	/**
	 * timecodeToSeconds tests
	 */
	@Test
	public void testTimecodeToSeconds() {
		DataWorkers dw = new DataWorkers();
		double result = dw.timecodeToSeconds("0:0:0:12");
		assertEquals(0.12, result);
		result = dw.timecodeToSeconds("0:0:10:12");
		assertEquals(10.12, result);
		result = dw.timecodeToSeconds("0:2:10:12");
		assertEquals(130.12, result);
		result = dw.timecodeToSeconds("3:2:10:12");
		assertEquals(10930.12, result);
	}
}
