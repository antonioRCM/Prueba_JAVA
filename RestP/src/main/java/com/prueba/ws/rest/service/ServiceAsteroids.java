package com.prueba.ws.rest.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.prueba.ws.rest.dto.Constant;
import com.prueba.ws.rest.dto.SalidaDto;

/**
 * Clase encargada de hacer peticion de Asteroides
 * @author ARCM
 */
@Path("/asteroids")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceAsteroids extends Application {

	/** Constante: LOGGER */
	private static final Logger LOGGER = Logger.getLogger(ServiceAsteroids.class);
	
	@GET
	public Response topAsteroids (@QueryParam("days") Integer days) {
		
		LOGGER.debug("ServiceAsteroids | topAsteroids() | INI");
		
		// Comprobar si viene informado el parametro de entrada
		if (days != null) {
			// Comprobar si el parametro de entrada es valido
			if (days >= 1 && days<=7) {
				//Obtener el rango de fechas a utilizar
				DateTimeFormatter format = DateTimeFormatter.ofPattern(Constant.FORMAT_DATE);
				String todayDate = format.format(LocalDateTime.now());
				String endDate = format.format(LocalDateTime.now().plusDays(days));
				//Obtener la lista de asteroides validos
				ArrayList<SalidaDto> resultList = petitionApi(todayDate, endDate);
				
				//Comprobar que se ha obtenido resultados
				if (!resultList.isEmpty()) {
					//Obtener los 3 de mayor diametro
					ArrayList<SalidaDto> resultListFinal = getTOP3(resultList);
					//Crear JSON resultante
			        JSONArray result = new JSONArray(resultListFinal);
			        LOGGER.debug("ServiceAsteroids | topAsteroids() | CORRECT | FIN");
					return Response.ok(result.toString(),MediaType.APPLICATION_JSON).build();
				}else {
					LOGGER.debug("ServiceAsteroids | topAsteroids() | ERROR_VALID | FIN");
					return Response.ok(Constant.ERROR_VALID,MediaType.APPLICATION_JSON).build();
				}
			} else {
				LOGGER.debug("ServiceAsteroids | topAsteroids() | ERROR_RANGO | FIN");
				return Response.ok(Constant.ERROR_RANGO,MediaType.APPLICATION_JSON).build();
			}
		} else {
			LOGGER.debug("ServiceAsteroids | topAsteroids() | ERROR_PARAM | FIN");
			return Response.ok(Constant.ERROR_PARAM,MediaType.APPLICATION_JSON).build();
		}
	}

	/**
	 * Metodo para obtener los 3 asteroides de mayor diametro
	 * @param resultList
	 * @return
	 */
	private ArrayList<SalidaDto> getTOP3(ArrayList<SalidaDto> resultList) {
		LOGGER.debug("ServiceAsteroids | getTOP3() | INI");
		// Ordenar la lista original por diametro 
		resultList.sort(new Comparator<SalidaDto>() {
			public int compare(SalidaDto o1, SalidaDto o2) {
				if (new Double(o1.getDiameter()) < new Double(o2.getDiameter())) {
					return 1;
				} else if (new Double(o1.getDiameter()) > new Double(o2.getDiameter())) {
					return -1;
				}
				return 0;
			}
		});
		ArrayList<SalidaDto> resultListFinal = new ArrayList<SalidaDto>();
		int i = 0;
		// Guardar los tres primeros
		for (SalidaDto request : resultList) {
			if (i > 2) {
				break;
			} else {
				i++;
			}
			resultListFinal.add(request);
		}
		LOGGER.debug("ServiceAsteroids | getTOP3() | FIN");
		return resultListFinal;
	}
	
	/**
	 * Metodo encargado de realizar la peticion a la API de la NASA
	 * @param todayDate
	 * @param endDate
	 * @return
	 */
	private ArrayList<SalidaDto> petitionApi(String todayDate, String endDate) {
		LOGGER.debug("ServiceAsteroids | petitionApi() | INI");
		//Crear la URL para la peticion a la API
		String urlApi = "https://api.nasa.gov/neo/rest/v1/feed?start_date=" + todayDate + "&end_date=" + endDate + "&api_key=zdUP8ElJv1cehFM0rsZVSQN7uBVxlDnu4diHlLSb"; 
        //Cliente para la conexion
        Client client = ClientBuilder.newClient();
        //Definicion de URL
        WebTarget target = client.target(urlApi);
        //Recogemos el resultado en una variable String
        String response = target.request(MediaType.APPLICATION_JSON).get(String.class);
        
        ArrayList<SalidaDto> listResult = new ArrayList<SalidaDto>();
        //Formatear el resultado en dato JSON
        JSONObject jsonResult;
        response = "[" + response + "]";
        JSONArray jsonList = new JSONArray(response);

        if (!jsonList.isEmpty()) {
        	 for (int i= 0; i < jsonList.length(); i++) {
             	
             	jsonResult = jsonList.getJSONObject(i);
             	//Obtencion de la lista de Asteroides completa
             	JSONObject jsonResultP = jsonResult.getJSONObject(Constant.NEAR_EARTH_OBJECTS);
             	if (jsonResultP != null) {
             		//Obtencion de los diferentes paquetes de ateroides
             		Iterator<String> keys = jsonResultP.keys();
                 	while(keys.hasNext()) {
                 		String key = keys.next();
                 		//Obtencion de la lista de Asteroides por paquete
                 		JSONArray asteroids = jsonResultP.getJSONArray(key);
                 		
                 		if (!asteroids.isEmpty()) {
                 			//Creacion de la lista con los asteroides validos
                 			asteroidDataExtraction(listResult, asteroids);
                 		}
                 	}
             	}
             }
        }
        LOGGER.debug("ServiceAsteroids | petitionApi() | FIN");
        return listResult;     
	}

	/**
	 * Metodo encagado de realizar la extracion de los datos de los asteroides validados
	 * @param listResult
	 * @param asteroids
	 */
	private void asteroidDataExtraction(ArrayList<SalidaDto> listResult, JSONArray asteroids) {
		LOGGER.debug("ServiceAsteroids | asteroidDataExtraction() | INI");
		SalidaDto result;
		JSONObject jsonAsteroid;
		for (int j= 0; j < asteroids.length(); j++) {
			
			jsonAsteroid = asteroids.getJSONObject(j);
			
			// Comprobar si es valido para la extracion
			if (jsonAsteroid.getBoolean(Constant.IS_SENTRY_OBJECT)) {
				result = new SalidaDto();
				//Obtencion del nombre
				result.setName(jsonAsteroid.getString(Constant.NAME));
				
				//Obtencion del diametro
				Double diameterMin = jsonAsteroid.getJSONObject(Constant.ESTIMATED_DIAMETER).getJSONObject(Constant.KILOMETERS).getDouble(Constant.ESTIMATED_DIAMETER_MIN);
				Double diameterMax = jsonAsteroid.getJSONObject(Constant.ESTIMATED_DIAMETER).getJSONObject(Constant.KILOMETERS).getDouble(Constant.ESTIMATED_DIAMETER_MAX);
				Double diameter = (diameterMin+diameterMax)/2;
				result.setDiameter(diameter.toString());
				
				JSONArray closeApproachDatas = jsonAsteroid.getJSONArray(Constant.CLOSE_APPROAC_DATA);
				
				if (!closeApproachDatas.isEmpty()) {
					JSONObject closeApproachData;
					for (int h= 0; h < closeApproachDatas.length(); h++) {
						closeApproachData = closeApproachDatas.getJSONObject(h);
						//Obtencion de la velocidad
						result.setSpeed(closeApproachData.getJSONObject(Constant.RELATIVE_VELOCITY).getString(Constant.KILOMETERS_PER_HOUR));
						//Obtencion de la fecha
		 				result.setDate(closeApproachData.getString(Constant.CLOSE_APPROACH_DATE));
		 				//Obtencion del planeta
		 				result.setPlanet(closeApproachData.getString(Constant.ORBITING_BODY));
					}
				}
				//Guardado del resultado en la lista
				listResult.add(result);
			}
		 }
		LOGGER.debug("ServiceAsteroids | asteroidDataExtraction() | FIN");
	}
	
}
