package fr.tse.lt2c.satin;

import java.io.File;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.video.processing.shotdetector.ShotBoundary;
import org.openimaj.video.processing.shotdetector.ShotDetectedListener;
import org.openimaj.video.processing.shotdetector.VideoKeyframe;
import org.openimaj.video.timecode.VideoTimecode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class Listener
 * Used automatically when a shot is detected
 * @author Antoine Lavignotte
 *
 */
public class Listener extends ShotDetectionWorker implements ShotDetectedListener<MBFImage> {

	/** Logger */
	private static final Logger logger = LoggerFactory.getLogger(Listener.class);
	
	public Listener() {		
	}
	
	/**
	 * Not used for now
	 */
	@Override
	public void differentialCalculated(VideoTimecode vt, double d,
			MBFImage t) {
		// Not used for now	
	}

	/**
	 * Automatically called by ShotDetectionWorker when a shot is detected
	 * Make a copy of the image detected as a shot
	 */
	@Override
	public void shotDetected(ShotBoundary<MBFImage> sb,
			VideoKeyframe<MBFImage> vk) {
		try {
			logger.debug("Shot detected");
			
			File outputFile = new File(this.m_pathFolderShotImages + "/" + m_fileName + "/" 
			+ m_shotDetectionDate + "/" + m_fileName + "_" + sb.toString() + ".png");
			
			ImageUtilities.write(vk.getImage(), "png", outputFile);
		}
		catch(Exception e) {
			logger.error("BUG: {}", e);
		}
		
	}
}
