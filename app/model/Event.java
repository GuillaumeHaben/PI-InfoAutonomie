package model;

import jboolexpr.BooleanExpression;
import jboolexpr.MalformedBooleanException;
import play.db.ebean.Model;
import utils.TimestampUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes an event.
 * Created by Ced on 31/01/2015.
 */

@Entity
public class Event extends Model {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public String id;

    /**
     * The name of the event.
     */
    public String name;
    /**
     * List of the basic events composing the event.
     */
    @ManyToMany(cascade = CascadeType.REMOVE)
    public List<BasicEvent> basicEvents = new ArrayList<>();
    /**
     * The duration of the event.
     */
    public int duration;
    /**
     * The time interval in which the event can happen.
     */
    @OneToOne
    public TimeInterval timeInterval;

    /**
     * The expression combining BasicEvent ids
     */
    public String expression;

    public String icon;
    public String color;

    public String validate() {
        validateExpression(expression);
        if (expression.isEmpty()) {
            return "Expression vide !";
        }
        if (!validateExpression(expression)) {
            return "Expression fausse ou un évènement de base n'existe pas.";
        }
        return null;
    }

    public boolean validateExpression(String toEval) {
        String[] basicEventIds = toEval.split("(\\|\\||&&)");

        // first, check if all given basic events exist in database
        for (String id : basicEventIds) {
            id = id.trim();
            toEval = toEval.replace(id, "true");
            boolean basicEventExist = BasicEvent.find.where().eq("id", id).findRowCount() == 1;
            System.out.println("basicEventID: " + id + " " + basicEventExist);
            if (!basicEventExist) {
                return false;
            }
        }

        // then, test if the expression is syntactically correct by trying to parse it (after having replacer all the ids by true for example
        BooleanExpression boolExpr;
        try {
            System.out.println("Expression: " + toEval);
            boolExpr = BooleanExpression.readLeftToRight(toEval);
            boolean bool = boolExpr.booleanValue();
            System.out.println("Result of the evaluation: " + boolExpr + " == " + bool);
        } catch (MalformedBooleanException e) {
            System.out.println("Invalid Expression");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * The list of all the existing Event.
     */
    public static Model.Finder<String, Event> find = new Model.Finder<>(String.class, Event.class);

    /**
     * Initializes the given Event with the given time interval and saves it.
     *
     * @param event        the Event to initialize.
     * @param timeInterval a description of the TimeInterval.
     * @return the Event saved and initialized.
     */
    public static Event create(Event event, String timeInterval) {
        event.timeInterval = TimeInterval.find.byId(timeInterval);
        event.save();
        event.saveManyToManyAssociations("basicEvents");
        event.save();
        return event;
    }

    /**
     * Returns the list of all the Events
     *
     * @return the list of all the Events
     */
    public static List<Event> all() {
        return find.all();
    }


    public void check() {
        // TODO: verify that event has not already been detected for the last TimeInterval
        TimeInterval todayTimeInterval = this.getTimeInterval().getActualTimeInterval();
        if (EventOccurrence.find.where().eq("event_id", getId())
                .between("timestamp", todayTimeInterval.getTimestampStart(), todayTimeInterval.getTimestampEnd())
                .findIds().size() > 0) { // Event already in DB
            return;
        }

        BasicEventOccurrence basicEventOccurrence = new BasicEventOccurrence();

        String toEval = expression; // copy the string

//        System.out.println("STRING : " + toEval);
        long mean = 0;
        int cpt = 0;
        String[] basicEventIds = toEval.split("(\\|\\||&&)");
        for (String id : basicEventIds) {
            id = id.trim();
            BasicEvent basicEvent = BasicEvent.find.ref(id);
//            System.out.println("Current BasicEventID: " + basicEvent.getId());
            long occurTime = basicEventOccurrence.occur(timeInterval, basicEvent);
            if (occurTime != -1) {
                cpt++;
                mean += occurTime;
            }
            toEval = toEval.replace(id, (occurTime != -1) + "");

        }

        System.out.println("STRING : " + toEval);

        BooleanExpression boolExpr;
        try {
            boolExpr = BooleanExpression.readLeftToRight(toEval);
            boolean bool = boolExpr.booleanValue();
            System.out.println(boolExpr.toString() + " == " + bool);

            if (bool && cpt > 0) {
                EventOccurrence eventOccurrence = new EventOccurrence(this, mean / cpt, TimestampUtils.formatToString(mean / cpt, "dd-MM-yyyy HH:mm:SS"));
                if (EventOccurrence.find.where().eq("timestamp", eventOccurrence.getTimestamp()).eq("event_id", eventOccurrence.getEvent().getId()).findUnique() == null) {
                    eventOccurrence.save();
                }
//                System.out.println("EVENT OCCURRENCE: persisted!");
            }
            // (((!true)&&false)||true) == true
        } catch (MalformedBooleanException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the name of the Event.
     *
     * @return the name of the Event.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the Event.
     *
     * @param name the new Event name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the list of the BasicEvent used to identify this Event.
     *
     * @return the list of the BasicEvent used to identify this Event.
     */
    public List<BasicEvent> getBasicEvents() {
        return basicEvents;
    }

    /**
     * Sets the list of the BasicEvent used to identify this Event.
     *
     * @param basicEvents the new list.
     */
    public void setBasicEvents(List<BasicEvent> basicEvents) {
        this.basicEvents = basicEvents;
    }

    /**
     * Returns the duration of the Event.
     *
     * @return the duration of the Event.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets the duration of the Event.
     *
     * @param duration the new duration.
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Returns the TimeInterval in which the Event can occur.
     *
     * @return the TimeInterval in which the Event can occur.
     */
    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    /**
     * Sets the TimeInterval in which the Event can occur.
     *
     * @param timeInterval the new TimeInterval.
     */
    public void setTimeInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Returns the description of the object as JSON.
     *
     * @return the description of the object as JSON.
     */
    @Override
    public String toString() {
        String basicEventsStr = "";
        for (BasicEvent b : getBasicEvents()) {
            basicEventsStr += b.toString() + " --- ";
        }
        return "Event{" +
                "name='" + getName() + '\'' +
                ", basicEvents=" + basicEventsStr +
                ", duration=" + getDuration() +
                ", timeInterval=" + getTimeInterval() +
                '}';
    }
}
