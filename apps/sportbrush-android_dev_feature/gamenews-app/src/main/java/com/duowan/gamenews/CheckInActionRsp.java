// **********************************************************************
// This file was generated by a TAF parser!
// TAF version 2.1.5.5
// Generated from `gamenews.jce'
// **********************************************************************

package com.duowan.gamenews;

public final class CheckInActionRsp extends com.duowan.taf.jce.JceStruct implements java.lang.Cloneable
{
    public String className()
    {
        return "gamenews.CheckInActionRsp";
    }

    public String fullClassName()
    {
        return "com.duowan.gamenews.CheckInActionRsp";
    }

    public String desc = "";

    public int checkInType = 0;

    public String giftCode = "";

    public java.util.ArrayList<com.duowan.gamenews.Button> button = null;

    public String copyValue = "";

    public String getDesc()
    {
        return desc;
    }

    public void  setDesc(String desc)
    {
        this.desc = desc;
    }

    public int getCheckInType()
    {
        return checkInType;
    }

    public void  setCheckInType(int checkInType)
    {
        this.checkInType = checkInType;
    }

    public String getGiftCode()
    {
        return giftCode;
    }

    public void  setGiftCode(String giftCode)
    {
        this.giftCode = giftCode;
    }

    public java.util.ArrayList<com.duowan.gamenews.Button> getButton()
    {
        return button;
    }

    public void  setButton(java.util.ArrayList<com.duowan.gamenews.Button> button)
    {
        this.button = button;
    }

    public String getCopyValue()
    {
        return copyValue;
    }

    public void  setCopyValue(String copyValue)
    {
        this.copyValue = copyValue;
    }

    public CheckInActionRsp()
    {
        setDesc(desc);
        setCheckInType(checkInType);
        setGiftCode(giftCode);
        setButton(button);
        setCopyValue(copyValue);
    }

    public CheckInActionRsp(String desc, int checkInType, String giftCode, java.util.ArrayList<com.duowan.gamenews.Button> button, String copyValue)
    {
        setDesc(desc);
        setCheckInType(checkInType);
        setGiftCode(giftCode);
        setButton(button);
        setCopyValue(copyValue);
    }

    public boolean equals(Object o)
    {
        if(o == null)
        {
            return false;
        }

        CheckInActionRsp t = (CheckInActionRsp) o;
        return (
            com.duowan.taf.jce.JceUtil.equals(desc, t.desc) && 
            com.duowan.taf.jce.JceUtil.equals(checkInType, t.checkInType) && 
            com.duowan.taf.jce.JceUtil.equals(giftCode, t.giftCode) && 
            com.duowan.taf.jce.JceUtil.equals(button, t.button) && 
            com.duowan.taf.jce.JceUtil.equals(copyValue, t.copyValue) );
    }

    public int hashCode()
    {
        try
        {
            throw new Exception("Need define key first!");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return 0;
    }
    public java.lang.Object clone()
    {
        java.lang.Object o = null;
        try
        {
            o = super.clone();
        }
        catch(CloneNotSupportedException ex)
        {
            assert false; // impossible
        }
        return o;
    }

    public void writeTo(com.duowan.taf.jce.JceOutputStream _os)
    {
        if (null != desc)
        {
            _os.write(desc, 0);
        }
        _os.write(checkInType, 1);
        if (null != giftCode)
        {
            _os.write(giftCode, 2);
        }
        if (null != button)
        {
            _os.write(button, 3);
        }
        if (null != copyValue)
        {
            _os.write(copyValue, 4);
        }
    }

    static java.util.ArrayList<com.duowan.gamenews.Button> cache_button;

    public void readFrom(com.duowan.taf.jce.JceInputStream _is)
    {
        setDesc( _is.readString(0, false));

        setCheckInType((int) _is.read(checkInType, 1, false));

        setGiftCode( _is.readString(2, false));

        if(null == cache_button)
        {
            cache_button = new java.util.ArrayList<com.duowan.gamenews.Button>();
            com.duowan.gamenews.Button __var_82 = new com.duowan.gamenews.Button();
            ((java.util.ArrayList<com.duowan.gamenews.Button>)cache_button).add(__var_82);
        }
        setButton((java.util.ArrayList<com.duowan.gamenews.Button>) _is.read(cache_button, 3, false));

        setCopyValue( _is.readString(4, false));

    }

    public void display(java.lang.StringBuilder _os, int _level)
    {
        com.duowan.taf.jce.JceDisplayer _ds = new com.duowan.taf.jce.JceDisplayer(_os, _level);
        _ds.display(desc, "desc");
        _ds.display(checkInType, "checkInType");
        _ds.display(giftCode, "giftCode");
        _ds.display(button, "button");
        _ds.display(copyValue, "copyValue");
    }

}

