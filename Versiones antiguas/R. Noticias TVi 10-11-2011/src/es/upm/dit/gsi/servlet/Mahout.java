package es.upm.dit.gsi.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
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
//import es.upm.dit.gsi.methods.Auxiliar;
import es.upm.dit.gsi.methods.Data;
import es.upm.dit.gsi.jdbc.Contents;
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
		//dataModel = new MySQLJDBCDataModel(conf.getDataSource(), "preferenceTable", "user_id","item_id", "preference");
		dataModel = new MySQLJDBCDataModel(conf.getDataSource(), "preferenceTable", "user_id","content_id", "preference");
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
				out.print(callback+ "([");
				for (int i = 0; i < recommendations.size(); i++) {
					RecommendedItem recommendation = recommendations.get(i);
					long contentId = recommendation.getItemID();
					float estimation = recommender.estimatePreference(userId, contentId);
					/*out.print("{'nombre':'"+Contents.getNameOfContent(contentId));
					out.print("','estimacion':'"+estimation);
					out.print("','video':'"+Auxiliar.getVideo());
					out.print("','captura':'"+Auxiliar.getCaptura());*/
					out.print("{'nombre':'"+Contents.getTitleOfContent(contentId));
					out.print("','estimacion':'"+estimation);
					out.print("','video':'"+Contents.getVideoOfContent(contentId));
					out.print("','captura':'"+Contents.getCaptureOfContent(contentId));
					out.print("','fecha':'"+Contents.getDateOfContent(contentId));
					out.print("','contenido':'"+Contents.getContent(contentId));
					out.print("','autor':'"+Contents.getAuthorOfContent(contentId));
					if (i + 1 != recommendations.size())
						out.print("'},");
					else 
						out.print("'}");
				}
				out.print("])");
			} else {
				LOGGER.warning("No se puede dar recomendación ya que no existe el usuario"); 
			}
		} catch (Exception e) {
			e.printStackTrace();
			res.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}
	}
	
	/**
	 * Pone en la base de datos la preferencia de un cierto usuario asociada a un contenido.
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
			String nameOfContent = req.getParameter(Constants.CONTENT);
			//Long contentId = Contents.introduceContent(nameOfContent);
			Long contentId = Contents.getContentId(nameOfContent);
			float preference = new Float(req.getParameter(Constants.PREFERENCE_PARAM));
			if (Preference.userHaveContent(userId, contentId)){
				dataModel.removePreference(userId, contentId);
				LOGGER.info("El usuario " + nameOfUser  + " ha modificado la valoración del contenido " + nameOfContent + " a " +preference);
			} else {
				LOGGER.info("El usuario " + nameOfUser  + " añade un valoración de " + preference + " al contenido " + nameOfContent);
			}
			dataModel.setPreference(userId, contentId, preference);
		} catch (Exception e) {
			e.printStackTrace();
			res.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}

	}

	/**
	 * Nos devuelve los contenidos que ha valorado un usuario dado y su valoración.
	 * 
	 * @param req,HttpServletRequest
	 * @param res,HttpServletResponse
	 * @throws ServletException si se produce algún error
	 * @throws IOException si se produce algún error
	 */
	private void getContentsFromUser(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try{
			String nameOfUser= req.getParameter (Constants.IDENTIFIER);
			Long userId = Users.getUserId(nameOfUser);
			if (userId != null){
				LOGGER.info("Los contenidos valorados por el usuario " + nameOfUser + " son:");
				PreferenceArray preferencesUser = dataModel.getPreferencesFromUser(userId);
				for (int i=0; i<preferencesUser.length(); i++){
					//String content = Contents.getNameOfContent(preferencesUser.getItemID(i));
					String content = Contents.getTitleOfContent(preferencesUser.getItemID(i));
					float value = preferencesUser.getValue(i);
					LOGGER.info("Contenido: "+content+". Valoración: " +value);
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
			Long contentId = Contents.getContentId(nameOfContent);
			if (userId != null){
				if (Preference.userHaveContent(userId, contentId)){
					out.print(callback+ "(");
					float rating = dataModel.getPreferenceValue(userId, contentId);
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
	 * Elimina la valoración a un contenido de la base de datos.
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
			String nameOfContent = req.getParameter(Constants.CONTENT);
			Long contentId = Contents.getContentId(nameOfContent);
			if (userId != null && contentId != null){
				if (Preference.userHaveContent(userId, contentId) == true){
					dataModel.removePreference(userId, contentId);
					LOGGER.info("Eliminamos la valoración del usuario " + nameOfUser + " para el contenido " + nameOfContent);
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
			int popular = new Integer(Constants.NUM_POPULAR);
			String callback = req.getParameter(Constants.CALLBACK);
			out.print(callback+ "([");
			HashMap<Long, Float> averageRatings = Preference.averageRatings();
			Vector<Long> contentsids = Contents.getContentsIds();
			long[] id = new long[popular];
			float[] value = new float[popular];
			for (int j=0; j<popular; j++){
				value[j]=0;
			}
			for (long i=1; i<=(long)contentsids.size(); i++){
				for (int k=0; k<popular; k++){
					if((averageRatings.get(i)) != null){
						if (averageRatings.get(i)>value[k]){
							for (int h=popular-1; h>k; h--){
								id[h]=id[h-1];
								value[h]=value[h-1];
							}
							id[k]=i;
							value[k]=averageRatings.get(i);
							break;
						}
					}
				}
			}
			for (int j=0; j<popular; j++){
				//out.print(Contents.getNameOfContent(id[j]));
				out.print(Contents.getTitleOfContent(id[j]));
				out.print(":");
				out.print(value[j]);
				if (j + 1 != popular)
					out.print(",");
			}
			out.print("])");	
		} catch (Exception e) {
			e.printStackTrace();
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	/**
	 * Introduce datos de prueba en la base de datos
	 * 
	 * @param req
	 * @param res
	 * @throws ServletException
	 * @throws IOException
	 */
	private void setData (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
		Data.introduceData();
		try {
			dataModel.setPreference(1, 2, 4);
			dataModel.setPreference(1, 4, 5);
			dataModel.setPreference(1, 7, 2);
			dataModel.setPreference(1, 10, 4);
			dataModel.setPreference(1, 8, 3);
			dataModel.setPreference(2, 3, 3);
			dataModel.setPreference(2, 1, 4);
			dataModel.setPreference(2, 10, 2);
			dataModel.setPreference(2, 3, 5);
			dataModel.setPreference(3, 4, 1);
			dataModel.setPreference(3, 7, 5);
			dataModel.setPreference(3, 1, 4);
			dataModel.setPreference(4, 6, 5);
			dataModel.setPreference(4, 2, 2);
			dataModel.setPreference(4, 5, 4);
			dataModel.setPreference(4, 11, 5);
			dataModel.setPreference(4, 7, 4);
			dataModel.setPreference(5, 5, 5);
			dataModel.setPreference(5, 11, 5);
			dataModel.setPreference(5, 8, 3);
			dataModel.setPreference(6, 9, 5);
			dataModel.setPreference(6, 3, 4);
			dataModel.setPreference(6, 7, 4);
			dataModel.setPreference(6, 1, 3);
			dataModel.setPreference(7, 10, 3);
			dataModel.setPreference(7, 5, 2);
			dataModel.setPreference(7, 2, 5);
			dataModel.setPreference(7, 4, 4);
			dataModel.setPreference(7, 11, 3);
			dataModel.setPreference(8, 9, 4);
			dataModel.setPreference(8, 8, 5);
			dataModel.setPreference(8, 6, 5);
			dataModel.setPreference(8, 2, 3);
			
			LOGGER.info("Datos introducios");
		} catch (TasteException e) {
			LOGGER.warning("Ha ocurrido un error al introducir los datos");
			e.printStackTrace();
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
			getContentsFromUser(req, res);
		} else if (req.getParameter("action").equals("removePreference")) {
			removePreference(req,res);
   		} else if (req.getParameter("action").equals("getPopular")){
   			getPopular(req, res);
   		} else if (req.getParameter("action").equals("setData")){
   			setData(req, res);
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
