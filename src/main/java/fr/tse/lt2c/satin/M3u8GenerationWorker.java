package fr.tse.lt2c.satin;

import org.bson.types.ObjectId;
import org.gearman.GearmanFunction;
import org.gearman.GearmanFunctionCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class M3u8GenerationWorker extends DataWorkers implements GearmanFunction {

	private static final Logger logger = LoggerFactory.getLogger(M3u8GenerationWorker.class);
	
	private ObjectId m_movieId;
	private String m_fileName;

	@Override
	public byte[] work(String function, byte[] data, GearmanFunctionCallback callback)
			throws Exception {

		try {
			// File m3u8 generation
			File tradM3u8 = new File(super.m_pathFolderVideos + "/" + ))
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}

		return null;

	}
}
