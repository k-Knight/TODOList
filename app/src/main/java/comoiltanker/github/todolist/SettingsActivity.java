package comoiltanker.github.todolist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import comoiltanker.github.todolist.TODOResurces.TODOResourceManager;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;

class Colours {
    public static final int RED     = 0xFFFF0000;
    public static final int ORANGE  = 0xFFFFA500;
    public static final int YELLOW  = 0xFFFFFF00;
    public static final int GREEN   = 0xFF00FF00;
    public static final int CYAN    = 0xFF00FFFF;
    public static final int BLUE    = 0xFF0000FF;
    public static final int PURPLE  = 0xFF9400D3;
    
    public static int themeIDToColor (int themeID) {
        switch (themeID) {
            case R.style.RedTheme: return RED;
            case R.style.OrangeTheme: return ORANGE;
            case R.style.YellowTheme: return YELLOW;
            case R.style.GreenTheme: return GREEN;
            case R.style.CyanTheme: return CYAN;
            case R.style.BlueTheme: return BLUE;
            case R.style.PurpleTheme: return PURPLE;
        }
        return 0;
    }
}

public class SettingsActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(TODOResourceManager.getTheme());
        setContentView(R.layout.settings_menu);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");

        LineColorPicker colorPicker =  (LineColorPicker) findViewById(R.id.colorPicker);
        colorPicker.setColors(new int[] {Colours.RED, Colours.ORANGE, Colours.YELLOW, Colours.GREEN, Colours.CYAN, Colours.BLUE, Colours.PURPLE});
        colorPicker.setSelectedColor(Colours.themeIDToColor(TODOResourceManager.getTheme()));

        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                switch (c) {
                    case Colours.RED : TODOResourceManager.setTheme(R.style.RedTheme); break;
                    case Colours.ORANGE : TODOResourceManager.setTheme(R.style.OrangeTheme); break;
                    case Colours.YELLOW : TODOResourceManager.setTheme(R.style.YellowTheme); break;
                    case Colours.GREEN : TODOResourceManager.setTheme(R.style.GreenTheme); break;
                    case Colours.CYAN : TODOResourceManager.setTheme(R.style.CyanTheme); break;
                    case Colours.BLUE : TODOResourceManager.setTheme(R.style.BlueTheme); break;
                    case Colours.PURPLE : TODOResourceManager.setTheme(R.style.PurpleTheme); break;
                }

                recreate();
            }
        });

        // TODO: TodoDoDataManager.setTheme(R.style.AppTheme_Yellow);
    }
}
