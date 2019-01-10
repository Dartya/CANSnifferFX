import com.cansnifferfx.models.Calculate;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class TestMain {
    @Test
    public void mainTest() throws Exception{
        Calculate calculate = new Calculate();
        int n = calculate.calA(3, 2);

        assertEquals(5, n);
    }
}
