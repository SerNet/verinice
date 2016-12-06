package org.primefaces.poseidon.view;

import java.io.Serializable;
import java.util.Calendar;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.primefaces.event.timeline.TimelineSelectEvent;
import org.primefaces.model.timeline.TimelineEvent;
import org.primefaces.model.timeline.TimelineModel;

@ManagedBean(name="basicTimelineView")
@ViewScoped
public class TimeLineView implements Serializable {

    private TimelineModel model;

    private boolean selectable = true;
    private boolean zoomable = true;
    private boolean moveable = true;
    private boolean stackEvents = true;
    private String eventStyle = "box";
    private boolean axisOnTop;
    private boolean showCurrentTime = true;
    private boolean showNavigation = false;

    @PostConstruct
    protected void initialize() {
        model = new TimelineModel();

        Calendar cal = Calendar.getInstance();
        cal.set(2014, Calendar.JUNE, 12, 0, 0, 0);
        model.add(new TimelineEvent("PrimeUI 1.1", cal.getTime()));

        cal.set(2014, Calendar.OCTOBER, 11, 0, 0, 0);
        model.add(new TimelineEvent("Primefaces 5.1.3", cal.getTime()));

        cal.set(2015, Calendar.DECEMBER, 8, 0, 0, 0);
        model.add(new TimelineEvent("PrimeUI 2.2", cal.getTime()));

        cal.set(2015, Calendar.MARCH, 10, 0, 0, 0);
        model.add(new TimelineEvent("Sentinel-Layout 1.1", cal.getTime()));

        cal.set(2015, Calendar.APRIL, 3, 0, 0, 0);
        model.add(new TimelineEvent("Spark-Layout 1.0", cal.getTime()));

        cal.set(2015, Calendar.MAY, 15, 0, 0, 0);
        model.add(new TimelineEvent("Ronin-Layout 1.0", cal.getTime()));

        cal.set(2015, Calendar.JULY, 10, 0, 0, 0);
        model.add(new TimelineEvent("Modena-Layout 1.0", cal.getTime()));

        cal.set(2015, Calendar.JUNE, 15, 0, 0, 0);
        model.add(new TimelineEvent("Rio-Layout 1.0", cal.getTime()));

        cal.set(2015, Calendar.SEPTEMBER, 4, 0, 0, 0);
        model.add(new TimelineEvent("Adamantium-Layout 1.0", cal.getTime()));

        cal.set(2015, Calendar.DECEMBER, 14, 0, 0, 0);
        model.add(new TimelineEvent("Titan-Layout 1.0", cal.getTime()));

        cal.set(2015, Calendar.OCTOBER, 12, 0, 0, 0);
        model.add(new TimelineEvent("Volt-Layout 1.0", cal.getTime()));

        cal.set(2016, Calendar.JANUARY, 28, 0, 0, 0);
        model.add(new TimelineEvent("Atlas-Layout 1.0", cal.getTime()));

        cal.set(2016, Calendar.FEBRUARY, 24, 0, 0, 0);
        model.add(new TimelineEvent("PrimeUI 4.1.0", cal.getTime()));

        cal.set(2016, Calendar.FEBRUARY, 29, 0, 0, 0);
        model.add(new TimelineEvent("Primefaces 5.3.8", cal.getTime()));

        cal.set(2016, Calendar.FEBRUARY, 29, 0, 0, 0);
        model.add(new TimelineEvent("PrimeNG 0.5", cal.getTime()));
    }

    public void onSelect(TimelineSelectEvent e) {
        TimelineEvent timelineEvent = e.getTimelineEvent();

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Selected event:", timelineEvent.getData().toString());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public TimelineModel getModel() {
        return model;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public boolean isZoomable() {
        return zoomable;
    }

    public void setZoomable(boolean zoomable) {
        this.zoomable = zoomable;
    }

    public boolean isMoveable() {
        return moveable;
    }

    public void setMoveable(boolean moveable) {
        this.moveable = moveable;
    }

    public boolean isStackEvents() {
        return stackEvents;
    }

    public void setStackEvents(boolean stackEvents) {
        this.stackEvents = stackEvents;
    }

    public String getEventStyle() {
        return eventStyle;
    }

    public void setEventStyle(String eventStyle) {
        this.eventStyle = eventStyle;
    }

    public boolean isAxisOnTop() {
        return axisOnTop;
    }

    public void setAxisOnTop(boolean axisOnTop) {
        this.axisOnTop = axisOnTop;
    }

    public boolean isShowCurrentTime() {
        return showCurrentTime;
    }

    public void setShowCurrentTime(boolean showCurrentTime) {
        this.showCurrentTime = showCurrentTime;
    }

    public boolean isShowNavigation() {
        return showNavigation;
    }

    public void setShowNavigation(boolean showNavigation) {
        this.showNavigation = showNavigation;
    }
}