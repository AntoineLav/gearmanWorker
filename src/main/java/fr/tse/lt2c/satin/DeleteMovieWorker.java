package fr.tse.lt2c.satin;

import org.gearman.GearmanFunction;
import org.gearman.GearmanFunctionCallback;

import org.json.simple.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bson.types.ObjectId;

/**
 * Worker to Delete a movie from the database
 * (called by a Gearman call named "deleteMovie")
 * @author Antoine Lavignotte
 *
 */
public class DeleteMovieWorker extends DataWorkers implements GearmanFunction{

	/** Logger */
	private static final Logger logger = LoggerFactory.getLogger(DeleteMovieWorker.class);

	/** Variables */
	private JSONObject m_dataJson;
	private String m_movieId;
	private String m_videoName;
	private ObjectId m_videoIdOid;

	@Override
	public byte[] work(String function, byte[] data, GearmanFunctionCallback callback)
			throws Exception {

		try {
			logger.info("IN DELETEMOVIEWORKER");

			// Convert data received into a JSONObject
			this.m_dataJson = super.dataToJSON(data);

			// Debug
			logger.debug("dataJson: {}", this.m_dataJson.toJSONString());

			// Find the movie Id from the data received
			this.m_movieId = (String) this.m_dataJson.get("oid");

			// Debug
			logger.debug("movieId: {}", this.m_movieId.toString());

			// Find the video name
			this.m_videoIdOid = new ObjectId(this.m_movieId);
			this.m_videoName = super.findInMongoDB("_id", this.m_videoIdOid, "fileName");

			// Delete the Movie folder in VideoShotImages
			super.deleteFolder(super.m_pathFolderShotImages + "/" + this.m_videoName);
			logger.debug("Dir to delete: {}/{} ", this.m_pathFolderShotImages, this.m_videoName);
			
			// Delete the Movie Compressed folder
			super.deleteFolder(super.m_pathFolderVideos + "/" + this.m_videoName);
			logger.debug("Dir to delete: {}/{} ", super.m_pathFolderVideos, this.m_videoName);
			
			// Delete the movie from the DB
			super.deleteInMongoDB("_id", this.m_videoIdOid);
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
		
		return null;
	}

}
