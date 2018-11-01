package testr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dhb on 2018/10/29.
 */

public class Test {
    public static void main(String args[]) {
        List<Double> list = new ArrayList();
        list.add(15.01);
        list.add(15.02);
        list.add(15.03);
        list.add(15.04);
        list.add(15.08);
        list.add(15.06);
        double max = Collections.max(list);
        int index = list.indexOf(max);
        System.out.println("max : " + max + ", index : " + index) ;
    }
}
