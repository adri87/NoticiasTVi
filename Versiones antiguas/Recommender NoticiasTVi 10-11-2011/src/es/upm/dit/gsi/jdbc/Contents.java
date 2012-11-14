package es.upm.dit.gsi.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Vector;

import com.mysql.jdbc.PreparedStatement;

import es.upm.dit.gsi.logger.Logger;
import es.upm.dit.gsi.connection.Configuration;

public class Contents {
	private static Configuration conf = Configuration.getConfiguration();
	private static Connection con = conf.getDbCon();

	private static final Logger LOGGER = Logger.getLogger("jdbc.Contents");
	
	/**
	 * Introduce un nuevo contenido en la base de datos y le asigna un
	 * identificador con el cuál se le asociará a partir de ahora.
	 * También obtiene el identificar si ya esta registrado.
	 * 
	 * @param nameOfContent
	 * @return itemId
	 */
	public static long introduceContent(String nameOfContent){
		Long contentId=null;
		
		try {

			String selectStatement = "SELECT id FROM items WHERE identifier = ? ";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
	    	prepStmt.setString(1, nameOfContent);
	    	ResultSet res = prepStmt.executeQuery();
	    	
	    	// Si el artículo ha sido ya introducido en la base de datos.
	    	if (res.next()){
	    		contentId = res.getLong("id");
	    		LOGGER.info("El identificador asociado al contenido " +nameOfContent+ " es: " + contentId);
	    	// Si es la primera vez que aparece el artículo.	
	    	} else {
	    		selectStatement = "INSERT INTO items (identifier) VALUES (?)";
				prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
		    	prepStmt.setString(1, nameOfContent);
		    	prepStmt.executeUpdate();
	    		LOGGER.info("Se ha introducido un nuevo contenido");
	    		
	    		selectStatement = "SELECT id FROM items WHERE identifier = ? ";
				prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
		    	prepStmt.setString(1, nameOfContent);
		    	res = prepStmt.executeQuery();
		    	if (res.next()){
		    		contentId = res.getLong("id");
		    		LOGGER.info("El identificador asociado al contenido " +nameOfContent+ " es: " + contentId);
		    	} else {
		    		LOGGER.severe("No deberíamos llegar aqui FALLO");
		    	}
	    	}
	    
		} catch (Exception e) {
	    	e.printStackTrace();
		}
		return contentId;
	}
	
	/**
	 * Nos devuelve el identificador asociado a un contenido ya registrado.
	 * 
	 * @param nameOfContent
	 * @return contentId
	 */
	
	public static Long getContentId (String nameOfContent) {
		Long contentId=null;
		
		try {
			String selectStatement = "SELECT id FROM items WHERE identifier = ? ";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
	    	prepStmt.setString(1, nameOfContent);
	    	ResultSet res = prepStmt.executeQuery();
	    	
	    	if (res.next()){
	    		contentId = res.getLong("id");
	    		LOGGER.info("El identificador asociado al contenido " +nameOfContent+ " es: " + contentId);
	    	} else {
	    		LOGGER.warning("No existe el contenido seleccionado");
	    	}
	    
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		return contentId;
	}
	
	/**
	 * Nos devuelve el nombre de un determinado contenido a 
	 * partir de su identificador.
	 * 
	 * @param contentId
	 * @return nameOfContent
	 */
	public static String getNameOfContent (Long contentId) {
		String nameOfContent="";

		try {
			String selectStatement = "SELECT identifier FROM items WHERE id = ? ";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
	    	prepStmt.setLong(1, contentId);
	    	ResultSet res = prepStmt.executeQuery();
	    	
	    	if (res.next()){
	    		nameOfContent = res.getString("identifier");
	    		//LOGGER.info("El identificador: " +itemId+ " se corresponde con el artículo: " +gesforItemId);
	    	} else {
	    		LOGGER.warning("No existe ningún contenido con este identificador");
	    	}
	    
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }	
		return nameOfContent;
	}
	
	/**
	 * Nos devuelve el cojunto de identificadores de los contenidos disponibles
	 * 
	 * @return contentsIds
	 */
	public static Vector<Long> getContentsIds(){
		Vector<Long> contentsIds = new Vector<Long>();
		try {
			String selectStatement = "SELECT id FROM items";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
	    	ResultSet res = prepStmt.executeQuery();
	    	while (res.next()){
	    		contentsIds.addElement(res.getLong("id"));
	    	} 
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return contentsIds;
		
	}
}