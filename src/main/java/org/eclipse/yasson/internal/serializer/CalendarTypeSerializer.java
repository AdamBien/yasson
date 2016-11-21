/*******************************************************************************
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * Roman Grigoriadi
 ******************************************************************************/

package org.eclipse.yasson.internal.serializer;

import org.eclipse.yasson.internal.ProcessingContext;
import org.eclipse.yasson.model.SerializerBindingModel;
import org.eclipse.yasson.model.PropertyModel;

import javax.json.bind.JsonbConfig;
import javax.json.bind.annotation.JsonbDateFormat;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;

/**
 * @author David Král
 */
public class CalendarTypeSerializer extends AbstractValueTypeSerializer<Calendar> {

    private final Calendar calendarTemplate;


    public CalendarTypeSerializer(SerializerBindingModel model) {
        super(Calendar.class, model);
        calendarTemplate = Calendar.getInstance();
        calendarTemplate.clear();
    }


    private String toJson(Calendar object, JsonbDateFormatter formatter) {
        if (JsonbDateFormat.TIME_IN_MILLIS.equals(formatter.getFormat())) {
            return String.valueOf(object.getTime().getTime());
        }

        Locale locale = ProcessingContext.getJsonbContext().getLocale(formatter.getLocale());
        if (JsonbDateFormat.DEFAULT_FORMAT.equals(formatter.getFormat())) {
            final Optional<Object> strictJson =
                    ProcessingContext.getJsonbContext().getConfig().getProperty(JsonbConfig.STRICT_IJSON);

            //Use ISO_DATE_TIME, convert to java.time first
            //TODO PERF subject to reconsider if conversion between java.time and java.util outweights threadsafe java.time formatter.
            if (strictJson.isPresent() && (Boolean) strictJson.get()
                    || object.isSet(Calendar.HOUR) || object.isSet(Calendar.HOUR_OF_DAY)) {

                ZonedDateTime zdt = ZonedDateTime.ofInstant(object.toInstant(), object.getTimeZone().toZoneId());
                return DateTimeFormatter.ISO_DATE_TIME.withLocale(locale).format(zdt);
            }
            final DateFormat defaultFormat = new SimpleDateFormat(JsonbDateFormatter.ISO_8601_DATE_FORMAT, locale);
            defaultFormat.setTimeZone(object.getTimeZone());
            return defaultFormat.format(object.getTime());
        }

        DateFormat custom = new SimpleDateFormat(formatter.getFormat(), locale);
        custom.setTimeZone(object.getTimeZone());
        return custom.format(object.getTime());
    }

    @Override
    public void serialize(Calendar obj, JsonGenerator generator, SerializationContext ctx) {
        final JsonbDateFormatter formatter = ProcessingContext.getJsonbContext().getComponentMatcher().getDateFormatter(model);
        if (model instanceof PropertyModel) {
            generator.write(((PropertyModel)model).getPropertyName(), toJson(obj, formatter));
        } else {
            generator.write(toJson(obj, formatter));
        }
    }

    @Override
    protected void serialize(Calendar obj, JsonGenerator generator, String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void serialize(Calendar obj, JsonGenerator generator) {
        throw new UnsupportedOperationException();
    }
}
