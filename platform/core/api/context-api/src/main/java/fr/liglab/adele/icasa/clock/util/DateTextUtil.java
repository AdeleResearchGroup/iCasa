/**
 *
 *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.clock.util;

import fr.liglab.adele.icasa.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utuilitary class to format dates in platform specific format. 
 *
 *
 */
public class DateTextUtil {

	public static String SIMULATOR_DATE_FORMAT = "dd/MM/yyyy-HH:mm:ss";

    private static final Logger logger = LoggerFactory
            .getLogger(Constants.ICASA_LOG);

	/**
	 * Gets a date from an string using the platform format
	 * @param dateStr The string representation of the date
	 * @return The date object or null if translation cannot be made.
	 */
	public static Date getDateFromText(String dateStr) {
		if (dateStr != null && (!dateStr.isEmpty())) {
			SimpleDateFormat formatter = new SimpleDateFormat(SIMULATOR_DATE_FORMAT);
			Date startDate = null;

			try {
				startDate = formatter.parse(dateStr);
			} catch (ParseException e) {
                logger.warn("Unable to parse Date, the expected format is " + SIMULATOR_DATE_FORMAT);
				e.printStackTrace();
			} finally {
				if (startDate == null)
					startDate = null;
			}
			return startDate;			
		}
		return null;
	}

	/**
	 * Returns the string representation of a date.
	 * 
	 * @param timeInMs The date expressed in milliseconds.
	 * @return The string representation of the date.
	 */
	public static String getTextDate(long timeInMs) {
		return getTextDate((new Date(timeInMs)));
	}

	/**
	 * Returns the string representation of a date.
	 * 
	 * @param date The Date to be formated
	 * @return The string representation of the date.
	 */
	public static String getTextDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat(SIMULATOR_DATE_FORMAT);
		return format.format(date);
	}
	
}
