package com.baoyz.bigbang.core.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by didm on 17/5/11.
 */

public class SearchMapAction implements Action {

    public static SearchMapAction create() {
        return new SearchMapAction();
    }
    @Override
    public void start(Context context, String text) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + text)));
    }
}
