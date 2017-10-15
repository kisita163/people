package com.kisita.people.location;

import android.location.Address;

import java.util.Locale;


/*
 * Created by HuguesKi on 14-10-17.
 */

public class People extends Address {

    private String mPseudo;

    private String mSex;

    //TODO Let user choose his profile picture

    public People(String pseudo, String sex) {

        super(Locale.ENGLISH);

        this.mPseudo = pseudo;
        this.mSex = sex;
    }

    public String getPseudo() {
        return mPseudo;
    }

    public void setPseudo(String pseudo) {
        this.mPseudo = pseudo;
    }

    public String getSex() {
        return mSex;
    }

    public void setSex(String sex) {
        this.mSex = sex;
    }
}
