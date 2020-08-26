package it.polimi.io;

import it.polimi.domain.Location;
import it.polimi.domain.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpeedyReader {

    public List<Service> read(String branch, Calendar startDate, int t) {
        String dirPath = "instances/speedy/" + t + "/";
        Calendar endDate = (Calendar) startDate.clone();
        endDate.add(Calendar.DATE, t - 1);
        String filePath = dirPath + getFileName(branch, startDate, endDate);
        File file = new File(filePath);
        if (!file.exists() || !file.isFile())
            throw new IllegalArgumentException("Cannot find dataset " + filePath);
        return readCSV(file);
    }

    public List<Service> readCSV(File csv) {
        List<Service> services = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(csv));
            String line = reader.readLine();
            int count = 0;
            while (line != null) {
                String[] splitted = line.split(",");
                double lat = Double.parseDouble(splitted[0]);
                double lng = Double.parseDouble(splitted[1]);
                Location loc = new Location(Integer.toString(count), lat, lng);
                TemporalAccessor tacc = DateTimeFormatter.ISO_INSTANT.parse(splitted[2]);
                Instant instant = Instant.from(tacc);
                ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
                Calendar from = GregorianCalendar.from(zdt);
                // TODO do better
                int ndays = 2;
                int score = 1;
                services.add(new Service(loc, from, ndays, score));
                line = reader.readLine();
                count++;
            }
            reader.close();
            return services;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFileName(String branch, Calendar from, Calendar to) {
        return branch + "-" + formatCalendar(from) + "-" + formatCalendar(to) + ".csv";
    }

    private String formatCalendar(Calendar c) {
        int date = c.get(Calendar.DATE);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        return date + "-" + (month + 1) + "-" + year;
    }

    public static List<Long> parseReleaseDates(final List<Service> services) {
        List<Long> releaseDays = services.stream()
                .map(Service::getReleaseDate)
                .map(Calendar::getTimeInMillis)
                .map(TimeUnit.MILLISECONDS::toDays)
                .collect(Collectors.toList());
        Long min = releaseDays.stream().min(Long::compareTo).orElse(0L);
        return releaseDays.stream().map(rd -> rd - min).collect(Collectors.toList());
    }

    public static List<Long> parseDueDates(final List<Service> services) {
        List<Long> releaseDates = parseReleaseDates(services);
        return IntStream.range(0, releaseDates.size())
                .mapToLong(i -> releaseDates.get(i) + services.get(i).getDays() - 1)
                .boxed()
                .collect(Collectors.toList());
    }
}
