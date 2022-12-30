package com.xiongtao.myglide;

import android.content.Context;

public class GlideBuilder {

    public GlideBuilder(Context context){

    }

    public Glide build(){
        RequestManagerRetriver retriver = new RequestManagerRetriver();
        Glide glide = new Glide(retriver);
        return glide;
    }
}
