package timeBench.calendar;

import java.text.ParseException;
import java.util.Date;

import timeBench.data.TemporalDataException;

/**
 * A granularity of a calendar. 
 * <p>
 * Added:         2011-07-19 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class Granularity {
	private Calendar calendar = null;
	private int identifier;
	private int contextIdentifier;
	
	
	/**
	 * The default constructor.
	 * @param calendar The calendar the granularity belongs to.
	 * @param identifier The identifier of the granularity whose meaning depends on the calendar.
	 */
	public Granularity(Calendar calendar, int identifier, int contextIdentifier){
		this.calendar = calendar;
		this.identifier = identifier;
		this.contextIdentifier = contextIdentifier;
	}
	
	
    /**
     * Calculate a timeStamp a given number of granules before another timeStamp.
     * @param timeStamp The base timeStamp.
     * @param granules The number of granules.
     * @return The resulting timeStamp.
     * @throws TemporalDataException
     */
	public long before(long timeStamp,long granules) throws TemporalDataException
	{
		return calendar.before(timeStamp,granules,identifier);
	}

	
    /**
     * Calculate a timeStamp a given number of granules after another timeStamp.
     * @param timeStamp The base timeStamp.
     * @param granules The number of granules.
     * @return The resulting timeStamp.
     * @throws TemporalDataException
     */	public long after(long timeStamp,long granules) throws TemporalDataException
	{
		return calendar.after(timeStamp,granules,identifier);
	}
     
     
     /**
      * Return calendar this granularity belongs to.
      * @return The calendar this granularity belongs to.
      */
     public Calendar getCalendar()
     {
    	 return calendar;
     }


	/** Converts a granule in a granularity to another granularity, but returns only one granule,
	 * using heuristics to decide which one if more would be correct.
	 * @param timeStamp The number of the granule in the original granularity.
	 * @return The number of the corresponding granule in the new granularity.
	 * @throws TemporalDataException 
	 */
	public Granule mapGranuleToGranularityAsGranule(long timeStamp, Granularity targetGranularity) throws TemporalDataException {
		return calendar.mapGranuleToGranularityAsGranule(timeStamp,identifier,targetGranularity.getIdentifier());
	}


	/**
	 * Returns identifier of this granularity.
	 * @return Identifier of this granularity
	 */
	public int getIdentifier() {
		return identifier;
	}

	public int getGranularityContextIdentifier() {
		return contextIdentifier;
	}
	

	/** Converts a granule in a granularity to another granularity and returns a list of all granules that are part of
	 * it. Use heuristics if necessary.
	 * @param timeStamp The number of the granule in the original granularity.
	 * @return The list of numbers of the corresponding granules in the new granularity.
	 * @throws TemporalDataException 
	 */
	public java.util.ArrayList<Granule> mapGranuleToGranularityAsGranuleList(long timeStamp,
			Granularity targetGranularity) throws TemporalDataException {
		return calendar.mapGranuleToGranularityAsGranuleList(timeStamp,identifier,targetGranularity.getIdentifier());
	}
	
	
//	public Granule parseStringToGranule(String input) throws ParseException, TemporalDataException {
//		return calendar.parseStringToGranule(input,this);
//	}
//
//    public Granule parseStringToGranule(String input, String dateTimePattern) throws ParseException, TemporalDataException {
//        return calendar.parseStringToGranule(input,this, dateTimePattern);
//    }
    
    public Granule parseDateToGranule(Date input) throws TemporalDataException {
        return calendar.parseDateToGranule(input, this);
    }

	public Granule parseInfToGranule(long inf) throws TemporalDataException {
		return calendar.parseInfToGranule(inf,this);
	}


	public long getGranuleIdentifier(Granule granule) throws TemporalDataException {
		return calendar.getGranuleIdentifier(granule);		
	}


	public Long getInf(Granule granule) throws TemporalDataException {
		return calendar.getInf(granule);
	}


	public Long getSup(Granule granule) throws TemporalDataException {
		return calendar.getSup(granule);
	}

    public long getContext(Granule granule)  throws TemporalDataException {
		return calendar.getGranuleContextIdentifier(granule);
	}
}
