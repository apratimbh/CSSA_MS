package pr_curve;

import java.util.ArrayList;

public class curve {
	 public ArrayList<curve_point> point_list=new ArrayList<curve_point>();
	 public void add(curve_point pt)
	 {
		 this.point_list.add(pt);
	 }
}
