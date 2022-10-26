package life.genny.datagenerator.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

public class GeneratorUtils {

    public static final boolean DEFAULT_INFERRED = false;
    public static final boolean DEFAULT_PRIVACY_FLAG = false;
    public static final boolean DEFAULT_READ_ONLY = false;
    public static final String DEFAULT_REALM = "Genny";

    protected static final String[] GENDER = {"MALE", "FEMALE", "OTHER"};
    public static final String COMPLETED = "Completed";
    public static final String AVAILABLE = "AVAILABLE";
    public static final String ACTIVE = "ACTIVE";

    private final Random random = new Random();

    private ObjectMapper objectMapper = new ObjectMapper();

    private int generateRandomNum(int size) {
        return random.nextInt(size);
    }

    public String generateRandString(int length) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = (int)(AlphaNumericString.length() * Math.random());

            sb.append(AlphaNumericString.charAt(index));
        }

        return sb.toString();
    }

    public String generateUUID() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    public String generateFirstName() {
        return generateRandString(8);
    }

    public String generateLastName() {
        return generateRandString(8);
    }

    public Email generateEmail(String firstName, String lastName) {
        String[] emailHost = new String[]{"gmail.com", "hotmail.com", "outlook.com", "yahoo.com"};
        String host = emailHost[random.nextInt(emailHost.length)];
        String[] separators = new String[]{".", "_", "-"};
        String separator = separators[random.nextInt(separators.length)];
        long timestamps = (new Date()).getTime();
        return new Email(firstName.toLowerCase() + separator + lastName.toLowerCase() + separator + timestamps, host);
    }

    public LocalDate generateDOB() throws ParseException {
        GregorianCalendar calendar = new GregorianCalendar();
        DateUtil dtUtil = new DateUtil();
        int year = dtUtil.pickRandom(1970, 2000);
        calendar.set(Calendar.YEAR, year);
        int day = dtUtil.pickRandom(1, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
        calendar.set(Calendar.DAY_OF_YEAR, day);
        String generatedDate = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return Timestamp.from(dateFormat.parse(generatedDate).toInstant()).toLocalDateTime().toLocalDate();
    }

    public <E> E pickRandomData(List<E> data) {
        int ranInt = generateRandomNum(data.size());
        return data.get(ranInt);
    }

    public String generateGender() {
        int i = random.nextInt(3);
        return GENDER[i];
    }

    public String generateLinkedInURL(String firstName, String lastName) {
        String linkedInBaseUrl = "https://www.linkedin.com/in/";
        return linkedInBaseUrl + firstName + lastName;
    }

    public String generatePhoneNumber() {
        int part1 = random.nextInt(600) + 100;
        int part2 = random.nextInt(641) + 100;
        int part3 = random.nextInt(8999) + 1000;
        return part1 + "" + part2 + "" + part3;
    }

    public String generateImageUrl(List<String> images) {
        return images.get(generateRandomNum(images.size()));
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public String generateUTCTimeZone(int utcOffset) {
        if (utcOffset >= 0)
            return "UTC +" + (utcOffset / 60);
        else
            return "UTC " + (utcOffset / 60);
    }
}
