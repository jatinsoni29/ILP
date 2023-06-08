import command.validCard;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.ThreadLocalRandom;

public class ValidCardNumberTest {

    @Test
    void TestValidCardNumber(){
        String testCardNo = "1234567891342342";
        Assertions.assertTrue(validCard.validCardNumber(testCardNo));
    }
    @Test
    void TestInvalidLengthCardNumber(){
        String testCardNo = "1234567892234892374282432351234567";
        Assertions.assertFalse(validCard.validCardNumber(testCardNo));
    }
    @Test
    void TestInvalidCardNumber(){
        String testCardNo = "hello world!";
        Assertions.assertFalse(validCard.validCardNumber(testCardNo));
    }
    @RepeatedTest(10)
    void TestRandomValidCardNumber(){
        String testCardNo = "";
        for (int i = 0; i < 16; i++) {
            int digit = ThreadLocalRandom.current().nextInt(0, 10);
            testCardNo = testCardNo.concat(Integer.toString(digit));
        }
        Assertions.assertTrue(validCard.validCardNumber(testCardNo));
    }

    @RepeatedTest(10)
    void TestRandomInvalidCardNumber(){
        String testCardNo = "";

        int length = ThreadLocalRandom.current().nextInt(0, 100);
        while (length == 16){
            length = ThreadLocalRandom.current().nextInt(0, 100);
        }

        for (int i = 0; i < length; i++) {
            int digit = ThreadLocalRandom.current().nextInt(0, 100);
            testCardNo = testCardNo.concat(Integer.toString(digit));
        }
        Assertions.assertFalse(validCard.validCardNumber(testCardNo));
    }
}