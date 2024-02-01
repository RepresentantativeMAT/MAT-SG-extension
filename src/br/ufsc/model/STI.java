/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsc.model;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 *
 * @author vanes
 */
public class STI implements Comparable{
    private Point point;
    private TemporalAspect interval;
    private float proportion;

    public STI(TemporalAspect interval, float proportion) {
//        this.point = point;
        this.interval = interval;
        this.proportion = proportion;
    }
    public STI(Date startTime, float proportion, Point point) {
        this.point = point;
        this.interval = new TemporalAspect(startTime);
        this.proportion = proportion;
    }
    public STI(Date startTime, Date endTime, float proportion, Point point) {
        this.point = point;
        this.interval = new TemporalAspect(startTime, endTime);
        this.proportion = proportion;
    }

    public float getProportion() {
        return proportion;
    }

    public void setProportion(float proportion) {
        this.proportion = proportion;
    }

    public TemporalAspect getInterval() {
        return interval;
    }

    public void setInterval(TemporalAspect interval) {
        this.interval = interval;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.interval);
        hash = 97 * hash + Float.floatToIntBits(this.proportion);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final STI other = (STI) obj;
        if (Float.floatToIntBits(this.proportion) != Float.floatToIntBits(other.proportion)) {
            return false;
        }
        if (!Objects.equals(this.interval, other.interval)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        DecimalFormat formatNumber = new DecimalFormat("0.##", new DecimalFormatSymbols(Locale.US));
        
        if(point!=null && point.getTrajectory() != null && point.getTrajectory().isDailyInfo())
            interval.setDailyInfo(true);
        return ""+interval+"::"+formatNumber.format(proportion);
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    @Override
    public int compareTo(Object o) {
        if(this.getInterval().getStartTime().after(((STI)o).getInterval().getStartTime()))
            return 1;
        else if (this.getInterval().getStartTime().before(((STI)o).getInterval().getStartTime()))
            return -1;
        else
            return 0;
    }
}
