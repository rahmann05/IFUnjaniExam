package com.rahman.ifunjaniexam.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class FeedbackUtils {

    public static void showToast(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    public static void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    public static void clickAnim(View view) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 0.93f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 0.93f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f);
        scaleUpX.setDuration(100);
        scaleUpY.setDuration(100);

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleDownX).with(scaleDownY);
        
        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.play(scaleUpX).with(scaleUpY);
        
        scaleDown.start();
        scaleDown.getChildAnimations().get(0).addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                scaleUp.start();
            }
        });
    }

    public static void shakeView(View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        anim.setDuration(500);
        anim.start();
    }
}
