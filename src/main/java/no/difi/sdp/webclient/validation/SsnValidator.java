package no.difi.sdp.webclient.validation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class SsnValidator {

	private static final String DATE_FORMAT = "ddMMyyyy";

    private static final int DNUMBER_INCREMENT_ON_DATE = 4;

    private static final int BIRTH_YEAR_END_INDEX = 6;

    private static final int BIRTH_YEAR_START_INDEX = 4;

    private static final int DATE_END_INDEX = 4;

    private static final int DATE_DNUMBER_START_INDEX = 1;

    private static final int INDIVID_NR_END_INDEX = 9;

    private static final int INDIVID_NR_START_INDEX = 6;

    private static final int DATE_CENTURY_SPLIT_1 = 40;

    private static final int DATE_CENTURY_SPLIT_2 = 54;

    private static final int INDIVID_NR_CENTURY_SPLIT_1 = 499;

    private static final int INDIVID_NR_CENTURY_SPLIT_2 = 500;

    private static final int INDIVID_NR_CENTURY_SPLIT_3 = 749;

    private static final int INDIVID_NR_CENTURY_SPLIT_4 = 900;

    private static final int DAY_NUMBER_MAX_VALUE = 7;

    private static final int ILLEGAL_CHECKSUM_VALUE = 10;

    private static final int EIGHTEENTH_CENTURY = 18;

    private static final int TWENTIETH_CENTURY = 20;

    private static final int NINETEENTH_CENTURY = 19;

    public static final int SSN_FULL_LENGTH = 11;

    public static boolean isValid(String ssn) {
        if (ssn == null) {
            return false;
        }
        if (ssn.length() != SSN_FULL_LENGTH) {
            return false;
        }

        if (!validateAllDigits(ssn)) {
            return false;
        }

        final int dNumberDigit = Integer.parseInt(String.valueOf(ssn.charAt(0)));
        final int birthYear = Integer.parseInt(ssn.substring(BIRTH_YEAR_START_INDEX, BIRTH_YEAR_END_INDEX));
        final int individNumber = Integer.parseInt(ssn.substring(INDIVID_NR_START_INDEX, INDIVID_NR_END_INDEX));
        final int century = getCentury(individNumber, birthYear);

        if (dNumberDigit > DAY_NUMBER_MAX_VALUE) {
            return false;
        }
        if (century < 0) {
            return false;
        }
        if (!validateDate(getDateAndMonth(dNumberDigit, ssn), century, birthYear)) {
            return false;
        }
        return validateChecksums(ssn);

    }

    private static int getCentury(final int individnumber, final int birthYear) {

        if (individnumber <= INDIVID_NR_CENTURY_SPLIT_1) {
            return NINETEENTH_CENTURY;
        }

        if (individnumber >= INDIVID_NR_CENTURY_SPLIT_2 && individnumber <= INDIVID_NR_CENTURY_SPLIT_3 && birthYear > DATE_CENTURY_SPLIT_2) {
            return EIGHTEENTH_CENTURY;
        }

        if (individnumber >= INDIVID_NR_CENTURY_SPLIT_2 && birthYear < DATE_CENTURY_SPLIT_1) {
            return TWENTIETH_CENTURY;
        }

        if (individnumber >= INDIVID_NR_CENTURY_SPLIT_4 && birthYear >= DATE_CENTURY_SPLIT_1) {
            return NINETEENTH_CENTURY;
        }

        return -1;
    }

    private static String getDateAndMonth(final int dNumberDigit, final String ssn) {

        if (dNumberDigit >= DNUMBER_INCREMENT_ON_DATE) {
            return (dNumberDigit - DNUMBER_INCREMENT_ON_DATE) + ssn.substring(DATE_DNUMBER_START_INDEX, DATE_END_INDEX);
        } else {
            return dNumberDigit + ssn.substring(DATE_DNUMBER_START_INDEX, DATE_END_INDEX);
        }
    }

    private static boolean validateChecksums(final String ssn) {
        int[] n = new int[SSN_FULL_LENGTH];

        for (int i = 0; i < SSN_FULL_LENGTH; i++) {
            n[i] = Character.getNumericValue(ssn.charAt(i));
        }

        // Checksum number 1
        int checksum1 = SSN_FULL_LENGTH
                        - ((3 * n[0] + 7 * n[1] + 6 * n[2] + 1 * n[3] + 8 * n[4] + 9 * n[5] + 4 * n[6] + 5 * n[7] + 2 * n[8]) % SSN_FULL_LENGTH);
        if (checksum1 == SSN_FULL_LENGTH) {
            checksum1 = 0;
        }

        if (checksum1 == ILLEGAL_CHECKSUM_VALUE || checksum1 != n[9]) {
            return false;
        }

        // Checksum number 2
        int checksum2 = SSN_FULL_LENGTH
                        - ((5 * n[0] + 4 * n[1] + 3 * n[2] + 2 * n[3] + 7 * n[4] + 6 * n[5] + 5 * n[6] + 4 * n[7] + 3 * n[8] + 2 * checksum1) % SSN_FULL_LENGTH);
        if (checksum2 == SSN_FULL_LENGTH) {
            checksum2 = 0;
        }

        if (checksum2 == ILLEGAL_CHECKSUM_VALUE || checksum2 != n[10]) {
            return false;
        }

        return true;
    }

    private static boolean validateAllDigits(final String string) {
        for (int i = 0; i < string.length(); i++) {
            if (!Character.isDigit(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean validateDate(final String dateAndMonth, int century, int birthYear) {

        try {
            final String dateString = dateAndMonth + century + birthYear;
            final DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            df.parse(dateString);
            return true;
        } catch (final ParseException e) {
            return false;
        }
    }
    
}
