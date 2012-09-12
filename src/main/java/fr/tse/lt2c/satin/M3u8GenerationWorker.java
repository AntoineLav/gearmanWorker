package fr.tse.lt2c.satin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.bson.types.ObjectId;
import org.gearman.GearmanFunction;
import org.gearman.GearmanFunctionCallback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.util.JSON;

public class M3u8GenerationWorker extends DataWorkers implements GearmanFunction {

	private static final Logger logger = LoggerFactory.getLogger(M3u8GenerationWorker.class);

	private JSONObject m_dataJson;
	private ObjectId m_movieId;
	private String m_fileName;
	private File m_tradM3u8;
	private File m_adaptM3u8;
	private int m_shotToUse1;
	private int m_shotToUse2;
	private JSONArray m_listShot;
	private JSONArray m_shotDuration;

	@Override
	public byte[] work(String function, byte[] data, GearmanFunctionCallback callback)
			throws Exception {

		try {

			logger.info("IN M3U8GENERATIONWORKER");

			// Data received
			this.m_dataJson = super.dataToJSON(data);
			this.m_movieId =  new ObjectId((String) this.m_dataJson.get("_id"));
			this.m_fileName = super.findInMongoDB("_id", this.m_movieId, "fileName");

			// Debug
			logger.debug("movieId: {}", this.m_movieId);

			// File m3u8 generation (traditional)
			this.m_tradM3u8 = new File(super.m_pathFolderVideos + "/" + this.m_fileName + "/" + this.m_fileName + "_trad.m3u8");
			this.m_tradM3u8.createNewFile();

			// File initialization
			this.fileInitialization(this.m_tradM3u8);
			this.tradFinalization(this.m_tradM3u8);

			// File m3u8 generation (adapted)
			this.m_adaptM3u8 = new File(super.m_pathFolderVideos + "/" + this.m_fileName + "/" + this.m_fileName + "_adapt.m3u8");
			this.m_adaptM3u8.createNewFile();

			// File initialization
			this.fileInitialization(this.m_adaptM3u8);
			this.adaptFinalization(this.m_tradM3u8);


		}
		catch(Exception e) {

		}

		return null;
	}

	/**
	 * M3U8 Initialization
	 * @param path of the m3u8 file
	 */
	private void fileInitialization(File path) {
		try {

			logger.info("IN FILEINITIALIZATION");

			BufferedWriter writer = null;

			String line1 = "#EXTM3U";
			String line2 = "#EXT-X-PLAYLIST-TYPE:VOD";
			String line3 = "#EXT-X-TARGETDURATION:30";
			String line4 = "#EXT-X-VERSION:3";
			String line5 = "#EXT-X-MEDIA-SEQUENCE:0";

			writer = new BufferedWriter(new FileWriter(this.m_tradM3u8.toString(), true));
			writer.write(line1,0,line1.length());
			writer.newLine();
			writer.write(line2,0,line2.length());
			writer.newLine();
			writer.write(line3,0,line3.length());
			writer.newLine();
			writer.write(line4,0,line4.length());
			writer.newLine();
			writer.write(line5,0,line5.length());
			writer.newLine();

			if(writer != null) {
				writer.close();
			}
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
	}

	private void tradFinalization(File path) {
		try {

			logger.info("IN TRADFINALIZATION");

			BufferedWriter writer = null;

			String line1 = "#EXTINF:10.0,";
			String line2 = "http://hebergvideo.dev/" + this.m_fileName + "/traditional/" + this.m_fileName + "_trad_0.ts";
			String line3 = "#EXTINF:10.0,";
			String line4 = "http://hebergvideo.dev/" + this.m_fileName + "/traditional/" + this.m_fileName + "_trad_1.ts";
			String line5 = "#EXTINF:10.0,";
			String line6 = "http://hebergvideo.dev/" + this.m_fileName + "/traditional/" + this.m_fileName + "_trad_2.ts";
			String line7 = "#EXT-X-ENDLIST";

			writer = new BufferedWriter(new FileWriter(this.m_tradM3u8.toString(), true));
			writer.write(line1,0,line1.length());
			writer.newLine();
			writer.write(line2,0,line2.length());
			writer.newLine();
			writer.write(line3,0,line3.length());
			writer.newLine();
			writer.write(line4,0,line4.length());
			writer.newLine();
			writer.write(line5,0,line5.length());
			writer.newLine();
			writer.write(line6,0,line6.length());
			writer.newLine();
			writer.write(line7,0,line7.length());
			writer.newLine();

			if(writer != null) {
				writer.close();
			}
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
	}
	
	private void adaptFinalization(File path) {
		try {

			logger.info("IN ADAPTFINALIZATION");

			BufferedWriter writer = null;
			
			this.m_shotToUse1 = Integer.parseInt(super.findInMongoDB("_id", this.m_movieId, "shotToUse1"));
			this.m_shotToUse2 = Integer.parseInt(super.findInMongoDB("_id", this.m_movieId, "shotToUse2"));
			Object obj = JSON.parse(super.findInMongoDB("_id", this.m_movieId, "listShot"));
			this.m_listShot = (JSONArray) obj;
			this.m_shotDuration = (JSONArray) JSON.parse(super.findInMongoDB("_id", this.m_movieId, "listShot"));
			
			String line1 = "#EXTINF:" + super.timecodeToSeconds(this.m_listShot.get(this.m_shotToUse1).toString()) + ",";
			String line2 = "http://hebergvideo.dev/" + this.m_fileName + "/adapted/" + this.m_fileName + "_adapt_0.ts";
			
			double durationShot2 = 
					super.timecodeToSeconds(this.m_listShot.get(this.m_shotToUse2).toString()) -
					super.timecodeToSeconds(this.m_listShot.get(this.m_shotToUse1).toString());
			
			String line3 = "#EXTINF:" + durationShot2 + ",";
			String line4 = "http://hebergvideo.dev/" + this.m_fileName + "/adapted/" + this.m_fileName + "_adapt_1.ts";
			
			double durationLastPart = 30 - super.timecodeToSeconds(this.m_listShot.get(this.m_shotToUse2).toString());
			
			String line5 = "#EXTINF:" + durationLastPart + ",";
			String line6 = "http://hebergvideo.dev/" + this.m_fileName + "/adapted/" + this.m_fileName + "_adapt_2.ts";
			String line7 = "#EXT-X-ENDLIST";

			writer = new BufferedWriter(new FileWriter(this.m_tradM3u8.toString(), true));
			writer.write(line1,0,line1.length());
			writer.newLine();
			writer.write(line2,0,line2.length());
			writer.newLine();
			writer.write(line3,0,line3.length());
			writer.newLine();
			writer.write(line4,0,line4.length());
			writer.newLine();
			writer.write(line5,0,line5.length());
			writer.newLine();
			writer.write(line6,0,line6.length());
			writer.newLine();
			writer.write(line7,0,line7.length());
			writer.newLine();

			if(writer != null) {
				writer.close();
			}
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
	}
}
