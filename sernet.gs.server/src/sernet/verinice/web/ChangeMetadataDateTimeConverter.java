package sernet.verinice.web;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.DateTimeConverter;
import javax.faces.convert.FacesConverter;

@FacesConverter("changeMetadataDateTimeConverter")
public class ChangeMetadataDateTimeConverter extends DateTimeConverter {
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            Date date = (Date) value;
            ZonedDateTime dt = date.toInstant().atZone(ZoneId.systemDefault());
            return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.MEDIUM)
                    .format(dt);
        } else {
            return super.getAsString(context, component, value);
        }
    }
}
