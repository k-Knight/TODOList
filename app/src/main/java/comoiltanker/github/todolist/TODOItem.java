package comoiltanker.github.todolist;

import java.util.Calendar;

public class TODOItem {
    public String title;
    public Calendar date;
    public boolean checked;
    public String description;
    public String id;
    public String imageURL;

    //public TODOItem(String id, String title, Calendar date, boolean check) {
    //    this.id = id;
    //    this.title = title;
    //    this.date = date;
    //    this.checked = check;
    //    this.description = "";
    //}

    public TODOItem(String id, String title, Calendar date, boolean check, String description, String imageURL) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.checked = check;
        this.description = description;
        this.imageURL = imageURL;
    }
}
