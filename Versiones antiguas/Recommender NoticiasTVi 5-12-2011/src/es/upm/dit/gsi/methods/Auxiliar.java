package es.upm.dit.gsi.methods;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.codehaus.jettison.json.JSONObject;

import es.upm.dit.gsi.logger.Logger;


public class Auxiliar {
	private static final Logger LOGGER = Logger.getLogger("methods.Auxiliar");
	
	public static String getLikes (String source){
		String likes = "0";
		try {
			String pag = "https://graph.facebook.com/?ids="+source;
			URL page = new URL(pag);
			URLConnection url = page.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(url.getInputStream()));
            String inputLine;
            String text = null;
            while ((inputLine = in.readLine()) != null) {
                text = inputLine;
            }
            JSONObject json = new JSONObject(text);
            if(json.getJSONObject(source).getString("shares")!=null)
            	likes= json.getJSONObject(source).getString("shares");
            in.close();
		} catch (Exception e) {
			LOGGER.warning("No hay likes para la noticia seleccionada");
		}
		return likes;
	}
	
}
