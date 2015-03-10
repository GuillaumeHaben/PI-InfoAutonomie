package model;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Ced on 09/02/2015.
 */
@Entity
public class BasicEventOccurrence extends Model implements Comparable<BasicEventOccurrence> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public String id;

    @ManyToOne
    public BasicEvent basicEvent;
    public long timestamp;
    public String date;
    public double fromValue, toValue;

    public BasicEventOccurrence() {
    }

    public BasicEventOccurrence(BasicEvent basicEvent, String date, long timestamp, double fromValue, double toValue) {
        this.basicEvent = basicEvent;
        this.date = date;
        this.fromValue = fromValue;
        this.toValue = toValue;
        this.timestamp = timestamp;
    }

    public static BasicEventOccurrence create(BasicEventOccurrence basicEventOccurrence) {
        basicEventOccurrence.save();
        return basicEventOccurrence;
    }

    public static List<BasicEventOccurrence> all() {
        return find.all();
    }

    public static Model.Finder<String,BasicEventOccurrence> find = new Model.Finder<>(String.class, BasicEventOccurrence.class);

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getFromValue() {
        return fromValue;
    }

    public void setFromValue(double fromValue) {
        this.fromValue = fromValue;
    }

    public double getToValue() {
        return toValue;
    }

    public void setToValue(double toValue) {
        this.toValue = toValue;
    }

    public BasicEvent getBasicEvent() {
        return basicEvent;
    }

    public void setBasicEvent(BasicEvent basicEvent) {
        this.basicEvent = basicEvent;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int compareTo(BasicEventOccurrence o) {
        return (int) (timestamp - o.getTimestamp());
    }

    @Override
    public String toString() {
        return "BasicEventOccurrence{" +
                "id='" + id + '\'' +
                ", basicEvent=" + basicEvent +
                ", timestamp=" + timestamp +
                ", date='" + date + '\'' +
                ", fromValue=" + fromValue +
                ", toValue=" + toValue +
                '}';
    }

    public long occur(TimeInterval t, BasicEvent basicEvent) {
        TimeInterval todayTimeInterval = t.getActualTimeInterval(); // get today timestamp corresponding to TimeInterval

        GregorianCalendar begin = new GregorianCalendar();
        begin.setTimeInMillis(todayTimeInterval.getTimestampStart()*1000);

        GregorianCalendar end = new GregorianCalendar();
        end.setTimeInMillis(todayTimeInterval.getTimestampEnd()*1000);

        System.out.println("From " +  (begin.getTimeInMillis()/1000) + " to " +  (end.getTimeInMillis()/1000));
        System.out.println("SEARCHING FOR " + basicEvent.getId());

        String basicEventId = basicEvent.getId();
        List<BasicEventOccurrence> basicsEventOccurrences = BasicEventOccurrence.find.where()
                .between("timestamp", begin.getTimeInMillis() / 1000, end.getTimeInMillis() / 1000).eq("basic_event_id", basicEventId).findList();

        System.out.println("Found " + basicsEventOccurrences.size() + " item(s) ");


        return basicsEventOccurrences.size() > 0 ? basicsEventOccurrences.get(0).getTimestamp() : -1;
    }
}