package es.upm.dit.gsi.methods;

import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import es.upm.dit.gsi.logger.Logger;


public class Auxiliar {
	private static final Logger LOGGER = Logger.getLogger("methods.Auxiliar");
	
	/**
	 * Nos indica si un usuario ha valorado un objeto.
	 * 
	 * @param dataModel
	 * @param userId
	 * @param itemId
	 */
	public static boolean userHaveItem (DataModel dataModel, Long userId, Long itemId){
		try{
			PreferenceArray preferencesUser = dataModel.getPreferencesFromUser(userId);
			for (int i=0; i<preferencesUser.length(); i++){
				if (itemId == preferencesUser.getItemID(i)){
					LOGGER.info("El usario ha puntuado este contenido");
					return true;
				}
					
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("El usuario no ha puntado este contenido");
		return false;
	}

}
