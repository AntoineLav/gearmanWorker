package fr.tse.lt2c.satin;

import org.gearman.GearmanFunction;
import org.gearman.GearmanFunctionCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class M3u8GenerationWorker extends DataWorkers implements GearmanFunction {

	private static final Logger logger = LoggerFactory.getLogger(M3u8GenerationWorker.class);

	@Override
	public byte[] work(String function, byte[] data, GearmanFunctionCallback callback)
			throws Exception {

		try {
			
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}

		return null;

	}
}
