package es.upm.dit.gsi.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;

import com.mysql.jdbc.PreparedStatement;

import es.upm.dit.gsi.connection.Configuration;
import es.upm.dit.gsi.logger.Logger;

public class Preference {
	private static Configuration conf = Configuration.getConfiguration();
	private static Connection con = conf.getDbCon();
	
	private static final Logger LOGGER = Logger.getLogger("jdbc.Preference");
	
	/**
	 * Nos indica si un usuario ha valorado un contenido.
	 * 
	 * @param userId
	 * @param itemId
	 * @return true or false
	 */
	public static boolean userHaveItem (Long userId, Long itemId){
		try {
			String selectStatement = "SELECT preference FROM preference_table WHERE user_id = ? AND item_id = ?";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
			prepStmt.setLong(1, userId);
			prepStmt.setLong(2, itemId);
	    	ResultSet res = prepStmt.executeQuery();
	    	if (res.next()){
	    		LOGGER.info("El usario ha puntuado este contenido");
	    		return true;
	    	}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    LOGGER.info("El usuario no ha puntado este contenido");
	    return false;
	}
	
	/**
	 * Nos devuelve la puntuación que un usuario ha dado a un determinado contenido.
	 * 
	 * @param userId
	 * @param itemId
	 * @return value, preferencia del usuario hacía un contenido
	 */
	public static float preferenceItemForUser (Long userId, Long itemId){
		float value = 0;
		try {
			String selectStatement = "SELECT preference FROM preference_table WHERE user_id=? AND item_id=?";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
			prepStmt.setLong(1, userId);
			prepStmt.setLong(2, itemId);
	    	ResultSet res = prepStmt.executeQuery();
	    	if (res.next()){
	    		value = res.getFloat("preference");
	    	} else {
	    		LOGGER.warning("ERROR: El usuario no ha puntuado el objeto");
	    		value = -1; 
	    	}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return value;
	}
	
	/**
	 * Nos devuelve el número de objetos puntuados.
	 * 
	 * @return num_contents
	 */
	public static int numberOfRatedContents () {
		int num_contents = 0;
		try {
			String selectStatement = "SELECT DISTINCT item_id FROM preference_table";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
			ResultSet res = prepStmt.executeQuery();
			if (res.next())
				num_contents++;
		} catch (Exception e) {
	    	e.printStackTrace();
	    }
		return num_contents;
	}
	
	/**
	 * Nos devuelve los contenidos y su media de valoración.
	 * 
	 * @return avRatings
	 */
	public static HashMap<Long, Float> averageRatings (){
		HashMap<Long, Float> avRatings = new HashMap<Long, Float>();
		try {
			String selectStatement = "SELECT item_id FROM preference_table";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
			ResultSet res = prepStmt.executeQuery();
			if (res.next()){
				long id = res.getLong("item_id");
				System.out.println(id);
				//float averageItem = getAverageRating(id);
				//System.out.println(id + "," +averageItem);
				//avRatings.put(id, averageItem);
			}
		} catch (Exception e) {
	    	e.printStackTrace();
		}
		return avRatings;
	}
	
	/**
	 * Nos da la valoración media de un contenido.
	 * 
	 * @param itemId
	 * @return average
	 */
	public static float getAverageRating (long itemId){
		int times = 0;
		float totalRating=0;
		try {
			String selectStatement = "SELECT preference FROM preference_table WHERE item_id=?";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
			prepStmt.setLong(1, itemId);
	    	ResultSet res = prepStmt.executeQuery();
	    	if (res.next())
	    		times++;
	    		totalRating += res.getFloat("preference");
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    float average=totalRating/times;
	    return average;
	}
}
