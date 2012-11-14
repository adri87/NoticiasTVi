package es.upm.dit.gsi.auxiliar;

import java.util.Calendar;
import java.util.GregorianCalendar;

import es.upm.dit.gsi.jdbc.Contents;

public class Fechas {
	final static long MIL_DIA = 24 * 60 * 60 * 1000; //Milisegundos al día 
	
	public static float ponderacionTiempo (long contentId, float rate){
		Calendar c2 = new GregorianCalendar();
		if (Contents.getDateOfContent(contentId) != null){
			long diferencia = (c2.getTimeInMillis()-Contents.getDateOfContent(contentId).getTime())/MIL_DIA;
			int limite = (int) (rate/0.5);
			if (diferencia < 7) return rate;
			for (int n=1; n<=limite; n++){
				if (diferencia >= (n*7)){
					rate = (float) (rate - (n*0.5));
				}
			}
			return rate;
		} else {
			return rate = 0;
		}
	}
	
}
