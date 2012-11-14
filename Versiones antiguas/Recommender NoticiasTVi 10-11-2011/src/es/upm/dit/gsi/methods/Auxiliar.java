package es.upm.dit.gsi.methods;

import java.util.Random;

import es.upm.dit.gsi.logger.Logger;


public class Auxiliar {
	private static final Logger LOGGER = Logger.getLogger("methods.Auxiliar");
	private static final String[] videos = {"/elmundo/videos/2011/11/09/espana/1320878161_extras_video.flv",
			"/elmundo/videos/2011/11/09/ocio/1320843697_extras_video.flv",
			"/elmundo/videos/2011/11/09/espana/1320862573_extras_video.flv",
			"/elmundo/videos/2011/11/09/espana/1320862573_extras_video.flv",
			"http://www.youtube.com/embed/mLXD1vjAXw4",
			"/elmundo/videos/2011/11/10/valencia/1320910557_extras_video.flv",
			"/elmundo/videos/2011/11/09/paisvasco/1320868014_extras_video.flv",
			"/elmundo/videos/2011/11/09/paisvasco/1320856990_extras_video.flv",
			"/elmundo/videos/2011/11/09/paisvasco/1320851978_extras_video.flv",
			"/elmundo/videos/2011/11/08/madrid/1320742027_extras_video.flv",
			"/elmundo/videos/2011/11/09/andalucia_sevilla/1320869289_extras_video.flv"};
	private static final String[] capturas = {"http://estaticos.elmundo.es/elmundo/videos/2011/11/09/espana/1320878161_extras_video_7.jpg",
			"http://estaticos.elmundo.es/elmundo/imagenes/2011/11/09/ocio/1320843697_g_1.jpg",
			"http://estaticos.elmundo.es/elmundo/imagenes/2011/11/09/espana/1320862573_g_0.jpg",
			"http://estaticos03.cache.el-mundo.net/america/imagenes/2011/10/18/deportes/1318964294_0.jpg",
			"",
			"http://estaticos.elmundo.es/elmundo/imagenes/2011/11/10/valencia/1320910557_g_0.jpg",
			"http://estaticos.elmundo.es/elmundo/videos/2011/11/09/paisvasco/1320868014_extras_video_8.jpg",
			"http://estaticos.elmundo.es/elmundo/videos/2011/11/09/paisvasco/1320856990_extras_video_6.jpg",
			"http://estaticos.elmundo.es/elmundo/videos/2011/11/09/paisvasco/1320851978_extras_video_7.jpg",
			"http://estaticos.elmundo.es/elmundo/videos/2011/11/08/madrid/1320742027_extras_video_3.jpg",
			"http://estaticos.elmundo.es/elmundo/imagenes/2011/11/10/andalucia_sevilla/1320869289_g_0.jpg"};
	private static int x;
	
	public static String getVideo(){
		Random rnd = new Random();
	    x = (int)(rnd.nextDouble() * 11.0);
	    LOGGER.info("Video elegido número "+x);
		return videos[x];
	}
	
	public static String getCaptura(){
		return capturas[x];
	}
}
