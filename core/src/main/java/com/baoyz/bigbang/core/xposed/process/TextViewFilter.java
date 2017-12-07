package com.baoyz.bigbang.core.xposed.process;

import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.Chronometer;
import android.widget.DigitalClock;
import android.widget.EditText;
import android.widget.TextView;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Created by dim on 16/10/24.
 */

public interface TextViewFilter {

    boolean filter(View view);

    String getContent(View view);

    class TextViewValidTextViewFilter implements TextViewFilter {

        @Override
        public boolean filter(View view) {
            return view instanceof TextView && !(view instanceof Button)
                    && !(view instanceof EditText)
                    && !(view instanceof CheckedTextView)
                    && !(view instanceof DigitalClock)
                    && !(view instanceof Chronometer);
        }

        @Override
        public String getContent(View view) {
            if (view instanceof TextView) {
                return ((TextView) view).getText().toString();
            }
            return null;
        }
    }

    class WeChatValidTextViewFilter implements TextViewFilter {

        private static final String TAG = "WeChatValidTextViewFilter";

        Class staticTextViewClass;
        Class mCellTextView;

        public WeChatValidTextViewFilter(ClassLoader classLoader) {
            try {
                staticTextViewClass = classLoader.loadClass("com.tencent.mm.kiss.widget.textview.StaticTextView");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                mCellTextView = classLoader.loadClass("com.tencent.mm.ui.widget.celltextview.CellTextView");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean filter(View view) {
            if (staticTextViewClass != null && staticTextViewClass.isInstance(view)) {
                return true;
            }
            if (mCellTextView != null && mCellTextView.isInstance(view)) {
                return true;
            }
            return false;
        }

        @Override
        public String getContent(View view) {
            if (view instanceof TextView) {
                return null;
            }
            if (staticTextViewClass != null && staticTextViewClass.isInstance(view)) {
                try {
                    Method getText = staticTextViewClass.getMethod("getText");
                    if (getText != null) {
                        Object invoke = getText.invoke(view);
                        if (invoke != null) {
                            return invoke.toString();
                        }
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            if (mCellTextView != null && mCellTextView.isInstance(view)) {
                for (Field field : mCellTextView.getDeclaredFields()) {
                    Class<?> type = field.getType();

                    if (type.getName().startsWith("com.tencent.mm.ui.widget.celltextview")) {
                        field.setAccessible(true);
                        try {
                            Object o = field.get(view);
                            Method getText = type.getDeclaredMethod("getText");
                            if (getText != null) {
                                Object invoke = getText.invoke(o);
                                if (invoke != null) {
                                    return invoke.toString();
                                }
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }
    }
}
