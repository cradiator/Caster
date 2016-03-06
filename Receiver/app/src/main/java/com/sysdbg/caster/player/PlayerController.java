package com.sysdbg.caster.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.sysdbg.caster.R;

import java.util.HashMap;
import java.util.Map;

import android.widget.MediaController;

/**
 * Created by crady on 2/8/2016.
 */
public class PlayerController extends MediaController {
    private static final int STEP = 10 * 1000;

    private FrameLayout dialogLayout;
    private FrameLayout controllerLayout;
    private RelativeLayout rootLayout;
    private Callback callback;
    private Map<String, Button> definitionButtonMap;
    private MediaPlayerControl mediaPlayerControl;

    private static FrameLayout.LayoutParams FRAME_LAYOUT_MATCH_PARENT = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
    );

    public interface Callback {
        String[] getDefinitions();
        String getCurrentDefinition();

        void reqeustChangeDefinition(String definition);
    }

    public PlayerController(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        rootLayout = null;
        dialogLayout = null;
        controllerLayout = null;

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
        definitionButtonMap = new HashMap<>();
        if (callback == null) {
            return;
        }

        String[] definitions = callback.getDefinitions();

        LinearLayout definitionLayout = (LinearLayout)dialogLayout.findViewById(R.id.definitionLayout);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        if (definitions.length > 1) {
            for(String def : definitions) {
                Button definitionButton = new Button(definitionLayout.getContext());
                definitionButton.setText(def);
                definitionButton.setOnClickListener(new DefinitionButtonClickListener(def));
                definitionLayout.addView(definitionButton, params);
                definitionButton.setFocusable(true);
                definitionButton.setFocusableInTouchMode(true);

                definitionButtonMap.put(def, definitionButton);
            }
        }
    }

    @Override
    public void show(int milliseconds) {
        if (definitionButtonMap == null) {
            super.show(milliseconds);
            return;
        }

        for(Map.Entry<String, Button> entry : definitionButtonMap.entrySet()) {
            Button button = entry.getValue();
            if (button != null) {
                button.setEnabled(true);
            }
        }

        if (callback != null) {
            String currentDefinition = callback.getCurrentDefinition();
            Button button = definitionButtonMap.get(currentDefinition);
            if (button != null) {
                button.setEnabled(false);
            }
        }

        super.show(milliseconds);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mediaPlayerControl == null) {
            return super.dispatchKeyEvent(event);
        }

        int keyCode = event.getKeyCode();
        boolean firstPress = (event.getRepeatCount() == 0);

        PlayerView playerView = (PlayerView)mediaPlayerControl;

        int step = STEP;
        if (firstPress) {
            step *= 3;
        }
        if  (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
            if (mediaPlayerControl.isPlaying()) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    playerView.takeEffectPendingPosition();
                }
                else if (event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.ACTION_MULTIPLE){
                    playerView.setPendingPostion(playerView.getCurrentPosition() + step);
                }
                show();
                return true;
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
            if (mediaPlayerControl.isPlaying()) {
                int offset = mediaPlayerControl.getCurrentPosition();
                if (offset < step) {
                    offset = 0;
                }
                else {
                    offset -= step;
                }

                if (event.getAction() == KeyEvent.ACTION_UP) {
                    playerView.takeEffectPendingPosition();
                }
                else if (event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.ACTION_MULTIPLE){
                    playerView.setPendingPostion(offset);
                }

                show();
                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void setMediaPlayer(MediaPlayerControl player) {
        mediaPlayerControl = player;
        super.setMediaPlayer(player);
    }

    private class DefinitionButtonClickListener implements OnClickListener {
        private String definition;

        public DefinitionButtonClickListener(String definition) {
            this.definition = definition;
        }

        @Override
        public void onClick(View v) {
            if (callback != null) {
                callback.reqeustChangeDefinition(definition);
            }
            show();
        }
    }
}
