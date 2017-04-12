package com.tle.common.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.google.common.base.Throwables;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@NonNullByDefault
public class UtcDate implements TleDate
{
	private static final long serialVersionUID = 1L;

	protected final long utc;
	@Nullable
	protected final String conceptualValue;
	protected final TimeZone zone;

	public UtcDate()
	{
		this(System.currentTimeMillis(), null, DateHelper.UTC_TIMEZONE);
	}

	public UtcDate(long utc)
	{
		this(utc, null, DateHelper.UTC_TIMEZONE);
	}

	public UtcDate(Date date)
	{
		this(date.getTime(), null, DateHelper.UTC_TIMEZONE);
	}

	public UtcDate(Calendar calendar)
	{
		this(calendar.getTime().getTime(), null, DateHelper.UTC_TIMEZONE);
	}

	/**
	 * @param conceptualValue *Must* be in Dates.ISO_DATE_ONLY format
	 */
	protected UtcDate(String conceptualValue) throws ParseException
	{
		this(0, conceptualValue, DateHelper.UTC_TIMEZONE);
		Dates.ISO_DATE_ONLY.parse(conceptualValue, DateHelper.UTC_TIMEZONE);
	}

	/**
	 * @param epochTime
	 * @param isUtcEpoch True if the epochTime was generated by the server. In
	 *            most cases this will be the true
	 */
	protected UtcDate(long utc, @Nullable String conceptualValue, TimeZone zone)
	{
		this.utc = utc;
		this.conceptualValue = conceptualValue;
		this.zone = zone;
	}

	/**
	 * @param utcString This MUST be a UTC date.
	 * @param dateFormat
	 * @throws ParseException
	 */
	public UtcDate(String utcString, Dates dateFormat) throws ParseException
	{
		this(dateFormat.parse(utcString, DateHelper.UTC_TIMEZONE).getTime(), null, DateHelper.UTC_TIMEZONE);
		if( dateFormat == Dates.ISO_DATE_ONLY )
		{
			throw new Error("Use the UtcDate.conceptualDate(String) static method for 'conceptual' dates");
		}
	}

	/**
	 * Milliseconds since 'epoch' in UTC time.
	 * 
	 * @return
	 */
	@Override
	public long toLong()
	{
		if( conceptualValue != null )
		{
			try
			{
				return new UtcDate(Dates.ISO_DATE_ONLY.parse(conceptualValue, DateHelper.UTC_TIMEZONE).getTime())
					.toLong();
			}
			catch( ParseException pe )
			{
				throw Throwables.propagate(pe);
			}
		}
		return utc;
	}

	/**
	 * A java Date initialised with UTC time.
	 */
	@Override
	public Date toDate()
	{
		return new Date(toLong());
	}

	/**
	 * A ISO date representation of this date
	 */
	@Override
	public String toString()
	{
		return (conceptualValue != null ? conceptualValue : format(Dates.ISO));
	}

	@Override
	public boolean equals(Object other)
	{
		if( other instanceof UtcDate )
		{
			UtcDate otherUtc = (UtcDate) other;
			if( conceptualValue != null )
			{
				return (otherUtc.conceptualValue != null && otherUtc.conceptualValue.equals(conceptualValue));
			}
			return utc == ((UtcDate) other).utc;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		if( conceptualValue != null )
		{
			return conceptualValue.hashCode();
		}
		return Long.valueOf(utc).hashCode();
	}

	@Override
	public String format(Dates dateFormat)
	{
		if( conceptualValue != null )
		{
			try
			{
				Date date = Dates.ISO_DATE_ONLY.parse(conceptualValue, zone);
				return dateFormat.format(date, zone);
			}
			catch( ParseException pe )
			{
				// Should never happen
				throw Throwables.propagate(pe);
			}
		}
		return dateFormat.format(toDate(), zone);
	}

	@Nullable
	@Override
	public String formatOrNull(Dates dateFormat)
	{
		return dateFormat.formatOrNull(toDate(), zone);
	}

	/**
	 * Comparable interface method
	 */
	@Override
	public int compareTo(@SuppressWarnings("null") TleDate other)
	{
		if( toLong() == other.toLong() )
		{
			return 0;
		}
		return (toLong() > other.toLong() ? 1 : -1);
		// if( !isConceptual() && !other.isConceptual() )
		// {
		// return toDate().compareTo(other.toDate());
		// }
		// else if( isConceptual() && other.isConceptual() )
		// {
		// // This works because it's an ISO date format
		// return conceptualValue.compareTo(other.getConceptualValue());
		// }
		// else
		// {
		// throw new Error("Cannot compare real dates with conceptual dates");
		// }
	}

	@Override
	public boolean isConceptual()
	{
		return conceptualValue != null;
	}

	@Nullable
	@Override
	public String getConceptualValue()
	{
		return conceptualValue;
	}

	@Override
	public boolean before(TleDate otherDate)
	{
		return compareTo(otherDate) < 0;
	}

	@Override
	public boolean after(TleDate otherDate)
	{
		return compareTo(otherDate) > 0;
	}

	@Override
	public TleDate toMidnight()
	{
		final Calendar c = Calendar.getInstance(zone);
		c.setTime(new Date(toLong()));

		final Calendar newCal = (Calendar) c.clone();
		newCal.set(Calendar.HOUR_OF_DAY, 0);
		newCal.set(Calendar.MINUTE, 0);
		newCal.set(Calendar.SECOND, 0);
		newCal.set(Calendar.MILLISECOND, 0);
		return new UtcDate(newCal.getTimeInMillis());
	}

	@Override
	public TimeZone getTimeZone()
	{
		return zone;
	}

	@Override
	public UtcDate addDays(int days)
	{
		Calendar cal = Calendar.getInstance(zone);
		cal.setTime(new Date(toLong()));
		cal.add(Calendar.DAY_OF_MONTH, days);
		return new UtcDate(cal.getTime());
	}

	public static UtcDate convertLocalMidnightToUtcMidnight(TleDate date, TimeZone zone)
	{
		// check date is actually local midnight
		LocalDate orig = new LocalDate(date.toLong(), zone);
		if( orig.compareTo(orig.toMidnight()) != 0 )
		{
			throw new Error("A local midnight date was not supplied!");
		}
		return new UtcDate(orig.toLong() + zone.getOffset(orig.toLong()));
	}

	public static LocalDate convertUtcMidnightToLocalMidnight(TleDate date, TimeZone zone)
	{
		// check date is actually UTC midnight
		UtcDate orig = new UtcDate(date.toLong());
		if( orig.compareTo(orig.toMidnight()) != 0 )
		{
			throw new Error("A UTC midnight date was not supplied!");
		}
		return new LocalDate(orig.toLong() - zone.getOffset(orig.toLong()), zone);
	}

	@Override
	public UtcDate conceptualDate()
	{
		try
		{
			return conceptualDate(format(Dates.ISO_DATE_ONLY));
		}
		catch( ParseException e )
		{
			// Not possible
			throw Throwables.propagate(e);
		}
	}

	/**
	 * @param date ISO_DATE_ONLY format
	 * @return
	 * @throws ParseException
	 */
	public static UtcDate conceptualDate(String date) throws ParseException
	{
		return new UtcDate(date);
	}
}