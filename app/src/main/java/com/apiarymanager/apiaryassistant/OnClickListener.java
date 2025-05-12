package com.apiarymanager.apiaryassistant;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import com.apiarymanager.apiaryassistant.Activity.ArchiveActivity;
import com.apiarymanager.apiaryassistant.Activity.AssistantActivity;
import com.apiarymanager.apiaryassistant.Activity.GuideActivity;
import com.apiarymanager.apiaryassistant.Activity.ReportActivity;

public class OnClickListener implements View.OnClickListener {
    private Activity activity;

    public OnClickListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        Class<?> currentClass = activity.getClass();
        int id = view.getId();

        if ((id == R.id.one && currentClass == ReportActivity.class) ||
                (id == R.id.two && currentClass == AssistantActivity.class) ||
                (id == R.id.three && currentClass == ArchiveActivity.class) ||
                (id == R.id.guide && currentClass == GuideActivity.class)) {
            return;
        }

        if (id == R.id.one) {
            intent = new Intent(activity, ReportActivity.class);
        } else if (id == R.id.two) {
            intent = new Intent(activity, AssistantActivity.class);
        } else if (id == R.id.three) {
            intent = new Intent(activity, ArchiveActivity.class);
        } else if (id == R.id.guide) {
            intent = new Intent(activity, GuideActivity.class);
        } else {
            return;
        }

        activity.startActivity(intent);
        applyTransitionAnimation(currentClass, intent.getComponent().getClassName());
    }

    private void applyTransitionAnimation(Class<?> fromClass, String toClassName) {
        int enterAnim = R.anim.slide_in_right;
        int exitAnim = R.anim.slide_out_left;

        if (fromClass == GuideActivity.class || toClassName.contains("GuideActivity")) {
            enterAnim = R.anim.fade_in;
            exitAnim = R.anim.fade_out;
        } else if (fromClass == ReportActivity.class) {
            enterAnim = R.anim.slide_in_right;
            exitAnim = R.anim.slide_out_left;
        } else if (fromClass == ArchiveActivity.class) {
            enterAnim = R.anim.slide_in_left;
            exitAnim = R.anim.slide_out_right;
        } else if (fromClass == AssistantActivity.class && toClassName.contains("ReportActivity")) {
            enterAnim = R.anim.slide_in_left;
            exitAnim = R.anim.slide_out_right;
        } else if (fromClass == AssistantActivity.class && toClassName.contains("ArchiveActivity")) {
            enterAnim = R.anim.slide_in_right;
            exitAnim = R.anim.slide_out_left;
        }

        activity.overridePendingTransition(enterAnim, exitAnim);
    }

    public static void initMenu(Activity activity, int activeButtonId) {
        OnClickListener onClickListener = new OnClickListener(activity);

        int[] buttonIds = {
                R.id.one,
                R.id.two,
                R.id.three,
                R.id.guide
        };

        for (int id : buttonIds) {
            activity.findViewById(id).setOnClickListener(onClickListener);
        }

        ImageButton activeButton = activity.findViewById(activeButtonId);
        if (activeButton != null) {
            if (activeButtonId == R.id.one) {
                activeButton.setImageResource(R.drawable.one_a);
            } else if (activeButtonId == R.id.two) {
                activeButton.setImageResource(R.drawable.two_a);
            } else if (activeButtonId == R.id.three) {
                activeButton.setImageResource(R.drawable.three_a);
            } else if (activeButtonId == R.id.guide) {
                activeButton.setImageResource(R.drawable.guide_a);
            }
        }

    }
}