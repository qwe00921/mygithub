// **********************************************************************
// This file was generated by a TAF parser!
// TAF version 2.1.5.5
// Generated from `gamenews.jce'
// **********************************************************************

package com.duowan.gamenews;

public final class updateStoreAppStatusReq extends com.duowan.taf.jce.JceStruct implements java.lang.Cloneable
{
    public String className()
    {
        return "gamenews.updateStoreAppStatusReq";
    }

    public String fullClassName()
    {
        return "com.duowan.gamenews.updateStoreAppStatusReq";
    }

    public java.util.Map<Integer, Integer> updateData = null;

    public java.util.Map<Integer, Integer> getUpdateData()
    {
        return updateData;
    }

    public void  setUpdateData(java.util.Map<Integer, Integer> updateData)
    {
        this.updateData = updateData;
    }

    public updateStoreAppStatusReq()
    {
        setUpdateData(updateData);
    }

    public updateStoreAppStatusReq(java.util.Map<Integer, Integer> updateData)
    {
        setUpdateData(updateData);
    }

    public boolean equals(Object o)
    {
        if(o == null)
        {
            return false;
        }

        updateStoreAppStatusReq t = (updateStoreAppStatusReq) o;
        return (
            com.duowan.taf.jce.JceUtil.equals(updateData, t.updateData) );
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
        if (null != updateData)
        {
            _os.write(updateData, 0);
        }
    }

    static java.util.Map<Integer, Integer> cache_updateData;

    public void readFrom(com.duowan.taf.jce.JceInputStream _is)
    {
        if(null == cache_updateData)
        {
            cache_updateData = new java.util.HashMap<Integer, Integer>();
            Integer __var_84 = 0;
            Integer __var_85 = 0;
            cache_updateData.put(__var_84, __var_85);
        }
        setUpdateData((java.util.Map<Integer, Integer>) _is.read(cache_updateData, 0, false));

    }

    public void display(java.lang.StringBuilder _os, int _level)
    {
        com.duowan.taf.jce.JceDisplayer _ds = new com.duowan.taf.jce.JceDisplayer(_os, _level);
        _ds.display(updateData, "updateData");
    }

}

