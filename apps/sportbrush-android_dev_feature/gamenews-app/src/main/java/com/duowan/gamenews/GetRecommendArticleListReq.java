// **********************************************************************
// This file was generated by a TAF parser!
// TAF version 2.1.4.3.
// Generated from `gamenews.jce'
// **********************************************************************

package com.duowan.gamenews;

public final class GetRecommendArticleListReq extends com.duowan.taf.jce.JceStruct implements java.lang.Cloneable
{
    public String className()
    {
        return "gamenews.GetRecommendArticleListReq";
    }

    public String fullClassName()
    {
        return "com.duowan.gamenews.GetRecommendArticleListReq";
    }

    public int channelId = 0;

    public int count = 0;

    public int getChannelId()
    {
        return channelId;
    }

    public void  setChannelId(int channelId)
    {
        this.channelId = channelId;
    }

    public int getCount()
    {
        return count;
    }

    public void  setCount(int count)
    {
        this.count = count;
    }

    public GetRecommendArticleListReq()
    {
        setChannelId(channelId);
        setCount(count);
    }

    public GetRecommendArticleListReq(int channelId, int count)
    {
        setChannelId(channelId);
        setCount(count);
    }

    public boolean equals(Object o)
    {
        if(o == null)
        {
            return false;
        }

        GetRecommendArticleListReq t = (GetRecommendArticleListReq) o;
        return (
            com.duowan.taf.jce.JceUtil.equals(channelId, t.channelId) && 
            com.duowan.taf.jce.JceUtil.equals(count, t.count) );
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
        _os.write(channelId, 0);
        _os.write(count, 1);
    }


    public void readFrom(com.duowan.taf.jce.JceInputStream _is)
    {
        setChannelId((int) _is.read(channelId, 0, false));

        setCount((int) _is.read(count, 1, false));

    }

    public void display(java.lang.StringBuilder _os, int _level)
    {
        com.duowan.taf.jce.JceDisplayer _ds = new com.duowan.taf.jce.JceDisplayer(_os, _level);
        _ds.display(channelId, "channelId");
        _ds.display(count, "count");
    }

}

