package sun.bob.mcalendarview.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.R;
import sun.bob.mcalendarview.listeners.OnDateClickListener;
import sun.bob.mcalendarview.utils.CurrentCalendar;
import sun.bob.mcalendarview.views.BaseCellView;
import sun.bob.mcalendarview.views.BaseMarkView;
import sun.bob.mcalendarview.views.DefaultCellView;
import sun.bob.mcalendarview.views.DefaultMarkView;
import sun.bob.mcalendarview.vo.DayData;
import sun.bob.mcalendarview.vo.MarkedDates;

/**
 * Created by bob.sun on 15/8/27.
 */
public class CalendarAdapter extends ArrayAdapter implements Observer {
    private ArrayList data;
    private int cellView = -1;
    private int markView = -1;
    public CalendarAdapter(Context context, int resource, ArrayList data) {
        super(context, resource);
        this.data = data;
        MarkedDates.getInstance().addObserver(this);
    }

    public CalendarAdapter setCellViews(int cellView, int markView){
        this.cellView = cellView;
        this.markView = markView;
        return this;
    }


    public View getView(int position, View convertView, ViewGroup viewGroup){
        View ret = null;
        DayData dayData = (DayData) data.get(position);
        MarkStyle style = MarkedDates.getInstance().check(dayData.getDate());
        boolean marked = style != null;
        if (marked){
            dayData.getDate().setMarkStyle(style);
            if (markView > 0){
                BaseMarkView baseMarkView = (BaseMarkView) View.inflate(getContext(), markView, null);
                baseMarkView.setDisplayText(dayData);
                ret = baseMarkView;
            } else {
                ret = new DefaultMarkView(getContext());
                ((DefaultMarkView) ret).setDisplayText(dayData);
            }
        } else {
            if (cellView > 0) {
                BaseCellView baseCellView = (BaseCellView) View.inflate(getContext(), cellView, null);
                baseCellView.setDisplayText(dayData);
                ret = baseCellView;
            } else {
                ret = new DefaultCellView(getContext());
                ((DefaultCellView) ret).setDisplayText(dayData);
            }
        }
        ((BaseCellView) ret).setDate(dayData.getDate());
        if (OnDateClickListener.instance != null) {
                ((BaseCellView) ret).setOnDateClickListener(OnDateClickListener.instance);
        }
        if (dayData.getDate().equals(CurrentCalendar.getCurrentDateData())){
            ret.setBackground(MarkStyle.todayBackground);
//            ret.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.ic_brightness_1_24dp));
        }
        return ret;
    }

    @Override
    public int getCount(){
        return data.size();
    }

    @Override
    public void update(Observable observable, Object data) {
        this.notifyDataSetChanged();
    }
}
