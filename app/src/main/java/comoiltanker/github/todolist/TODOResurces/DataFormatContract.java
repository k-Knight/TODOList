package comoiltanker.github.todolist.TODOResurces;

import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DataFormatContract {
    public static final SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS", Locale.ENGLISH);
    public static final SimpleDateFormat humanDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
}
