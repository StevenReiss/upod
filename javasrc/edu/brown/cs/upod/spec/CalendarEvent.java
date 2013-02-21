/********************************************************************************/
/*										*/
/*		CalendarEvent.java						*/
/*										*/
/*	This interface represents a calendar event				*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/



package edu.brown.cs.upod.spec;

import java.util.*;



/**
 *	A calendar event represents a series of time slots corresponding to
 *	an event specification.  It might be a one-shot event (i.e. 1/20/2013
 *	from 2:00pm to 4:00pm), or a repeated event (i.e. every Monday at noon,
 *	possibly with exceptions and with a start/stop date).  The basic
 *	idea is that however it is specified, it should be possible to deduce
 *	for a given time period, what time slots are covered by this event.
 *	This can be used both to find overlapping or conflicting events and
 *	to create triggers when events start/stop.
 **/

public interface CalendarEvent extends Describable
{

/**
 *	Given a start and end time, this method should return all the times
 *	that this event is active during that interval.  It assumes the event
 *	is not active at the start.  If the event does not occur during the
 *	interval, this should yield an empty list.  If the event is active at
 *	the start of the interval, the first element in the returned list should
 *	be the from time.  If the event is still active at the end of the
 *	end of the interval, an entry will be added at the end of the list
 *	with the to time.  Entries should also be added to the list whenever
 *	the event starts or stops beyond that.
 **/

List<Calendar> getSlots(Calendar from,Calendar to);



}	// end of interface CalendarEvent




/* end of CalendarEvent.java */

