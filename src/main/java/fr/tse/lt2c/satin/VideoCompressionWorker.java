package fr.tse.lt2c.satin;

import java.io.InputStream;

import org.bson.types.ObjectId;

import org.gearman.GearmanFunction;
import org.gearman.GearmanFunctionCallback;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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
	private ObjectId m_movieId;
	private JSONArray m_arrayShot;
	private JSONArray m_shotDuration;
	private String m_fileName;
	private String m_fileExtension;
	private String m_videoName;
	private String m_folderMovieCompressed;
	private String m_folderTraditional;
	private String m_folderAdapted;

	@Override
	public byte[] work(String function, byte[] data, GearmanFunctionCallback callback)
			throws Exception {

		try {
			logger.info("IN VIDEOCOMPRESSIONWORKER");

			// Data received
			this.m_dataJson = super.dataToJSON(data);
			this.m_movieId = new ObjectId((String) this.m_dataJson.get("_id"));

			// Debug
			logger.debug("movieId: {}", this.m_movieId);

			// Find the list of shots in the DB
			String listShot = super.findInMongoDB("_id", this.m_movieId, "listShot");
			Object obj = JSONValue.parse(listShot);
			this.m_arrayShot = (JSONArray) obj;

			// Debug
			logger.debug("listShot: {}", this.m_arrayShot.toJSONString());

			// Remove fade shots
			for(int i=this.m_arrayShot.size()-1; i>=0; i--) {
				if(this.m_arrayShot.get(i).toString().startsWith("Fade")) {
					this.m_arrayShot.remove(i);
				}
			}

			// Debug
			logger.debug("listShot: {}", this.m_arrayShot.toJSONString());

			/*
			 *  Create a JSON list of Shot's duration
			 */

			this.createShotDurationArray();

		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
		return null;
	}

	/**
	 * Create a JSONArray of shot's duration and send it to the database
	 * @param listShot
	 */
	@SuppressWarnings("unchecked")
	private void createShotDurationArray() {
		try {
			logger.info("IN CREATESHOTDURATIONARRAY");

			this.m_shotDuration = new JSONArray();

			for(int i=0; i<this.m_arrayShot.size()-1; i++) {
				double shotDuration = super.timecodeToSeconds(this.m_arrayShot.get(i+1).toString()) - super.timecodeToSeconds(this.m_arrayShot.get(i).toString()); 

				logger.debug("Duration for the Shot {}: {}", i+1, shotDuration);

				this.m_shotDuration.add(super.roundDouble(shotDuration, 2));
			}

			// Debug
			logger.debug("m_shotDuration: {}", this.m_shotDuration.toJSONString());
			logger.debug("Size of m_listShot: {}", this.m_arrayShot.size());
			logger.debug("Size of m_shotDuration: {}", this.m_shotDuration.size());

			// Save it in the database
			super.addInMongoDB("_id", this.m_movieId, "shotDuration", this.m_shotDuration);

			// Find the video name
			this.m_fileName = super.findInMongoDB("_id", this.m_movieId, "fileName");
			this.m_fileExtension = super.findInMongoDB("_id", this.m_movieId, "fileExtension");
			this.m_videoName =  this.m_fileName + "." + this.m_fileExtension;

			// Create the folders if they don't exist yet
			this.m_folderMovieCompressed = this.m_pathFolderVideos + "/" + this.m_fileName;
			super.createFolder(this.m_folderMovieCompressed);
			this.m_folderTraditional = this.m_folderMovieCompressed + "/traditional";
			super.createFolder(this.m_folderTraditional);
			this.m_folderAdapted = this.m_folderMovieCompressed + "/adapted";
			super.createFolder(this.m_folderAdapted);

			// Call the traditional bash
			this.traditionalCompression();
			
			//Call the bash with ShotDetection adaptation
			this.adaptedCompression();
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}

	}

	/**
	 * Call the traditional conversion for HTTP Live Streaming (quality adaptation every 10 seconds)
	 */
	private void traditionalCompression() {
		try {
			this.bash(super.m_pathFolderVideos + "/" + this.m_videoName,
					"0",
					"10",
					"1280",
					"960",
					"4500",
					this.m_folderTraditional + "/" + this.m_fileName + "_trad_0.ts");
			this.bash(super.m_pathFolderVideos + "/" + this.m_videoName,
					"10",
					"10",
					"1280",
					"960",
					"1500",
					this.m_folderTraditional + "/" + this.m_fileName + "_trad_1.ts");
			this.bash(super.m_pathFolderVideos + "/" + this.m_videoName,
					"20",
					"640",
					"480",
					"600",
					this.m_folderTraditional + "/" + this.m_fileName + "_trad_2.ts");
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
	}
	
	/**
	 * Bash command to launch the compression
	 * @param videoPath
	 * @param beginTime
	 * @param xResolution
	 * @param yResolution
	 * @param bitrate
	 * @param destination
	 */
	private void bash(String videoPath, String beginTime, String shotDuration, String xResolution, String yResolution, String bitrate, String destination) {
		try {
			logger.info("IN BASH");

			String cmd = "ffmpeg -threads 4 -i " + videoPath +
					" -ss " + beginTime + 
					" -t " + shotDuration +
					" -s " + xResolution + "x" + yResolution +
					" -b " + bitrate + "k " +
					" " + destination;
			// Debug
			logger.debug("bash command: {}", cmd);

			// Call the bash
			this.execBash(cmd);

		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
	}
	
	/**
	 * Bash command to launch the compression
	 * @param videoPath
	 * @param beginTime
	 * @param xResolution
	 * @param yResolution
	 * @param bitrate
	 * @param destination
	 */
	private void bash(String videoPath, String beginTime, String xResolution, String yResolution, String bitrate, String destination) {
		try {
			logger.info("IN BASH");

			String cmd = "ffmpeg -threads 4 -i " + videoPath +
					" -ss " + beginTime + 
					" -s " + xResolution + "x" + yResolution +
					" -b " + bitrate + "k " +
					" " + destination;
			// Debug
			logger.debug("bash command: {}", cmd);

			// Call the bash
			this.execBash(cmd);
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
	}

	/**
	 * Execute the bash command
	 * @param cmd String of the command to to execute
	 */
	@SuppressWarnings("unused")
	private void execBash(String cmd) {
		try {
			logger.info("IN EXECBASH");

			ProcessBuilder pb = new ProcessBuilder("zsh", "-c", cmd);
			pb.redirectErrorStream(true);
			Process shell = pb.start();
			InputStream shellIn = shell.getInputStream();
			int shellExitStatus = shell.waitFor();
			int c;
			while((c = shellIn.read()) != -1) {
				System.out.write(c);
			}
			
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
			
		}
	}
	
	private void adaptedCompression() {
		
		int shotToUse_1 = 0;
		while(super.timecodeToSeconds(this.m_arrayShot.get(shotToUse_1).toString()) <= 10) {
			shotToUse_1++;
		}
		
		this.bash(
				super.m_pathFolderVideos + "/" + this.m_videoName, 
				"0", 
				String.valueOf(super.timecodeToSeconds(this.m_arrayShot.get(shotToUse_1).toString())), 
				"1280",
				"720", 
				"4500", 
				this.m_folderAdapted + "/" + this.m_videoName + "_adapt_0.ts" 
				);
		
		int shotToUse_2 = shotToUse_1;
		while(super.timecodeToSeconds(this.m_arrayShot.get(shotToUse_2).toString()) <= 20) {
			shotToUse_2++;
		}
		
		double durationShot2 =  
				super.timecodeToSeconds(this.m_arrayShot.get(shotToUse_2).toString()) - 
				super.timecodeToSeconds(this.m_arrayShot.get(shotToUse_1).toString());
		
		this.bash(
				super.m_pathFolderVideos + "/" + this.m_videoName, 
				String.valueOf(super.timecodeToSeconds(this.m_arrayShot.get(shotToUse_1).toString())), 
				String.valueOf(durationShot2), 
				"1280",
				"720", 
				"1500", 
				this.m_folderAdapted + "/" + this.m_videoName + "_adapt_1.ts" 
				);
		
		this.bash(
				super.m_pathFolderVideos + "/" + this.m_videoName, 
				String.valueOf(super.timecodeToSeconds(this.m_arrayShot.get(shotToUse_2).toString())), 
				"640",
				"360", 
				"600", 
				this.m_folderAdapted + "/" + this.m_videoName + "_adapt_2.ts" 
				);
		
	}
}
