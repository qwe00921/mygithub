// **********************************************************************
// This file was generated by a TAF parser!
// TAF version 2.1.5.5
// Generated from `gamenews.jce'
// **********************************************************************

package com.duowan.gamenews;

public final class ActiveInfoType
{
    private static ActiveInfoType[] __values = new ActiveInfoType[3];
    private int __value;
    private String __T = new String();

    public static final int _ENUM_ACTIVEINFO_TYPE_CHANNEL = 1;
    public static final ActiveInfoType ENUM_ACTIVEINFO_TYPE_CHANNEL = new ActiveInfoType(0,_ENUM_ACTIVEINFO_TYPE_CHANNEL,"ENUM_ACTIVEINFO_TYPE_CHANNEL");
    public static final int _ENUM_ACTIVEINFO_TYPE_URL = 2;
    public static final ActiveInfoType ENUM_ACTIVEINFO_TYPE_URL = new ActiveInfoType(1,_ENUM_ACTIVEINFO_TYPE_URL,"ENUM_ACTIVEINFO_TYPE_URL");
    public static final int _ENUM_ACTIVEINFO_TYPE_TDOU = 3;
    public static final ActiveInfoType ENUM_ACTIVEINFO_TYPE_TDOU = new ActiveInfoType(2,_ENUM_ACTIVEINFO_TYPE_TDOU,"ENUM_ACTIVEINFO_TYPE_TDOU");

    public static ActiveInfoType convert(int val)
    {
        for(int __i = 0; __i < __values.length; ++__i)
        {
            if(__values[__i].value() == val)
            {
                return __values[__i];
            }
        }
        assert false;
        return null;
    }

    public static ActiveInfoType convert(String val)
    {
        for(int __i = 0; __i < __values.length; ++__i)
        {
            if(__values[__i].toString().equals(val))
            {
                return __values[__i];
            }
        }
        assert false;
        return null;
    }

    public int value()
    {
        return __value;
    }

    public String toString()
    {
        return __T;
    }

    private ActiveInfoType(int index, int val, String s)
    {
        __T = s;
        __value = val;
        __values[index] = this;
    }

}
