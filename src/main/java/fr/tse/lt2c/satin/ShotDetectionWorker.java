package fr.tse.lt2c.satin;

import org.gearman.GearmanFunction;
import org.gearman.GearmanFunctionCallback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
import org.openimaj.video.processing.shotdetector.ShotBoundary;
import org.openimaj.video.processing.shotdetector.VideoShotDetector;
import org.openimaj.video.xuggle.XuggleVideo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.List;


/**
 * Worker for shot detection
 * (called by a Gearman call named "shotDetection")
 * 
 * @author Antoine Lavignotte
 * 
 */
public class ShotDetectionWorker extends DataWorkers implements GearmanFunction {

	private static final Logger logger = LoggerFactory.getLogger(ShotDetectionWorker.class);

	private JSONObject m_dataReceived;
	private String m_videoTitle;
	private String m_httpAddress;
	protected static String m_fileName;
	private String m_fileExtension;
	protected static String m_shotDetectionDate;
	private JSONArray m_listShotJson;

	@Override
	public byte[] work(String function, byte[] data, GearmanFunctionCallback callback)
			throws Exception {

		try {
			// Info
			logger.info("IN SHOT_DETECTION_WORKER");

			// Debug
			logger.debug("Number of documents in the database: {}", m_coll.getCount());

			// Data received conversion into JSONArray
			this.m_dataReceived = this.dataToJSON(data);

			// variable initialization
			this.m_videoTitle = (String) this.m_dataReceived.get("title");
			logger.debug("m_videoName: {}", this.m_videoTitle);
			this.m_httpAddress = (String) this.m_dataReceived.get("httpAddress");
			logger.debug("m_httpAddress: {}", this.m_httpAddress);

			// Find the file name
			m_fileName = super.findFileName(this.m_httpAddress);
			logger.debug("m_fileName: {}", m_fileName);

			// Find the file extension
			this.m_fileExtension = super.findFileExtension(this.m_httpAddress);
			logger.debug("m_fileExtension: {}", this.m_fileExtension);

			// Create the folders to stock the shot images
			this.createMovieFolder();

			// Add the pathFolderShotImages to the DB
			super.addInMongoDB("title", this.m_videoTitle, "pathFolderShotImages", this.m_pathFolderShotImages);

			// Video analysis
			this.m_listShotJson = xugglerWork(this.m_pathFolderVideos + "/" + m_fileName + "." + this.m_fileExtension);
			
			// Insert listShotJSON into mongoDB
			super.addInMongoDB("title", this.m_videoTitle, "listShot", this.m_listShotJson);
			
			// Add the ShotDetection Field (for the web interface), the fileName & the extension in the DB
			super.addInMongoDB("title", this.m_videoTitle, "shotDetection", true);
			super.addInMongoDB("title", this.m_videoTitle, "fileName", m_fileName);
			super.addInMongoDB("title", this.m_videoTitle, "fileExtension", this.m_fileExtension);
			
			// Debug
			super.collectionDisplay();
			
			// Call a new Job to compress the video
			callJobVideoCompression();

		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}

		return null;
	}

	/**
	 * Method which create all the necessaries folders to stock the shot images founded
	 */
	private void createMovieFolder() {
		try {
			logger.info("IN CREATEMOVIEFOLDER");

			// Create the principal image folder to stock the shot images
			super.createFolder(this.m_pathFolderShotImages);

			// Create the movie folder in the previous one
			super.createFolder(this.m_pathFolderShotImages + "/" + m_fileName);

			// Create a folder with the actual date as name to distinguish different 
			// shot detection on a same video
			findDate();
			super.createFolder(this.m_pathFolderShotImages + "/" + m_fileName 
					+ "/" + m_shotDetectionDate);
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
	}

	/**
	 * Found the actual date and cast it to create a folder name
	 * The date is stored in the variable "m_shotDetectionDate"
	 */
	private void findDate() {
		try {
			logger.info("IN FINDDATE");
			String date = new Date().toString().substring(0, 20);
			date = date.trim();
			m_shotDetectionDate = date.replace(" ", "_");
			logger.debug("Date founded: {}", m_shotDetectionDate);
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
	}
	
	private JSONArray xugglerWork(String videoPath) {
		try {
			logger.info("IN XUGGLERWORK");
			
			// Video Instantiation
			Video<MBFImage> video = new XuggleVideo(new File(videoPath));
			
			// Shot Detector Creation
			VideoShotDetector vsd = new VideoShotDetector(video, false);
			
			// Listener instantiation
			Listener listener = new Listener();
			
			// Parameters for the shotDetection
			vsd.setFindKeyframes(true);
			vsd.addShotDetectedListener(listener);
			vsd.setThreshold(25000);
			vsd.process();
			
			List<ShotBoundary<MBFImage>> listShot = vsd.getShotBoundaries();
			
			// Debug
			for(int i=0; i<listShot.size(); i++) {
				logger.debug("{}", listShot.get(i).toString());
			}
			
			// Convert listShot into a JSONArray
			JSONArray listShotJson = listShotToJsonArray(listShot);
			
			return listShotJson;
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
			return null;
		}
	}
	
	/**
	 * Convert a list of Shots into a JSONArray
	 * @param list List of Shots detected
	 * @return JSONArray of Shots detected
	 */
	@SuppressWarnings("unchecked")
	private JSONArray listShotToJsonArray(List<ShotBoundary<MBFImage>> list) {
		try {
			logger.info("IN LISTSHOTTOJSONARRAY");
			JSONArray list_json = new JSONArray();
			for(int i=0; i<list.size(); i++) {
				list_json.add(list.get(i).toString());
			}
			logger.debug("ListShotJson : {}", list_json.toJSONString());
			return list_json;
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
			return null;
		}
	}
	
	/**
	 * Call the job "videoCompression"
	 * Called when Shot Detection is finished
	 */
	@SuppressWarnings("unchecked")
	private void callJobVideoCompression() {
		try {
			logger.info("IN CALLCOMPRESSIONTREATMENT");
			
			// Find the Video Id
			String videoId = super.findInMongoDB("title", this.m_videoTitle, "_id");
			
			// Prepare data to send
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("_id", videoId);
			byte[] dataToSend = jsonObj.toJSONString().getBytes();
			
			// Call the gearman job "videoCompression"
			m_client.submitJob("videoCompression", dataToSend);
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
	}
}
