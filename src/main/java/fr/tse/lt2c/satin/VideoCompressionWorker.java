package fr.tse.lt2c.satin;

import org.gearman.GearmanFunction;
import org.gearman.GearmanFunctionCallback;

import org.json.simple.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class VideoCompressionWorker
 * This class prepare the movies to the video test
 * @author Antoine Lavignotte
 *
 */
public class VideoCompressionWorker extends DataWorkers implements GearmanFunction {

	private static final Logger logger = LoggerFactory.getLogger(VideoCompressionWorker.class);
	
	private JSONObject m_dataJson;
	private String m_movieId;
	private String m_listShot;
	
	@Override
	public byte[] work(String function, byte[] data, GearmanFunctionCallback callback)
			throws Exception {
		
		try {
			logger.info("IN VIDEOCOMPRESSIONWORKER");
			
			// Data received
			this.m_dataJson = super.dataToJSON(data);
			this.m_movieId = (String) this.m_dataJson.get("_id");
			
			// Debug
			logger.debug("movieId: {}", this.m_movieId);
			
			// Find the list of shots in the DB
			this.m_listShot = super.findInMongoDB("_id", this.m_dataJson, "listShot");
			
			// Debug
			logger.debug("listShot: {}", this.m_listShot);
			
			
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
		return null;
	}

}
