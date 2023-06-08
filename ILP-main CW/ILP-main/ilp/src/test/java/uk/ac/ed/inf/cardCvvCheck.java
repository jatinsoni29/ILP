import command.validCard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

public class ValidCVVTest {
    @Test
    void TestValidThreeDigitCVV(){
        String testCVV = "123";
        Assertions.assertTrue(validCard.validCvv(testCVV));
    }

    @Test
    void TestValidFourDigitCVV(){
        String testCVV = "1234";
        Assertions.assertTrue(validCard.validCvv(testCVV));
    }
    @Test
    void TestInvalidLengthCVV(){
        String testCVV = "187234293874928243244327";
        Assertions.assertFalse(validCard.validCvv(testCVV));
    }
    @Test
    void TestInvalidCVV(){
        String testCVV = "diuwehdiwueh";
        Assertions.assertFalse(validCard.validCvv(testCVV));
    }

    @Test
    void TestInvalidCVVWithCorrectLength(){
        String testCVV = "abc";
        Assertions.assertFalse(validCard.validCvv(testCVV));
    }

    @RepeatedTest(10)
    void TestRandomThreeDigitValidCVV(){
        String testCVV = "";
        for (int i = 0; i < 3; i++) {
            int digit = ThreadLocalRandom.current().nextInt(0, 10);
            testCVV = testCVV.concat(Integer.toString(digit));
        }
        Assertions.assertTrue(validCard.validCvv(testCVV));
    }

    @RepeatedTest(10)
    void TestRandomFourDigitValidCVV(){
        String testCVV = "";
        for (int i = 0; i < 4; i++) {
            int digit = ThreadLocalRandom.current().nextInt(0, 10);
            testCVV = testCVV.concat(Integer.toString(digit));
        }
        Assertions.assertTrue(validCard.validCvv(testCVV));
    }

    @RepeatedTest(10)
    void TestRandomInvalidCVV(){
        String testCVV = "";
        int length = ThreadLocalRandom.current().nextInt(0, 100);
        while (length == 3 || length == 4){
            length = ThreadLocalRandom.current().nextInt(0, 100);
        }

        for (int i = 0; i < length; i++) {
            int digit = ThreadLocalRandom.current().nextInt(20, 100);
            testCVV = testCVV.concat(Integer.toString(digit));
        }
        Assertions.assertFalse(validCard.validCvv(testCVV));
    }
}