package com.sysdbg.caster.player;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;

import com.sysdbg.caster.R;

/**
 * Created by crady on 2/8/2016.
 */
public class PlayerController extends MediaController {
    private FrameLayout dialogLayout;
    private FrameLayout controllerLayout;
    private RelativeLayout rootLayout;
    private Callback callback;

    private static FrameLayout.LayoutParams FRAME_LAYOUT_MATCH_PARENT = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
    );

    public interface Callback {
        int getSectionCount();
        int getCurrentSection();

        int[] getResolutions();
        int getCurrentResolution();

        void requestChangeSection(int sectionNumber);
        void reqeustChangeResolution(int resolution);
    }

    public PlayerController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerController(Context context, boolean useFastForward) {
        super(context, useFastForward);
    }

    public PlayerController(Context context) {
        super(context);
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void setAnchorView(View view) {
        // make system generated view and remove it from root
        super.setAnchorView(view);

        View controllerRoot = getChildAt(0);
        removeView(controllerRoot);

        // generate new root
        makeRootLayout();
        controllerLayout.addView(controllerRoot, FRAME_LAYOUT_MATCH_PARENT);

        addView(rootLayout, FRAME_LAYOUT_MATCH_PARENT);
    }

    private View makeRootLayout() {
        // create view
        LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflate.inflate(R.layout.fragment_playercontroller, null);

        // init layouts
        rootLayout = (RelativeLayout)view;
        dialogLayout = (FrameLayout)view.findViewById(R.id.dialogFrame);
        controllerLayout = (FrameLayout)view.findViewById(R.id.controllerFrame);

        // init dialog
        makeDialog();

        return view;
    }

    private void makeDialog() {
        if (callback == null) {
            return;
        }

        int sectionCount = callback.getSectionCount();
        int currentSection = callback.getCurrentSection();
        int[] resolutions = callback.getResolutions();
        int currentResolution = callback.getCurrentResolution();

        LinearLayout sectionLayout = (LinearLayout)dialogLayout.findViewById(R.id.sectionLayout);
        LinearLayout resolutionLayout = (LinearLayout)dialogLayout.findViewById(R.id.resolutionLayout);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        if (sectionCount > 1) {
            for(int i = 0; i < sectionCount; i++) {
                Button sectionButton  = new Button(sectionLayout.getContext());
                sectionButton.setText(String.format("Section %d", i));
                sectionButton.setOnClickListener(new SectionButtonClickListener(i));
                if (i == currentSection) {
                    sectionButton.setEnabled(false);
                }

                sectionLayout.addView(sectionButton, params);
            }
        }

        if (resolutions.length > 1) {
            for(int res : resolutions) {
                Button resolustionButton = new Button(resolutionLayout.getContext());
                resolustionButton.setText(String.format("%dp", res));
                resolustionButton.setOnClickListener(new ResolutionButtonClickListener(res));
                if (res == currentResolution) {
                    resolustionButton.setEnabled(false);
                }

                resolutionLayout.addView(resolustionButton, params);
            }
        }
    }

    private class SectionButtonClickListener implements OnClickListener {
        private int sectionNumber;

        public SectionButtonClickListener(int sectionNumber) {
            this.sectionNumber = sectionNumber;
        }

        @Override
        public void onClick(View v) {
            if (callback != null) {
                callback.requestChangeSection(sectionNumber);
            }
            show();
        }
    }

    private class ResolutionButtonClickListener implements OnClickListener {
        private int resolution;

        public ResolutionButtonClickListener(int resolution) {
            this.resolution = resolution;
        }

        @Override
        public void onClick(View v) {
            if (callback != null) {
                callback.reqeustChangeResolution(resolution);
            }
            show();
        }
    }
}
