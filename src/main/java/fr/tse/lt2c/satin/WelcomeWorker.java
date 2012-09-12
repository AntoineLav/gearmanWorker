package fr.tse.lt2c.satin;

import java.net.UnknownHostException;

import org.gearman.Gearman;
import org.gearman.GearmanServer;
import org.gearman.GearmanWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class WelcomeWorker extends DataWorkers {

	private static final Logger logger = LoggerFactory.getLogger(WelcomeWorker.class);

	/** The worker's names */
	private static final String m_FUNCTION_NAME_1 = "shotDetection";
	private static final String m_FUNCTION_NAME_2 = "deleteMovie";
	private static final String m_FUNCTION_NAME_3 = "videoCompression";
	//public static final String FUNCTION_NAME_3 = "m3u8Generation";

	/** The host address of the job server */
	private static final String m_HOST = "localhost";

	/** The port number where the job server is listening on */
	private static final int m_PORT = 4730;

	private static Gearman m_gearman;
	private static GearmanServer m_server;
	private static GearmanWorker m_worker;

	/**
	 * @param args
	 * @throws MongoException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException, MongoException {

		try {
			/*
			 * Create a Gearman instance
			 */
			m_gearman = Gearman.createGearman();

			/*
			 * Create the job server object. This call creates an object represents
			 * a remote job server.
			 * 
			 * Parameter 1: the host address of the job server.
			 * Parameter 2: the port number the job server is listening on.
			 * 
			 * A job server receives jobs from clients and distributes them to
			 * registered workers.
			 */
			m_server = m_gearman.createGearmanServer(
					m_HOST, m_PORT);

			/*
			 * Create a gearman worker. The worker poll jobs from the server and
			 * executes the corresponding GearmanFunction
			 */
			m_worker = m_gearman.createGearmanWorker();

			/*
			 *  Tell the worker how to perform the ShotDetection function
			 */
			m_worker.addFunction(m_FUNCTION_NAME_1, new ShotDetectionWorker());
			m_worker.addFunction(m_FUNCTION_NAME_2, new DeleteMovieWorker());
			m_worker.addFunction(m_FUNCTION_NAME_3, new VideoCompressionWorker());

			/*
			 *  Tell the worker that it may communicate with the job server
			 */
			m_worker.addServer(m_server);

			/*
			 * Create a new gearman client.
			 * 
			 * The client is used to submit requests the job server.
			 */
			m_client = m_gearman.createGearmanClient();

			/*
			 * Tell the client that it may connect to this server when submitting
			 * jobs.
			 */
			m_client.addServer(m_server);

			/*
			 * Define the mongoDB connection 
			 */
			m_m = new Mongo("localhost");
			m_db = m_m.getDB("shot_detector");
			m_coll = m_db.getCollection("movies");
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
	}


}
