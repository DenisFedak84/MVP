package com.fedak.denis.optima.fragment;

import android.support.v4.app.Fragment;

import com.fedak.denis.optima.R;

public class SocialFragment extends BaseFragment {

    public static final String FRAGMENT_TAG = "SocialFragmentTag";

    @Override
    protected int getLayoutId() {
        return R.layout.social_fragment;
    }

    public static Fragment newInstance() {
        return new SocialFragment();
    }
}
