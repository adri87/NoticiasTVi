package es.upm.dit.gsi.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Vector;

import com.mysql.jdbc.PreparedStatement;

import es.upm.dit.gsi.logger.Logger;
import es.upm.dit.gsi.connection.Configuration;

public class Items {
	private static Configuration conf = Configuration.getConfiguration();
	private static Connection con = conf.getDbCon();

	private static final Logger LOGGER = Logger.getLogger("jdbc.Items");
	
	/**
	 * Introduce un nuevo artículo en la base de datos y le asigna un
	 * identificador con el cuál se le asociará a partir de ahora.
	 * También obtiene el identificar si ya esta registrado.
	 * 
	 * @param nameOfItem
	 * @return itemId
	 */
	public static long introduceItem(String nameOfItem){
		Long itemId=null;
		
		try {

			String selectStatement = "SELECT id FROM items WHERE identifier = ? ";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
	    	prepStmt.setString(1, nameOfItem);
	    	ResultSet res = prepStmt.executeQuery();
	    	
	    	// Si el artículo ha sido ya introducido en la base de datos.
	    	if (res.next()){
	    		itemId = res.getLong("id");
	    		LOGGER.info("El identificador asociado al artículo " +nameOfItem+ " es: " + itemId);
	    	// Si es la primera vez que aparece el artículo.	
	    	} else {
	    		selectStatement = "INSERT INTO items (identifier) VALUES (?)";
				prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
		    	prepStmt.setString(1, nameOfItem);
		    	prepStmt.executeUpdate();
	    		LOGGER.info("Se ha introducido un nuevo artículo");
	    		
	    		selectStatement = "SELECT id FROM items WHERE identifier = ? ";
				prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
		    	prepStmt.setString(1, nameOfItem);
		    	res = prepStmt.executeQuery();
		    	if (res.next()){
		    		itemId = res.getLong("id");
		    		LOGGER.info("El identificador asociado al artículo " +nameOfItem+ " es: " + itemId);
		    	} else {
		    		LOGGER.severe("No deberíamos llegar aqui FALLO");
		    	}
	    	}
	    
		} catch (Exception e) {
	    	e.printStackTrace();
		}
		return itemId;
	}
	
	/**
	 * Nos devuelve el identificador asociado a un artículo ya registrado.
	 * 
	 * @param nameOfItem
	 * @return itemId
	 */
	
	public static Long getItemId (String nameOfItem) {
		Long itemId=null;
		
		try {
			String selectStatement = "SELECT id FROM items WHERE identifier = ? ";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
	    	prepStmt.setString(1, nameOfItem);
	    	ResultSet res = prepStmt.executeQuery();
	    	
	    	if (res.next()){
	    		itemId = res.getLong("id");
	    		LOGGER.info("El identificador asociado al artículo " +nameOfItem+ " es: " + itemId);
	    	} else {
	    		LOGGER.warning("No existe el artículo seleccionado");
	    	}
	    
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		return itemId;
	}
	
	/**
	 * Nos devuelve el nombre de un determinado artículo a 
	 * partir de su identificador.
	 * 
	 * @param itemId
	 * @return nameOfItem
	 */
	public static String getNameOfItem (Long itemId) {
		String nameOfItem="";

		try {
			String selectStatement = "SELECT identifier FROM items WHERE id = ? ";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
	    	prepStmt.setLong(1, itemId);
	    	ResultSet res = prepStmt.executeQuery();
	    	
	    	if (res.next()){
	    		nameOfItem = res.getString("identifier");
	    		//LOGGER.info("El identificador: " +itemId+ " se corresponde con el artículo: " +gesforItemId);
	    	} else {
	    		LOGGER.warning("No existe ningún artículo con este identificador");
	    	}
	    
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }	
		return nameOfItem;
	}
	
	/**
	 * Nos devuelve el cojunto de identificadores de los contenidos disponibles
	 * 
	 * @return itemsIds
	 */
	public static Vector<Long> getItemsIds(){
		Vector<Long> itemsIds = new Vector<Long>();
		try {
			String selectStatement = "SELECT id FROM items";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
	    	ResultSet res = prepStmt.executeQuery();
	    	while (res.next()){
	    		itemsIds.addElement(res.getLong("id"));
	    	} 
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return itemsIds;
		
	}
}