package com.ioansen.today;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A <code>Label</code> object is used to define a label/tag
 * in the form of a text. It also has a color attribute for
 * quick reference.
 * <p>
 * This class is immutable.
 *
 * @since 1.0
 */
public class Label implements Parcelable{

    private final long id;
    private final String text;
    /**The color of this label
     * represented as a 32-bit ARGB*/
    private final int color;

    public Label(long id, String text, int color) {
        this.id = id;
        this.text = text;
        this.color = color;
    }

    public Label(String text, int color) {
        this.id = -1;
        this.text = text;
        this.color = color;
    }

    public Label(Parcel source){
        this.id = source.readLong();
        this.text = source.readString();
        this.color = source.readInt();
    }

    public long getId() {
        return id;
    }


    public String getText() {
        return text;
    }

    public int getColor() {
        return color;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(text);
        dest.writeInt(color);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Label> CREATOR = new Creator<Label>() {
        @Override
        public Label createFromParcel(Parcel source) {
            return new Label(source);
        }

        @Override
        public Label[] newArray(int size) {
            return new Label[0];
        }
    };
}
