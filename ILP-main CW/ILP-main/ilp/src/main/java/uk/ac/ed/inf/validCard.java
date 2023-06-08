package command;



import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;



public class validCard {


    public static boolean validCard(String cardNo, String CVV, String expiryMonth){
        if (!validCardNumber(cardNo)){
            return false;
        }
        if (!validCvv(CVV)){
            return false;
        }
        if (!validExpiryDate(expiryMonth)){
            return false;
        }
        return true;
    }

    public static boolean validCardNumber(String cardNumber){
        // matches if there are 16 digits in string ONLY
        String regex = "^\\d{16}$";
        Pattern pattern = Pattern.compile(regex);
        // returns true if card number is valid, false otherwise
        return pattern.matcher(cardNumber).find();
    }


    public static boolean validExpiryDate(String expiryDate){
        Logger log = Logger.getInstance();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth expiry = YearMonth.parse(expiryDate, formatter);
            // true if expiry date in the future, false if expiry date has passed
            return expiry.isAfter(YearMonth.now());
        } catch (DateTimeParseException e) {
            log.logAction("OrderValidation.validExpiryDate(expiryDate)", LogStatus.VALID_EXPIRY_DATE_PARSE_EXCEPTION);
            return false;
        }
    }

    public static boolean validCvv(String cvv){
        // matches if there are either 3 or 4 digits in string ONLY
        String regex = "^\\d{3,4}$";
        Pattern pattern = Pattern.compile(regex);
        // returns true if cvv is valid, false otherwise
        return pattern.matcher(cvv).find();
    }

    private enum LogStatus{
        VALID_EXPIRY_DATE_PARSE_EXCEPTION
    }
}