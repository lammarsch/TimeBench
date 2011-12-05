package timeBench.calendar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import timeBench.data.TemporalDataException;

/**
 * The interface used to access any CalendarManager that may exist.
 * 
 * <p>
 * Added:         2011-08-19 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public interface CalendarManager {

	public Calendar calendar();
	public Calendar getDefaultCalendar();
	public long before(long timeStamp, long granules, int granularityIdentifier) throws TemporalDataException;
	public long after(long timeStamp, long granules, int granularityIdentifier) throws TemporalDataException;
	public Granule mapGranuleToGranularityAsGranule(long timeStamp,int sourceGranularity, int targetGranularity) throws TemporalDataException;
	public ArrayList<Granule> mapGranuleToGranularityAsGranuleList(long timeStamp,int sourceGranularity, int targetGranularity)
			throws TemporalDataException, TemporalDataException;
//	Granule parseStringToGranule(String input, Granularity granularity)
//			throws ParseException, TemporalDataException;
//
//    Granule parseStringToGranule(String input, Granularity granularity,
//            String dateTimePattern) throws ParseException,
//            TemporalDataException;

    Granule parseDateToGranule(Date input, Granularity granularity)
            throws TemporalDataException;
    
	public int[] getGranularityIdentifiers();
	public Granule parseInfToGranule(long inf,Granularity granularity) throws TemporalDataException;
	public long getGranuleIdentifier(Granule granule) throws TemporalDataException;
	public Long getInf(Granule granule) throws TemporalDataException;
	public Long getSup(Granule granule) throws TemporalDataException;
	public int getBottomGranularityIdentifier();
	public int getTopGranularityIdentifier();
}