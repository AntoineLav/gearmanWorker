package fr.tse.lt2c.satin;

import java.io.File;

import org.gearman.GearmanClient;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

/**
 * Class DataWorkers
 * This class contains all data that can be used by workers
 * @author Antoine Lavignotte
 *
 */
public class DataWorkers {

	/** Gearman parameters */
	protected static GearmanClient m_client;

	/** MongoDB parameters */
	protected static Mongo m_m;
	protected static DB m_db;
	protected static DBCollection m_coll;
	protected DBCursor m_cur;

	/** Logger */
	private static final Logger logger = LoggerFactory.getLogger(DataWorkers.class);

	/** Variables */
	/*
	 * Change the folder's path as you prefer
	 */
	protected String m_pathFolderVideos = "/Users/Antoine/Sites/HebergVideo";
	protected String m_pathFolderShotImages = this.m_pathFolderVideos + "/VideoShotImages";


	/**
	 * Constructor
	 */
	public DataWorkers() {	
	}

	/**
	 * Convert the data received into a JSONObject
	 * 
	 * @param data	The data received by the worker
	 * @return data_jsonobject A JSONObject of data
	 */
	protected JSONObject dataToJSON(byte[] data) {
		try{
			// Info
			logger.info("IN METHOD dataToJSON");

			String data_string = new String(data);
			logger.debug("String before conversion into JSONArray: {}", data_string);
			Object obj = JSONValue.parse(data_string);
			JSONObject data_jsonobject = (JSONObject) obj;

			// Debug
			logger.debug("JSONObject: {}", data_jsonobject.toJSONString());

			return data_jsonobject;
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
			return null;
		}	
	}

	/**
	 * Find the Video file name from http address 
	 * @param httpAddress Sent by the admin from the web page
	 * @return fileName String
	 */
	protected String findFileName(String httpAddress) {
		try {
			logger.info("IN FINDFILENAME");
			String[] nameAndExtension = cutHttpAddress(httpAddress);
			String fileName = nameAndExtension[0];
			return fileName;
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
			return null;
		}
	}

	/**
	 * Find the Video file extension from http address
	 * @param httpAddress Sent by the admin from the web page
	 * @return fileExtension String
	 */
	protected String findFileExtension(String httpAddress) {
		try {
			logger.info("IN FINDFILEEXTENSION");
			String[] nameAndExtension = cutHttpAddress(httpAddress);
			String fileExtension = nameAndExtension[1];
			return fileExtension;
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
			return null;
		}
	}

	/**
	 * Find the last parameters from an http addres (e.g: "thor.mp4" from "http://www.videotest.com/thor.mp4")
	 * @param httpAddress Sent by the admin from the web page
	 * @return fileSeparated[] Return a tab with two parameters: the file name and the extension 
	 */
	private String[] cutHttpAddress(String httpAddress) {
		try {
			logger.info("IN CUTHTTPADDRESS");
			String params[] = httpAddress.split("/");
			String fileName = params[params.length-1];
			String fileSeparated[] = fileName.split("\\.");
			return fileSeparated;
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
			return null;
		}
	}

	/**
	 * Create a folder
	 * @param path Path of the folder to create
	 */
	protected void createFolder(String path) {
		try{
			logger.info("IN CREATEFOLDER");
			File folder = new File(path);
			if(!folder.exists()){
				folder.mkdirs();
			}	
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}	
	}

	/**
	 * Delete a folder
	 * @param path Where the directory is
	 */
	protected void deleteFolder(String path) {
		try {
			logger.info("IN DELETEFOLDER");
			File folder = new File(path);
			if(folder.exists()){
				deleteDir(folder);
			}
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
	}

	/**
	 * Navigate in the directory to find children and to delete them
	 * @param dir Directory you want to delete
	 * @return boolean
	 */
	private static boolean deleteDir(File dir) {
		try {
			logger.info("IN DELETEDIR");
			if (dir.isDirectory()) {
				String[] children = dir.list();
				for (int i=0; i<children.length; i++) {
					boolean success = deleteDir(new File(dir, children[i]));
					if (!success) {
						return false;
					}
				}
			}
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	/**
	 * Find an information in the database MongoDB
	 * @param where Name of the variable used in 'where' research
	 * @param value Value of the variable used in 'where' research
	 * @param what Variable searched
	 * @return result String which contains the find result
	 */
	protected String findInMongoDB(String where, Object whereValue, String what) {
		try {
			logger.info("IN FINDINMONGODB");
			this.m_cur = m_coll.find(new BasicDBObject(where, whereValue), new BasicDBObject(what, 1));
			String result = null;
			while(this.m_cur.hasNext()) {
				result = this.m_cur.next().get(what).toString();
			}
			logger.debug("Information founded: {}", result);
			return result;
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
			return null;
		}
	}

	/**
	 * Add an information in the database MongoDB
	 * @param where Name of the variable used in 'where' research
	 * @param where Value Value of the variable used in 'where' research
	 * @param what Name of the variable to add
	 * @param whatValue Value of the variable to add
	 */
	protected void addInMongoDB(String where, Object whereValue, String what, Object whatValue) {
		try {
			logger.info("IN ADDINMONGODB");
			m_coll.update(new BasicDBObject(where, whereValue), new BasicDBObject("$set", new BasicDBObject(what, whatValue)));

		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
	}
	
	protected void deleteInMongoDB(String where, Object whereValue) {
		try {
			logger.info("IN DELETEINMONGODB");
			m_coll.remove(new BasicDBObject(where, whereValue));
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
	}

	/**
	 * Display in the logger all the database
	 */
	protected void collectionDisplay() {
		try {
			logger.info("IN COLLECTIONDISPLAY");
			this.m_cur = m_coll.find();
			int i=1;
			while(this.m_cur.hasNext()) {
				logger.debug("Data {}: {}", i, this.m_cur.next());
				i++;
			}
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
	}
}
