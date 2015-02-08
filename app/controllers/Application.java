package controllers;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.OldEvent;
import model.json.Data;
import model.json.DataNode;
import play.libs.Yaml;
import play.mvc.Controller;
import play.mvc.Result;
import utils.TimestampUtils;
import views.html.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The main controller of the Play application.
 */
public class Application extends Controller {

    /**
     * Initializes the controller with test data.
     * @return the index result.
     */
    public static Result init() {
        Ebean.save((List) Yaml.load("test-data.yml"));

        return index();
    }

    /**
     * Loads the home page of the application.
     * @return the index result.
     */
    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    /**
     * Loads the raw data page, displaying all the raw data from the sensors (light, humidity...).
     * @return the raw data result.
     */
    public static Result data() {
        // url to parse: http://iotlab.telecomnancy.eu/rest/data/1/light1/1

        // fetch the json data
        String url = "http://iotlab.telecomnancy.eu/rest/data/1/light1/24/153.111";
        StringBuffer result = new StringBuffer();
        DataNode dataNode = new DataNode();
        ArrayList<OldEvent> oldEvents = null;
        try {
            URL oracle = new URL(url);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null)
                result.append(inputLine);
            System.out.println(inputLine);
            in.close();

            // map the json data in a DataNode
            ObjectMapper mapper = new ObjectMapper();
            dataNode = mapper.readValue(result.toString(), DataNode.class);

            Collections.reverse(dataNode.getData());
            boolean day = true;
            boolean light = false;
            double thresholdNightDay = 170, thresholdLight = 200;
            oldEvents = new ArrayList<>();
            for(Data data : dataNode.getData()) {
                double lightValue = data.getValue();
                if(lightValue < thresholdNightDay) { // night level
                    if (day || light) {
                        oldEvents.add(new OldEvent(data.getTimestamp(), "back to night", TimestampUtils.timestampToString(data.getTimestamp()), lightValue));
                    } else {
                        oldEvents.add(new OldEvent(data.getTimestamp(),"night", TimestampUtils.timestampToString(data.getTimestamp()), lightValue));
                    }
                    day = false;
                    light = false;
                } else if(lightValue > thresholdNightDay && lightValue < thresholdLight) { // day level
                    if(!day) {
                        oldEvents.add(new OldEvent(data.getTimestamp(),"sunrise", TimestampUtils.timestampToString(data.getTimestamp()), lightValue));
                    } else if(light) {
                        oldEvents.add(new OldEvent(data.getTimestamp(), "light turned off", TimestampUtils.timestampToString(data.getTimestamp()), lightValue));

                    } else {
                        oldEvents.add(new OldEvent(data.getTimestamp(), "day", TimestampUtils.timestampToString(data.getTimestamp()), lightValue));
                    }
                    day = true;
                    light= false;
                } else { // light is on
                    if(!light) {
                        oldEvents.add(new OldEvent(data.getTimestamp(),"light turned on", TimestampUtils.timestampToString(data.getTimestamp()), lightValue));
                    } else {
                        oldEvents.add(new OldEvent(data.getTimestamp(),"light already on", TimestampUtils.timestampToString(data.getTimestamp()), lightValue));
                    }
                    light= true;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ok(raw_values.render("Your new application is ready.", dataNode.getData(), oldEvents));
    }

    /**
     * Loads the page to display events.
     * @return events results.
     */
    public static Result oldEvents() {
        String url = "http://iotlab.telecomnancy.eu/rest/data/1/light1/24/153.111";
        StringBuffer result = new StringBuffer();
        DataNode dataNode = new DataNode();
        ArrayList<OldEvent> eventsList = null;
        try {
            URL oracle = new URL(url);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null)
                result.append(inputLine);
            System.out.println(inputLine);
            in.close();

            // map the json data in a DataNode
            ObjectMapper mapper = new ObjectMapper();
            dataNode = mapper.readValue(result.toString(), DataNode.class);

            Collections.reverse(dataNode.getData());
            boolean day = true;
            boolean light = false;
            double thresholdNightDay = 170, thresholdLight = 200;
            eventsList = new ArrayList<>();
            for(Data data : dataNode.getData()) {
                double lightValue = data.getValue();
                if(lightValue < thresholdNightDay) { // night level
                    if (day || light) {
                        eventsList.add(new OldEvent(data.getTimestamp(), "back to night", TimestampUtils.timestampToString(data.getTimestamp()), lightValue));
                    } else {
                        eventsList.add(new OldEvent(data.getTimestamp(), "night", TimestampUtils.timestampToString(data.getTimestamp()), lightValue));
                    }
                    day = false;
                    light = false;
                } else if(lightValue > thresholdNightDay && lightValue < thresholdLight) { // day level
                    if(!day) {
                        eventsList.add(new OldEvent(data.getTimestamp(), "sunrise", TimestampUtils.timestampToString(data.getTimestamp()), lightValue));
                    } else if(light) {
                        eventsList.add(new OldEvent(data.getTimestamp(), "light turned off", TimestampUtils.timestampToString(data.getTimestamp()), lightValue));

                    } else {
                        eventsList.add(new OldEvent(data.getTimestamp(), "day", TimestampUtils.timestampToString(data.getTimestamp()), lightValue));
                    }
                    day = true;
                    light= false;
                } else { // light is on
                    if(!light) {
                        eventsList.add(new OldEvent(data.getTimestamp(), "light turned on", TimestampUtils.timestampToString(data.getTimestamp()), lightValue));
                    } else {
                        eventsList.add(new OldEvent(data.getTimestamp(), "light already on", TimestampUtils.timestampToString(data.getTimestamp()), lightValue));
                    }
                    light= true;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ok(events.render("Your new application is ready.", eventsList));
    }

    /**
     * Displays the live stream page with the raw data from the sensors in live.
     * @return the live stream results.
     */
    public static Result liveStream() {
        return ok(liveStream.render("Live Stream"));
    }

    /**
     * Loads the JavaScript for the LiveStream.
     * @return the JavaScript code to run the LiveStream.
     */
    public static Result liveStreamJS() {
        return ok(views.js.liveStream.render());
    }

    /**
     * Loads the create event page.
     * @return the result of the event page.
     */
    public static Result createEvent() {
        return ok(create_events.render("Your new application is ready."));
    }
}
