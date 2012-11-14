package es.upm.dit.gsi.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Vector;

import com.mysql.jdbc.PreparedStatement;

//import es.upm.dit.gsi.h2.Configuration;
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
	public static long introduceContent(String title, String video, String capture, String date, String content, String author){
		Long contentId=null;
		
		try {

			String selectStatement = "SELECT id FROM contents WHERE title = ? ";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
	    	prepStmt.setString(1, title);
	    	ResultSet res = prepStmt.executeQuery();
	    	
	    	// Si el artículo ha sido ya introducido en la base de datos.
	    	if (res.next()){
	    		contentId = res.getLong("id");
	    		LOGGER.info("El identificador asociado al contenido " +title+ " es: " + contentId);
	    	// Si es la primera vez que aparece el artículo.	
	    	} else {
	    		selectStatement = "INSERT INTO contents (title, video, capture, date, content, author) VALUES (?,?,?,?,?,?)";
				prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
		    	prepStmt.setString(1, title);
		    	prepStmt.setString(2, video);
		    	prepStmt.setString(3, capture);
		    	prepStmt.setString(4, date);
		    	prepStmt.setString(5, content);
		    	prepStmt.setString(6, author);
		    	prepStmt.executeUpdate();
	    		LOGGER.info("Se ha introducido un nuevo contenido");
	    		
	    		selectStatement = "SELECT id FROM contents WHERE title = ? ";
				prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
		    	prepStmt.setString(1, title);
		    	res = prepStmt.executeQuery();
		    	if (res.next()){
		    		contentId = res.getLong("id");
		    		LOGGER.info("El identificador asociado al contenido " +title+ " es: " + contentId);
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
	public static Long getContentId (String title) {
		Long contentId=null;
		
		try {
			String selectStatement = "SELECT id FROM contents WHERE title = ? ";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
	    	prepStmt.setString(1, title);
	    	ResultSet res = prepStmt.executeQuery();
	    	
	    	if (res.next()){
	    		contentId = res.getLong("id");
	    		LOGGER.info("El identificador asociado al contenido " +title+ " es: " + contentId);
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
	public static String getTitleOfContent (Long contentId) {
		String titleOfContent="";

		try {
			String selectStatement = "SELECT title FROM contents WHERE id = ? ";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
	    	prepStmt.setLong(1, contentId);
	    	ResultSet res = prepStmt.executeQuery();
	    	
	    	if (res.next()){
	    		titleOfContent = res.getString("title");
	    	} else {
	    		LOGGER.warning("No existe ningún contenido con este identificador");
	    	}
	    
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }	
		return titleOfContent;
	}
	
	/**
	 * Nos devuelve el video de un determinado contenido a 
	 * partir de su identificador.
	 * 
	 * @param contentId
	 * @return videoOfContent
	 */
	public static String getVideoOfContent (Long contentId) {
		String videoOfContent="";

		try {
			String selectStatement = "SELECT video FROM contents WHERE id = ? ";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
	    	prepStmt.setLong(1, contentId);
	    	ResultSet res = prepStmt.executeQuery();
	    	
	    	if (res.next()){
	    		videoOfContent = res.getString("video");
	    	} else {
	    		LOGGER.warning("No existe ningún contenido con este identificador");
	    	}
	    
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }	
		return videoOfContent;
	}
	
	/**
	 * Nos devuelve la captura de un determinado contenido a 
	 * partir de su identificador.
	 * 
	 * @param contentId
	 * @return captureOfContent
	 */
	public static String getCaptureOfContent (Long contentId) {
		String captureOfContent="";

		try {
			String selectStatement = "SELECT capture FROM contents WHERE id = ? ";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
	    	prepStmt.setLong(1, contentId);
	    	ResultSet res = prepStmt.executeQuery();
	    	
	    	if (res.next()){
	    		captureOfContent = res.getString("capture");
	    	} else {
	    		LOGGER.warning("No existe ningún contenido con este identificador");
	    	}
	    
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }	
		return captureOfContent;
	}
	
	/**
	 * Nos devuelve la fecha de un determinado contenido a 
	 * partir de su identificador.
	 * 
	 * @param contentId
	 * @return dateOfContent
	 */
	public static String getDateOfContent (Long contentId) {
		String dateOfContent="";

		try {
			String selectStatement = "SELECT date FROM contents WHERE id = ? ";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
	    	prepStmt.setLong(1, contentId);
	    	ResultSet res = prepStmt.executeQuery();
	    	
	    	if (res.next()){
	    		dateOfContent = res.getString("date");
	    	} else {
	    		LOGGER.warning("No existe ningún contenido con este identificador");
	    	}
	    
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }	
		return dateOfContent;
	}
	
	/**
	 * Nos devuelve el autor de un determinado contenido a 
	 * partir de su identificador.
	 * 
	 * @param contentId
	 * @return authorOfContent
	 */
	public static String getAuthorOfContent (Long contentId) {
		String authorOfContent="";

		try {
			String selectStatement = "SELECT author FROM contents WHERE id = ? ";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
	    	prepStmt.setLong(1, contentId);
	    	ResultSet res = prepStmt.executeQuery();
	    	
	    	if (res.next()){
	    		authorOfContent = res.getString("author");
	    	} else {
	    		LOGGER.warning("No existe ningún contenido con este identificador");
	    	}
	    
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }	
		return authorOfContent;
	}
	
	/**
	 * Nos devuelve el contenido a partir de su identificador.
	 * 
	 * @param contentId
	 * @return content
	 */
	public static String getContent (Long contentId) {
		String content="";

		try {
			String selectStatement = "SELECT content FROM contents WHERE id = ? ";
			PreparedStatement prepStmt = (PreparedStatement) con.prepareStatement(selectStatement);
	    	prepStmt.setLong(1, contentId);
	    	ResultSet res = prepStmt.executeQuery();
	    	
	    	if (res.next()){
	    		content = res.getString("content");
	    	} else {
	    		LOGGER.warning("No existe ningún contenido con este identificador");
	    	}
	    
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }	
		return content;
	}
	
	/**
	 * Nos devuelve el cojunto de identificadores de los contenidos disponibles
	 * 
	 * @return contentsIds
	 */
	 public static Vector<Long> getContentsIds(){
		Vector<Long> contentsIds = new Vector<Long>();
		try {
			String selectStatement = "SELECT id FROM contents";
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