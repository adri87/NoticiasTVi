package es.upm.dit.gsi.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.common.TasteException;

import es.upm.dit.gsi.logger.Logger;
import es.upm.dit.gsi.jdbc.Items;
import es.upm.dit.gsi.jdbc.Preference;
import es.upm.dit.gsi.jdbc.Users;
import es.upm.dit.gsi.connection.Configuration;

public class Mahout extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private JDBCDataModel dataModel;
	private UserNeighborhood neighborhood;
	private UserSimilarity similarity;
	private Recommender recommender;
	public static Configuration conf;
	private static final Logger LOGGER = Logger.getLogger("servlet.Mahout");

	/**
	 * init del servlet
	 * 
	 * @throws ServletException si se produce un error
	 */
	public void init() throws ServletException {
		super.init();
		conf = Configuration.getConfiguration();
		dataModel = new MySQLJDBCDataModel(conf.getDataSource(), "preference_table", "user_id","item_id", "preference");
		LOGGER.info("dato model es"+dataModel);
		try {
			similarity = new PearsonCorrelationSimilarity(dataModel);
		} catch (TasteException e) {
			e.printStackTrace();
		}
		neighborhood = new NearestNUserNeighborhood(2, similarity, dataModel);
		recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
	}

   /**
	 * Genera la recomendación para un determinado usuario del servicio.
	 * 
	 * @param req,HttpServletRequest
	 * @param res,HttpServletResponse
	 * @throws ServletException si se produce algún error
	 * @throws IOException si se produce algún error
	 */
	private void getRecommendation(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		try {
			PrintWriter out =res.getWriter();
			String callback = req.getParameter(Constants.CALLBACK);
			String identifier = req.getParameter(Constants.IDENTIFIER);
			Long userId = Users.getUserId(identifier);
			Integer max = new Integer(req.getParameter(Constants.MAX_RECOM_PARAM));
			//Integer max = new Integer(Constants.MAX_RECOM_PARAM);
			if (userId !=null){
				List<RecommendedItem> recommendations = recommender.recommend(userId, max);
				out.print(callback+ "({");
				for (int i = 0; i < recommendations.size(); i++) {
					RecommendedItem recommendation = recommendations.get(i);
					long itemId = recommendation.getItemID();
					out.print(Items.getNameOfItem(itemId));
					out.print(":");
					float estimation = recommender.estimatePreference(userId, itemId);
					out.print(estimation);
					if (i + 1 != recommendations.size())
						out.print(",");
				}
				out.print("})");
			} else {
				LOGGER.warning("No se puede dar recomendación ya que no existe el usuario"); 
			}
		} catch (Exception e) {
			e.printStackTrace();
			res.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}
	}
	
	/**
	 * Pone en la base de datos la preferencia de un cierto usuario asociada a un objeto.
	 * 
	 * @param req,HttpServletRequest
	 * @param res,HttpServletResponse
	 * @throws ServletException si se produce algún error
	 * @throws IOException si se produce algún error
	 */
	public synchronized void setPreference(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException{
		try {
			String nameOfUser = req.getParameter(Constants.IDENTIFIER);
			Long userId = Users.introduceUser(nameOfUser);
			String nameOfItem = req.getParameter(Constants.ITEM);
			Long itemId = Items.introduceItem(nameOfItem);
			float preference = new Float(req.getParameter(Constants.PREFERENCE_PARAM));
			if (Preference.userHaveItem(userId, itemId)){
				dataModel.removePreference(userId, itemId);
				LOGGER.info("El usuario " + nameOfUser  + " ha modificado la valoración del objeto " + nameOfItem + " a " +preference);
			} else {
				LOGGER.info("El usuario " + nameOfUser  + " añade un valoración de " + preference + " al objeto " + nameOfItem);
			}
			dataModel.setPreference(userId, itemId, preference);
		} catch (Exception e) {
			e.printStackTrace();
			res.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}

	}

	/**
	 * Nos devuelve los objetos que ha valorado un usuario dado y su valoración.
	 * 
	 * @param req,HttpServletRequest
	 * @param res,HttpServletResponse
	 * @throws ServletException si se produce algún error
	 * @throws IOException si se produce algún error
	 */
	private void getItemsFromUser(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try{
			String nameOfUser= req.getParameter (Constants.IDENTIFIER);
			Long userId = Users.getUserId(nameOfUser);
			if (userId != null){
				LOGGER.info("Los objetos valorados por el usuario " + nameOfUser + " son:");
				PreferenceArray preferencesUser = dataModel.getPreferencesFromUser(userId);
				for (int i=0; i<preferencesUser.length(); i++){
					String item = Items.getNameOfItem(preferencesUser.getItemID(i));
					float value = preferencesUser.getValue(i);
					LOGGER.info("Item: "+item+". Valoración: " +value);
				}
			} else {
				LOGGER.warning("Usuario no encontrado");
			}
		} catch (Exception e) {
			e.printStackTrace();
			res.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}
	}
	
	/**
	 * Nos devuelve la valoración que le ha dado el usuario un determinado contenido.
	 * 
	 * @param req,HttpServletRequest
	 * @param res,HttpServletResponse
	 * @throws ServletException si se produce algún error
	 * @throws IOException si se produce algún error
	 */
	public void getRatingOfUserToContent (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try{
			PrintWriter out =res.getWriter();
			String callback = req.getParameter(Constants.CALLBACK);
			String nameOfUser= req.getParameter (Constants.IDENTIFIER);
			String nameOfContent= req.getParameter (Constants.CONTENT);
			Long userId = Users.getUserId(nameOfUser);
			Long itemId = Items.getItemId(nameOfContent);
			if (userId != null){
				if (Preference.userHaveItem(userId, itemId)){
					out.print(callback+ "(");
					float rating = dataModel.getPreferenceValue(userId, itemId);
					LOGGER.info("La valoración al contenido " +nameOfContent+" por el usuario " + nameOfUser + " es:" +rating);
					out.print(rating);
					out.print(")");
				} else {
					LOGGER.warning("El usuario no ha valorado el contenido");
				}
			} else {
				LOGGER.warning("Usuario no encontrado");
			}
		} catch (Exception e) {
			e.printStackTrace();
			res.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Elimina un elemento de la base de datos.
	 * 
	 * @param req,HttpServletRequest
	 * @param res,HttpServletResponse
	 * @throws ServletException si se produce algún error
	 * @throws IOException si se produce algún error
	 */
	private void removePreference(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
		try {
			String nameOfUser = req.getParameter(Constants.IDENTIFIER);
			Long userId = Users.getUserId(nameOfUser);
			String nameOfItem = req.getParameter(Constants.ITEM);
			Long itemId = Items.getItemId(nameOfItem);
			if (userId != null && itemId != null){
				if (Preference.userHaveItem(userId, itemId) == true){
					dataModel.removePreference(userId, itemId);
					LOGGER.info("Eliminamos la valoración del usuario " + nameOfUser + " para el objeto " + nameOfItem);
				}
			} else {
				LOGGER.warning("Usuario o objeto no encontrado");
			}
		} catch (Exception e) {
			e.printStackTrace();
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	/**
	 * Devuelve los contenidos más populares (los mejores votados) por los clientes del servicio
	 * 
	 * @param req,HttpServletRequest
	 * @param res,HttpServletResponse
	 * @throws ServletException si se produce algún error
	 * @throws IOException si se produce algún error
	 */
	private void getPopular (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
		try {
			PrintWriter out =res.getWriter();
			Integer popular = new Integer(Constants.NUM_POPULAR);
			String callback = req.getParameter(Constants.CALLBACK);
			out.print(callback+ "([");
			//for (int i = 0; i < popular; i++) {
			HashMap<Long, Float> averageRatings = Preference.averageRatings();
			//}
			out.print("])");	
		} catch (Exception e) {
			e.printStackTrace();
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

   /**
	 * Ejecuta la acción solicitada por el cliente.
	 * 
	 * @param req,HttpServletRequest
	 * @param res,HttpServletResponse
	 * @throws ServletException si se produce algún error
	 * @throws IOException si se produce algún error
	 */
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		System.out.println("Esperando respuesta");

		if (req.getParameter("action").equals("getRecommendation")) {
			getRecommendation(req, res);
		} else if (req.getParameter("action").equals("getRatingOfUserToContent")) {
			getRatingOfUserToContent(req, res);
		} else if (req.getParameter("action").equals("setPreference")) {
			setPreference(req, res);
		} else if (req.getParameter("action").equals("getItemsFromUser")) {
			getItemsFromUser(req, res);
		} else if (req.getParameter("action").equals("removePreference")) {
			removePreference(req,res);
   		} else if (req.getParameter("action").equals("getPopular")){
   			getPopular(req, res);
   		}
		res.setStatus(HttpServletResponse.SC_OK);
   }

   /**
    * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
    */
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       // TODO Auto-generated method stub
   }

}
