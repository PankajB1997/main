package seedu.tasklist.commons.util;

import java.util.Calendar;

public class RecurringUtil {
	
	public Calendar updateRecurringDate(Calendar toUpdate, String frequency) {
		
		switch (frequency) {
		case "daily": toUpdate.add(Calendar.DAY_OF_YEAR, 1); break;
		case "weekly": toUpdate.add(Calendar.WEEK_OF_YEAR, 1); break;
		case "monthly": toUpdate.add(Calendar.MONTH, 1); break;
		case "yearly": toUpdate.add(Calendar.YEAR, 1); break;
		}
		
		return toUpdate;
	}
}