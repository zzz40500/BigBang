/*
 * The MIT License (MIT)
 * Copyright (c) 2016 baoyongzhang <baoyz94@gmail.com>
 */
package com.baoyz.bigbang.core.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.net.URLEncoder;

/**
 * Created by baoyongzhang on 2016/10/24.
 */
public class BigBangService extends AccessibilityService {

    private CharSequence mWindowClassName;
    private static final String TAG = "BigBangService";
    private long lastTime;
    private CharSequence txt;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        CharSequence className = event.getClassName();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {
                CharSequence windowClassName = event.getClassName();
                if (isWechatUI() && windowClassName.equals("android.widget.FrameLayout")) {
                    event.getSource().getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } else {
                    mWindowClassName = windowClassName;
                }
                break;
            }
            case AccessibilityEvent.TYPE_VIEW_CLICKED: {
                if (isWechatUI() && "android.widget.TextView".equals(className)) {
                    AccessibilityNodeInfo source = event.getSource();
                    CharSequence text = source.getText();
                    if (text != null) {
                        ;
                        if (text.length() > 3 && text.equals(txt) && System.currentTimeMillis() - lastTime < 1000) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("bigBang://?extra_text=" + URLEncoder.encode(text.toString(), "utf-8")));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        txt = text;
                        lastTime = System.currentTimeMillis();
                    }

                }
                break;
            }
        }
    }

    private boolean isWechatUI() {
        return "com.tencent.mm.ui.LauncherUI".equals(mWindowClassName);
    }

    @Override
    public void onInterrupt() {

    }

    public static boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        final String service = getServiceKey(context);
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessibilityService = splitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }

        return accessibilityFound;
    }

    @NonNull
    public static String getServiceKey(Context context) {
        return context.getPackageName() + "/" + BigBangService.class.getCanonicalName();
    }
}
