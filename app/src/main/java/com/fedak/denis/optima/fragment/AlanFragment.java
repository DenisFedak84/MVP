package com.fedak.denis.optima.fragment;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.fedak.denis.optima.R;

import java.util.Locale;

public class AlanFragment extends BaseFragment {

    public static final String FRAGMENT_TAG = "AlanFragmentTag";

    private TextToSpeech tts;

    public static Fragment newInstance() {
        return new AlanFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.alan_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startSpeech(getString(R.string.alan));
    }

    private void startSpeech(final String text) {
        tts = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.ITALIAN);
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    Toast.makeText(getActivity(),
                            "Feature not Supported in Your Device",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finishSpeech();
    }

    private void finishSpeech() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }


}
